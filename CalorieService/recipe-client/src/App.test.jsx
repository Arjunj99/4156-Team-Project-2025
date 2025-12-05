// App.test.jsx
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import App from "./App";

// We Mock the config so API_BASE_URL is always defined in tests
jest.mock("./config", () => ({
  API_BASE_URL: "http://mock-api",
}));

/**
 * This file contains unit tests for the App component
 * of the demo client program.
 */

describe("App component", () => {
  let originalFetch;

  /**
   * Set up globals before any tests run.
   */
  beforeAll(() => {
    // Save original fetch so we can restore later
    originalFetch = global.fetch;

    // We Mock crypto.randomUUID so instanceId is stable in tests
    Object.defineProperty(global, "crypto", {
      value: {
        randomUUID: jest.fn(() => "test-instance-id"),
      },
      configurable: true,
    });
  });

  /**
   * Reset mocks and create a fresh fetch mock for each test.
   */
  beforeEach(() => {
    // Clean localStorage between tests
    window.localStorage.clear();

    // Jest mock for fetch
    global.fetch = jest.fn();
  });

  /**
   * Restore original globals after all tests complete.
   */
  afterAll(() => {
    global.fetch = originalFetch;
  });

  /**
   * Tests that the header and basic UI render without crashing.
   */
  test("renders header and basic layout", () => {
    render(<App />);

    expect(
      screen.getByRole("heading", { name: /Healthy Recipe Client/i })
    ).toBeInTheDocument();

    expect(screen.getByText(/Service client ID/i)).toBeInTheDocument();
    expect(screen.getByText(/Local User/i)).toBeInTheDocument();
    expect(screen.getByText(/Healthy Recommendations/i)).toBeInTheDocument();
  });

  /**
   * Tests that trying to find healthy recipes without signing in
   * shows an error message.
   */
  test("shows error if trying to load healthy recipes when not signed in", async () => {
    render(<App />);

    const button = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });

    fireEvent.click(button);

    // Error is shown in the message bar
    await waitFor(() => {
      expect(
        screen.getByText(/Please enter a user id and press Sign In first\./i)
      ).toBeInTheDocument();
    });
  });

  /**
   * Tests that submitting sign-in with an empty user id shows the proper error.
   */
  test("shows error when trying to sign in with empty user id", async () => {
    // logEvent won't be called because handleSignIn returns early
    render(<App />);

    const input = screen.getByLabelText(/User ID/i);
    const button = screen.getByRole("button", { name: /Sign In/i });

    fireEvent.change(input, { target: { value: "   " } });
    fireEvent.click(button);

    await waitFor(() => {
      expect(
        screen.getByText(/Please enter a user id\./i)
      ).toBeInTheDocument();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });

  /**
   * Tests that the user can sign in locally and that the signed-in
   * message appears.
   */
  test("allows user to sign in and shows signed-in state", async () => {
    // First fetch call is for /client/log in logEvent()
    global.fetch.mockResolvedValueOnce({
      ok: true,
      text: async () => "",
    });

    render(<App />);

    const input = screen.getByLabelText(/User ID/i);
    const button = screen.getByRole("button", { name: /Sign In/i });

    fireEvent.change(input, { target: { value: "alice01" } });
    fireEvent.click(button);

    // Signed-in text should appear
    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // "alice01" appears in multiple places (toast + info line), so use getAllByText
    const matches = screen.getAllByText(/alice01/);
    expect(matches.length).toBeGreaterThanOrEqual(1);

    // Ensure logEvent triggered a fetch to /client/log
    expect(global.fetch).toHaveBeenCalledWith(
      "/client/log",
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          "Content-Type": "application/json",
        }),
      })
    );
  });

  /**
   * Tests that after signing in, loading healthy recipes calls the API
   * and renders recipe cards.
   */
  test("loads healthy recipes and displays them", async () => {
    // First fetch: sign-in logging (/client/log)
    // Second fetch: /user/recommendHealthy
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify([
            {
              id: 101,
              title: "Test Healthy Recipe",
              totalCalories: 450,
              category: "Healthy",
            },
          ]),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "bob01" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    // Recipe card should be rendered
    await waitFor(() => {
      expect(
        screen.getByText("Test Healthy Recipe")
      ).toBeInTheDocument();
    });

    // Check calories in a way that matches the DOM structure
    expect(screen.getByText(/Estimated total:/i)).toBeInTheDocument();
    expect(screen.getByText("450")).toBeInTheDocument();
  });

  /**
   * Tests that liking a recipe updates the UI (button becomes "Liked").
   */
  test("liking a recipe updates the button state to Liked", async () => {
    // 1. Sign-in log
    // 2. RecommendHealthy
    // 3. LikeRecipe API call
    // 4. Local /client/log from logEvent in likeRecipe
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify([
            {
              id: 202,
              title: "Likeable Recipe",
              totalCalories: 300,
            },
          ]),
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "charlie01" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    await waitFor(() => {
      expect(screen.getByText("Likeable Recipe")).toBeInTheDocument();
    });

    // Click Like
    const likeButton = screen.getByRole("button", { name: /Like/i });
    fireEvent.click(likeButton);

    // It should now show "Liked" and be disabled
    await waitFor(() => {
      expect(screen.getByRole("button", { name: /Liked/i })).toBeDisabled();
    });
  });

  /**
   * Tests branch where healthy recommendation returns an empty list
   * and the "no healthy recipes" toast is shown.
   */
  test("shows toast when no healthy recipes are found", async () => {
    // 1. Sign-in log
    // 2. recommendHealthy returns empty array
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify([]),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "noresults" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    // Toast should appear indicating no healthy recipes
    await waitFor(() => {
      expect(
        screen.getByText(
          /No healthy recipes found for this client and calorie limit\./i
        )
      ).toBeInTheDocument();
    });
  });

  /**
   * Tests callApi error path where the backend returns a plain string error.
   */
  test("shows error when healthy recommendations fail with string error", async () => {
    // 1. Sign-in log
    // 2. recommendHealthy fails with string body
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        text: async () => "Server error occurred",
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "erruser" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes to trigger the failing callApi
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    await waitFor(() => {
      expect(
        screen.getByText(/Server error occurred/i)
      ).toBeInTheDocument();
    });
  });

  /**
   * Tests callApi error path where the backend returns a JSON object error.
   */
  test("shows error when healthy recommendations fail with JSON error object", async () => {
    // 1. Sign-in log
    // 2. recommendHealthy fails with JSON body { message: "Bad request" }
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: false,
        status: 400,
        text: async () => JSON.stringify({ message: "Bad request" }),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "jsonerr" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes to trigger the failing callApi
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    await waitFor(() => {
      expect(screen.getByText(/Bad request/i)).toBeInTheDocument();
    });
  });

  /**
   * Tests viewing recipe details, hitting the branches in openRecipeDetails:
   * - viewRecipe returns an object with a "recipe" property
   * - calorieBreakdown returns a non-object, so "Raw response" branch is used
   */
  test("opens recipe details and shows breakdown from raw response", async () => {
    // Fetch order for this test:
    // 1) /client/log (sign-in)
    // 2) /user/recommendHealthy (list of recipes)
    // 3) /recipe/viewRecipe (details)
    // 4) /recipe/calorieBreakdown (non-object to hit 'Raw response' branch)
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify([
            {
              id: 303,
              title: "Details Recipe",
              totalCalories: 500,
            },
          ]),
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify({
            recipe: {
              title: "Full Details Recipe",
              ingredients: ["Tomato", "Cheese"],
              steps: ["Step 1", "Step 2"],
            },
          }),
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () => JSON.stringify(["not", "an", "object"]),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "detailsuser" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    await waitFor(() => {
      expect(screen.getByText("Details Recipe")).toBeInTheDocument();
    });

    // Click "View Details" on that recipe card
    const viewButton = screen.getByRole("button", { name: /View Details/i });
    fireEvent.click(viewButton);

    // The final recipe title uses the original list recipe's title
    // because { ...viewResponse.recipe, ...recipe } spreads `recipe` last.
    await waitFor(() => {
      expect(screen.getByText("Details Recipe")).toBeInTheDocument();
    });

    // Because calorieBreakdown returned a non-object (array),
    // the component should show a "Raw response" entry
    await waitFor(() => {
      expect(screen.getByText(/Raw response/i)).toBeInTheDocument();
    });
  });

  /**
   * Tests viewing recipe details when calorieBreakdown returns a proper object,
   * hitting the "normal" breakdown branch.
   */
  test("opens recipe details and shows normal calorie breakdown object", async () => {
    // Fetch order:
    // 1) /client/log
    // 2) /user/recommendHealthy
    // 3) /recipe/viewRecipe (no 'recipe' property, direct object)
    // 4) /recipe/calorieBreakdown (object)
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify([
            {
              id: 404,
              title: "Breakdown Recipe",
              totalCalories: 250,
            },
          ]),
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify({
            title: "Breakdown Recipe",
            ingredients: ["Apple"],
            steps: ["Eat"],
          }),
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify({
            Apple: 100,
            "Other stuff": 150,
          }),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "breakdownUser" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    await waitFor(() => {
      expect(screen.getByText("Breakdown Recipe")).toBeInTheDocument();
    });

    // Click "View Details"
    const viewButton = screen.getByRole("button", { name: /View Details/i });
    fireEvent.click(viewButton);

    // Breakdown rows should show the object keys.
    // "Apple" appears both in the table and as an ingredient, so we use getAllByText.
    await waitFor(() => {
      expect(screen.getAllByText("Apple").length).toBeGreaterThanOrEqual(1);
    });
    expect(screen.getByText("100")).toBeInTheDocument();
    expect(screen.getByText(/Other stuff/i)).toBeInTheDocument();
  });

  /**
   * Tests that logEvent failing (fetch rejecting) does not prevent sign-in UI update.
   * This hits the catch branch in logEvent.
   */
  test("sign-in still works even if logEvent fails", async () => {
    const warnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

    // First fetch (logEvent) will reject
    global.fetch.mockRejectedValueOnce(new Error("network down"));

    render(<App />);

    const input = screen.getByLabelText(/User ID/i);
    const button = screen.getByRole("button", { name: /Sign In/i });

    fireEvent.change(input, { target: { value: "failLog" } });
    fireEvent.click(button);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
      expect(screen.getAllByText(/failLog/).length).toBeGreaterThanOrEqual(1);
    });

    warnSpy.mockRestore();
  });

  /**
   * Tests that loadHealthyRecipes handles an object with a `recipes` property
   * and that getRecipeId/getRecipeTitle work with recipeId/recipeName fields.
   */
  test("loads recipes from data.recipes using recipeId and recipeName", async () => {
    // 1) /client/log (sign-in)
    // 2) /user/recommendHealthy returns an object with a `recipes` array
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify({
            recipes: [
              {
                recipeId: 777,
                recipeName: "From recipes field",
                totalCalories: 123,
              },
            ],
          }),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "recipesUser" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    // Card should use recipeName as title, and recipeId internally for key/id
    await waitFor(() => {
      expect(
        screen.getByText("From recipes field")
      ).toBeInTheDocument();
    });
  });

  /**
   * Tests that getRecipeTitle falls back to "Recipe <id>" when no
   * title-like fields are present.
   */
  test('falls back to "Recipe <id>" title when name fields are missing', async () => {
    // 1) /client/log (sign-in)
    // 2) /user/recommendHealthy returns a recipe with only `id`
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify([
            {
              id: 999,
              totalCalories: 321,
            },
          ]),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "fallbackUser" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    // Title should be derived from id: "Recipe 999"
    await waitFor(() => {
      expect(screen.getByText("Recipe 999")).toBeInTheDocument();
    });
  });

  /**
   * Tests that clicking "View Details" on a recipe with no id/recipeId
   * does nothing (hits `if (recipeId == null) return` branch in openRecipeDetails).
   */
  test("view details does nothing when recipe has no id", async () => {
    // 1) /client/log (sign-in)
    // 2) /user/recommendHealthy returns a recipe with no id/recipeId
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        text: async () => "",
      })
      .mockResolvedValueOnce({
        ok: true,
        text: async () =>
          JSON.stringify([
            {
              title: "No ID Recipe",
              totalCalories: 111,
            },
          ]),
      });

    render(<App />);

    // Sign in
    const input = screen.getByLabelText(/User ID/i);
    const signInButton = screen.getByRole("button", { name: /Sign In/i });
    fireEvent.change(input, { target: { value: "noIdUser" } });
    fireEvent.click(signInButton);

    await waitFor(() => {
      expect(screen.getByText(/Signed in as/i)).toBeInTheDocument();
    });

    // Load recipes
    const loadButton = screen.getByRole("button", {
      name: /Find Healthy Recipes/i,
    });
    fireEvent.click(loadButton);

    await waitFor(() => {
      expect(screen.getByText("No ID Recipe")).toBeInTheDocument();
    });

    // Click "View Details" — since getRecipeId returns null, openRecipeDetails
    // should return early and not call any more APIs
    const viewButton = screen.getByRole("button", { name: /View Details/i });
    fireEvent.click(viewButton);

    // We only expect the two original fetches (log + recommendHealthy)
    expect(global.fetch).toHaveBeenCalledTimes(2);

    // Recipe details panel should still show the default placeholder text
    expect(
      screen.getByText(
        /Select a recipe to see its ingredients and calories\./i
      )
    ).toBeInTheDocument();
  });
});
