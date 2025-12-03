import { useState } from "react";
import { API_BASE_URL } from "./config";

const SERVICE_CLIENT_ID = 502;

function getRecipeId(recipe) {
  return (
    recipe?.id ??
    recipe?.recipeId ??
    recipe?.recipeID ?? // just in case
    recipe?.recipe_id ??
    null
  );
}

function getRecipeTitle(recipe) {
  if (!recipe) return "Recipe";

  return (
    recipe.title ||
    recipe.name ||
    recipe.recipeName ||
    recipe.recipe_title ||
    recipe.recipeTitle ||
    recipe.label ||
    recipe.foodName ||
    recipe.food_name ||
    recipe.foodTitle ||
    (getRecipeId(recipe) != null ? `Recipe ${getRecipeId(recipe)}` : "Recipe")
  );
}

// Component

function App() {
  // client user details
  const [userId, setUserId] = useState("");
  const [signedIn, setSignedIn] = useState(false);

  const [calorieMax, setCalorieMax] = useState(600);
  const [recipes, setRecipes] = useState([]);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [calorieBreakdown, setCalorieBreakdown] = useState(null);

  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState("");
  const [toast, setToast] = useState("");

  // Track which recipes have been liked in this client session (for UI state)
  const [likedRecipeIds, setLikedRecipeIds] = useState([]);

  // Persistent instance id (per browser install)
  const [instanceId] = useState(() => {
    const stored = localStorage.getItem("instanceId");
    if (stored) return stored;
    const id = crypto.randomUUID();
    localStorage.setItem("instanceId", id);
    return id;
  });

  function clearToastSoon(message) {
    setToast(message);
    setTimeout(() => setToast(""), 3000);
  }

  // Cloud Run API calls (via /api -> proxy)

  async function callApi(path, options = {}) {
    if (!API_BASE_URL) {
      setError("API_BASE_URL is not configured");
      throw new Error("API_BASE_URL is not configured");
    }

    setError("");
    const res = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        "X-Client-Id": String(SERVICE_CLIENT_ID),
        "X-Instance-Id": instanceId,
        ...(options.headers || {}),
      },
    });

    const text = await res.text();
    let data;
    try {
      data = text ? JSON.parse(text) : null;
    } catch {
      data = text;
    }

    if (!res.ok) {
      throw new Error(
        typeof data === "string"
          ? data
          : data?.message || `Request failed with status ${res.status}`
      );
    }

    return data;
  }

  // Log to LOCAL Spring Boot via /client/log

  async function logEvent(event) {
    try {
      await fetch(`/client/log`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          instanceId,
          serviceClientId: SERVICE_CLIENT_ID,
          userId: userId || null,
          timestamp: new Date().toISOString(),
          ...event,
        }),
      });
    } catch (e) {
      console.warn("Failed to log event", e);
    }
  }

  // Sign in

  function handleSignIn(e) {
    e.preventDefault();
    const trimmed = String(userId).trim();
    if (!trimmed) {
      setError("Please enter a user id.");
      return;
    }

    const usersRaw = localStorage.getItem("demoUsers") || "{}";
    let users;
    try {
      users = JSON.parse(usersRaw);
    } catch {
      users = {};
    }
    users[trimmed] = {
      ...(users[trimmed] || {}),
      lastSeen: Date.now(),
    };
    localStorage.setItem("demoUsers", JSON.stringify(users));

    setUserId(trimmed);
    setSignedIn(true);
    setError("");
    clearToastSoon(`Signed in locally as ${trimmed}`);

    logEvent({
      type: "signin",
      event: "user_signed_in",
    });
  }

  // Load healthy recommendations
  async function loadHealthyRecipes() {
    if (!signedIn) {
      setError("Please enter a user id and press Sign In first.");
      return;
    }
    setLoading(true);
    setError("");
    setSelectedRecipe(null);
    setCalorieBreakdown(null);

    try {
      const data = await callApi(
        `/user/recommendHealthy?userId=${encodeURIComponent(
          SERVICE_CLIENT_ID
        )}&calorieMax=${encodeURIComponent(calorieMax)}`
      );

      const list = Array.isArray(data) ? data : data.recipes || [];
      const topSix = list.slice(0, 6);
      setRecipes(topSix);

      if (topSix.length === 0) {
        clearToastSoon("No healthy recipes found for this client and calorie limit.");
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // View Recipe Details

  async function openRecipeDetails(recipe) {
    if (!recipe) return;
    const recipeId = getRecipeId(recipe);
    if (recipeId == null) return;

    setSelectedRecipe(null);
    setCalorieBreakdown(null);
    setDetailLoading(true);
    setError("");

    try {
      const viewResponse = await callApi(
        `/recipe/viewRecipe?recipeId=${encodeURIComponent(recipeId)}`,
        { method: "POST" }
      );

      let fullRecipe = recipe;

      if (viewResponse && typeof viewResponse === "object") {
        if (viewResponse.recipe && typeof viewResponse.recipe === "object") {
          fullRecipe = { ...viewResponse.recipe, ...recipe };
        } else {
          fullRecipe = { ...viewResponse, ...recipe };
        }
      }

      setSelectedRecipe(fullRecipe);

      const breakdown = await callApi(
        `/recipe/calorieBreakdown?recipeId=${encodeURIComponent(recipeId)}`
      );

      if (breakdown && typeof breakdown === "object" && !Array.isArray(breakdown)) {
        setCalorieBreakdown(breakdown);
      } else {
        setCalorieBreakdown({ "Raw response": JSON.stringify(breakdown) });
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setDetailLoading(false);
    }
  }

  // --- Like recipe (CLOUD) + log (LOCAL) --------------------------------

  async function likeRecipe(recipe) {
    if (!signedIn) {
      setError("Sign in with a user id before liking recipes.");
      return;
    }
    if (!recipe) return;
    const recipeId = getRecipeId(recipe);
    if (recipeId == null) return;

    try {
      await callApi(
        `/user/likeRecipe?userId=${encodeURIComponent(
          SERVICE_CLIENT_ID
        )}&recipeId=${encodeURIComponent(recipeId)}`,
        { method: "POST" }
      );

      setLikedRecipeIds((prev) =>
        prev.includes(recipeId) ? prev : [...prev, recipeId]
      );

      const likesRaw = localStorage.getItem("userLikes") || "{}";
      let likesStore;
      try {
        likesStore = JSON.parse(likesRaw);
      } catch {
        likesStore = {};
      }
      const keyUser = userId || "anonymous";
      const userLikesSet = new Set(likesStore[keyUser] || []);
      userLikesSet.add(recipeId);
      likesStore[keyUser] = Array.from(userLikesSet);
      localStorage.setItem("userLikes", JSON.stringify(likesStore));

      logEvent({
        type: "like",
        event: "user_liked_recipe",
        recipeId,
        recipeTitle: getRecipeTitle(recipe),
      });

      clearToastSoon(`User ${keyUser} liked "${getRecipeTitle(recipe)}"`);
    } catch (err) {
      setError(err.message);
    }
  }

  function isRecipeLiked(recipe) {
    const recipeId = getRecipeId(recipe);
    if (recipeId == null) return false;
    return likedRecipeIds.includes(recipeId);
  }

  // Render Components

  return (
    <div className="app">
      <header className="app-header">
        <div>
          <h1>Healthy Recipe Client</h1>
          <p className="subtitle">
            Service client ID: <code>{SERVICE_CLIENT_ID}</code> · Instance ID:{" "}
            <code>{instanceId}</code>
          </p>
        </div>
      </header>

      <main className="app-main">
        {/* Left panel: local user + filters + recipe list */}
        <section className="left-panel">
          <div className="card">
            <h2>Local User</h2>
            <form onSubmit={handleSignIn} className="form-row">
              <label>
                User ID
                <input
                  type="text"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                  placeholder="e.g. alice01"
                />
              </label>
              <button type="submit">{signedIn ? "Change User" : "Sign In"}</button>
            </form>
            {signedIn && (
              <p className="info">
                Signed in as <strong>{userId}</strong>
              </p>
            )}
          </div>

          <div className="card">
            <h2>Healthy Recommendations</h2>
            <div className="form-row">
              <label>
                Max Calories
                <input
                  type="number"
                  value={calorieMax}
                  onChange={(e) => setCalorieMax(Number(e.target.value))}
                  min={0}
                />
              </label>
              <button type="button" onClick={loadHealthyRecipes} disabled={loading}>
                {loading ? "Loading..." : "Find Healthy Recipes"}
              </button>
            </div>
            <p className="hint">
              Uses <code>/user/recommendHealthy</code> with client ID{" "}
              <code>{SERVICE_CLIENT_ID}</code> to fetch up to six recipes under this
              calorie limit.
            </p>
          </div>

          <div className="card">
            <h2>Recommended Recipes</h2>
            {recipes.length === 0 && (
              <p className="muted">No recipes loaded yet. Try fetching healthy recipes.</p>
            )}
            <div className="recipe-grid">
              {recipes.map((recipe) => {
                const recipeId = getRecipeId(recipe);
                const title = getRecipeTitle(recipe);
                const liked = isRecipeLiked(recipe);

                return (
                  <div
                    key={recipeId ?? title}
                    className="recipe-card"
                  >
                    <h3>{title}</h3>
                    {recipe.category && (
                      <p className="tag">Category: {recipe.category}</p>
                    )}
                    {typeof recipe.totalCalories === "number" && (
                      <p className="muted">
                        Estimated total: <strong>{recipe.totalCalories}</strong> kcal
                      </p>
                    )}
                    <div className="card-actions">
                      <button
                        type="button"
                        onClick={() => openRecipeDetails(recipe)}
                      >
                        View Details
                      </button>
                      <button
                        type="button"
                        onClick={() => likeRecipe(recipe)}
                        disabled={liked}
                      >
                        {liked ? "Liked" : "Like"}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        {/* Right panel: recipe details */}
        <section className="right-panel">
          <div className="card">
            <h2>Recipe Details</h2>
            {!selectedRecipe && (
              <p className="muted">Select a recipe to see its ingredients and calories.</p>
            )}

            {selectedRecipe && (
              <>
                <h3>{getRecipeTitle(selectedRecipe)}</h3>
                {detailLoading && <p>Loading details...</p>}

                {calorieBreakdown && (
                  <>
                    <h4>Ingredient Calorie Breakdown</h4>
                    <table className="calorie-table">
                      <thead>
                        <tr>
                          <th>Ingredient</th>
                          <th>Estimated Calories</th>
                        </tr>
                      </thead>
                      <tbody>
                        {Object.entries(calorieBreakdown).map(([ingredient, cals]) => (
                          <tr key={ingredient}>
                            <td>{ingredient}</td>
                            <td>{cals}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </>
                )}

                {Array.isArray(selectedRecipe.ingredients) && (
                  <>
                    <h4>Ingredients</h4>
                    <ul>
                      {selectedRecipe.ingredients.map((ing, idx) => (
                        <li key={idx}>
                          {typeof ing === "string"
                            ? ing
                            : ing.foodName ||
                              ing.name ||
                              ing.ingredientName ||
                              JSON.stringify(ing)}
                        </li>
                      ))}
                    </ul>
                  </>
                )}

                {Array.isArray(selectedRecipe.steps) && (
                  <>
                    <h4>Steps</h4>
                    <ol>
                      {selectedRecipe.steps.map((step, idx) => (
                        <li key={idx}>{step}</li>
                      ))}
                    </ol>
                  </>
                )}
              </>
            )}
          </div>
        </section>
      </main>

      {(error || toast) && (
        <div className="message-bar">
          {error && <div className="error">{error}</div>}
          {toast && !error && <div className="toast">{toast}</div>}
        </div>
      )}
    </div>
  );
}

export default App;
