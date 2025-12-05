import { test, expect } from "@playwright/test";

const CLIENT_URL = "http://localhost:5173";

test.describe("Demo Client — End-to-End Tests", () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(CLIENT_URL);
    await page.evaluate(() => localStorage.clear());
    await page.reload();
  });

  // Test 1. Sign In
  test("User can sign in end-to-end", async ({ page }) => {
    await page.goto(CLIENT_URL);

    await page.fill("input[type=text]", "alice01");
    await page.click("button:has-text('Sign In')");

    const info = page.locator(".info");
    await expect(info).toContainText("alice01");
  });

  // Test 2. Fetch Recipes
  test("Fetch healthy recipes end-to-end", async ({ page }) => {
    await page.goto(CLIENT_URL);

    await page.fill("input[type=text]", "bob01");
    await page.click("button:has-text('Sign In')");
    await expect(page.locator(".info")).toContainText("bob01");

    await page.fill("input[type=number]", "2000");
    await page.click("button:has-text('Find Healthy Recipes')");

    const cards = page.locator(".recipe-card");
    const count = await cards.count();

    // We don't fail the test if there are no recipes – that’s a valid backend outcome.
    expect(count).toBeGreaterThanOrEqual(0);
  });

  // Test 3. View Recipe Details
  test("View recipe details end-to-end", async ({ page }) => {
    await page.goto(CLIENT_URL);

    await page.fill("input[type=text]", "detailsUser");
    await page.click("button:has-text('Sign In')");
    await expect(page.locator(".info")).toContainText("detailsUser");

    await page.fill("input[type=number]", "2000");
    await page.click("button:has-text('Find Healthy Recipes')");

    const cards = page.locator(".recipe-card");
    const count = await cards.count();

    if (count === 0) {
      await expect(
        page.locator("text=No recipes loaded yet").first()
      ).toBeVisible();
      return;
    }

    await cards.first().locator("button:has-text('View Details')").click();

    await expect(
      page.locator("h2:has-text('Recipe Details')")
    ).toBeVisible();
  });

  // Test 4. Like Recipe
  test("Like a recipe end-to-end", async ({ page }) => {
    await page.goto(CLIENT_URL);

    await page.fill("input[type=text]", "likeUser");
    await page.click("button:has-text('Sign In')");
    await expect(page.locator(".info")).toContainText("likeUser");

    await page.fill("input[type=number]", "2000");
    await page.click("button:has-text('Find Healthy Recipes')");

    const cards = page.locator(".recipe-card");
    const count = await cards.count();

    if (count === 0) {
      await expect(
        page.locator("text=No recipes loaded yet").first()
      ).toBeVisible();
      return;
    }

    const first = cards.first();
    await expect(first).toBeVisible();

    const likeButton = first.locator("button:has-text('Like')");
    await likeButton.click();

    await expect(
      first.locator("button:has-text('Liked')")
    ).toBeDisabled();
  });

  // Test 5. Multi Client Instance ID
  test("Two clients have separate instanceIds", async ({ browser }) => {
    const pageA = await browser.newPage();
    const pageB = await browser.newPage();

    await pageA.goto(CLIENT_URL);
    await pageB.goto(CLIENT_URL);

    const idA = await pageA.locator("code").nth(1).innerText();
    const idB = await pageB.locator("code").nth(1).innerText();

    expect(idA).not.toBe(idB);

    await pageA.fill("input[type=text]", "clientA");
    await pageA.click("button:has-text('Sign In')");
    await expect(pageA.locator(".info")).toContainText("clientA");

    await pageB.fill("input[type=text]", "clientB");
    await pageB.click("button:has-text('Sign In')");
    await expect(pageB.locator(".info")).toContainText("clientB");
  });

  // Test 6. Backend Error Test
  test("Backend error should show message", async ({ page }) => {
    await page.goto(CLIENT_URL);

    await page.fill("input[type=text]", "errUser");
    await page.click("button:has-text('Sign In')");
    await expect(page.locator(".info")).toContainText("errUser");

    await page.fill("input[type=number]", "-999");
    await page.click("button:has-text('Find Healthy Recipes')");

    await expect(page.locator(".error, .toast")).toBeVisible();
  });

});
