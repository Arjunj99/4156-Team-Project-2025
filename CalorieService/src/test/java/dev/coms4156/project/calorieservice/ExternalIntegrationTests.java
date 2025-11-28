package dev.coms4156.project.calorieservice;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * External integration tests that verify our code works with an
 * out-of-process dependency. These tests require a Firestore Emulator.
 *
 * <p>Skips automatically when FIRESTORE_EMULATOR_HOST is not set.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ExternalIntegrationTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FirestoreService firestoreService;

  private boolean emulatorReady;

  @BeforeEach
  void assumeEmulator() {
    emulatorReady = System.getenv("FIRESTORE_EMULATOR_HOST") != null;
    assumeTrue(emulatorReady, "Firestore emulator not configured; skipping external tests");
  }

  @AfterEach
  void cleanup() throws Exception {
    if (!emulatorReady) {
      return;
    }
    try {
      firestoreService.deleteRecipe(99001);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteUser(99100);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99101);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99102);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99103);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99200);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteFood(99300);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteUser(99301);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteFood(99400);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteFood(99401);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteFood(99402);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99500);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99501);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99502);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99503);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteUser(99600);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99601);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99602);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99603);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99604);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteRecipe(99620);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteFood(99630);
    } catch (Exception ignored) {
      ignored.toString();
    }
    try {
      firestoreService.deleteFood(99640);
    } catch (Exception ignored) {
      ignored.toString();
    }
  }

  /**
   * Adds a recipe via the API and verifies totals, then confirms it persisted
   * by reading it back through FirestoreService.
   */
  @Test
  @DisplayName("External: add recipe JSON persists and totals compute")
  void externalAddRecipePersistsAndComputes() throws Exception {
    final int recipeId = 99001;
    String payload = String.format("""
        {
          "recipeName": "Ext Recipe",
          "recipeId": %d,
          "category": "Lunch",
          "ingredients": [
            { "foodName": "A", "foodId": %d, "calories": 120, "category": "T" },
            { "foodName": "B", "foodId": %d, "calories": 80,  "category": "T" }
          ],
          "views": 0,
          "likes": 0
        }
        """, recipeId, recipeId + 1, recipeId + 2);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.recipeId").value(recipeId));

    mockMvc.perform(get("/recipe/totalCalorie")
            .param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCalories").value(200));

    Recipe stored = firestoreService.getRecipeById(recipeId);
    org.junit.jupiter.api.Assertions.assertNotNull(stored);
    org.junit.jupiter.api.Assertions.assertEquals(200, stored.getTotalCalories());
  }

  /**
   * Updates a recipe via API and confirms the change persisted in the emulator.
   */
  @Test
  @DisplayName("External: view/like updates persist")
  void externalViewLikePersist() throws Exception {
    final int rid = 99200;
    Recipe r = new Recipe("U", rid, "Any", new ArrayList<>(), 0, 0, 100);
    firestoreService.addRecipe(r);
    Recipe before = firestoreService.getRecipeById(rid);
    final int v0 = before.getViews();
    final int l0 = before.getLikes();

    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", String.valueOf(rid)))
        .andExpect(status().isOk());
    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", String.valueOf(rid)))
        .andExpect(status().isOk());

    Recipe after = firestoreService.getRecipeById(rid);
    org.junit.jupiter.api.Assertions.assertEquals(v0 + 1, after.getViews());
    org.junit.jupiter.api.Assertions.assertEquals(l0 + 1, after.getLikes());
  }

  /**
   * Deletes a recipe and verifies it is no longer retrievable.
   */
  @Test
  @DisplayName("External: delete removes data")
  void externalDeleteRemovesData() throws Exception {
    final int rid = 99210;
    firestoreService.addRecipe(new Recipe("D", rid, "Any", new ArrayList<>(), 0, 0, 50));
    Recipe exists = firestoreService.getRecipeById(rid);
    org.junit.jupiter.api.Assertions.assertNotNull(exists);
    firestoreService.deleteRecipe(rid);
    Recipe missing = firestoreService.getRecipeById(rid);
    org.junit.jupiter.api.Assertions.assertNull(missing);
  }

  /**
   * Attempts to read a non-existent recipe; verifies null (absence) is returned.
   */
  @Test
  @DisplayName("External: get non-existent returns null")
  void externalGetNonExistent() throws Exception {
    Recipe missing = firestoreService.getRecipeById(999991);
    org.junit.jupiter.api.Assertions.assertNull(missing);
  }

  /**
   * Seeds user and recipes, likes via API, and verifies recommend and
   * recommendHealthy responses exclude liked and honor cap.
   */
  @Test
  @DisplayName("External: user like + recommend and healthy")
  void externalUserLikeAndRecommendFlows() throws Exception {
    final int uid = 99600;
    firestoreService.addUser(new User("u", uid));
    firestoreService.addRecipe(new Recipe("L", 99601, "ExtC", new ArrayList<>(), 0, 0, 10));
    firestoreService.addRecipe(new Recipe("E", 99602, "ExtC", new ArrayList<>(), 0, 0, 230));
    firestoreService.addRecipe(new Recipe("O", 99603, "ExtC", new ArrayList<>(), 0, 0, 231));
    firestoreService.addRecipe(new Recipe("X", 99604, "ExtC", new ArrayList<>(), 0, 0, 100));

    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", String.valueOf(uid))
            .param("recipeId", "99601"))
        .andExpect(status().isOk());

    User after = firestoreService.getUserById(uid);
    org.junit.jupiter.api.Assertions.assertTrue(
        after.getLikedRecipes().stream().anyMatch(r -> r.getRecipeId() == 99601));
    org.junit.jupiter.api.Assertions.assertTrue(
        firestoreService.getRecipeById(99601).getLikes() > 0);

    mockMvc.perform(get("/user/recommend").param("userId", String.valueOf(uid)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(99601))));

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", String.valueOf(uid))
            .param("calorieMax", "230"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].recipeId").value(hasItem(99602)))
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(99603))))
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(99601))));
  }

  /**
   * Adds a recipe via API and verifies calorie breakdown via API.
   */
  @Test
  @DisplayName("External: calorieBreakdown via API")
  void externalCalorieBreakdownViaApi() throws Exception {
    final int rid = 99620;
    String payload = String.format("""
        {
          "recipeName": "B",
          "recipeId": %d,
          "category": "Any",
          "ingredients": [
            { "foodName": "A", "foodId": %d, "calories": 100, "category": "T" },
            { "foodName": "B", "foodId": %d, "calories": 80,  "category": "T" }
          ],
          "views": 0,
          "likes": 0
        }
        """, rid, rid + 1, rid + 2);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", String.valueOf(rid)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.A").value(100))
        .andExpect(jsonPath("$.B").value(80));
  }

  /**
   * Adds food via API and verifies persistence via FirestoreService.
   */
  @Test
  @DisplayName("External: add food via API persists")
  void externalFoodAddApiPersists() throws Exception {
    final int fid = 99630;
    String payload = String.format("""
        {
          "foodName": "EF",
          "foodId": %d,
          "calories": 210,
          "category": "Snack"
        }
        """, fid);

    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk());

    Food fetched = firestoreService.getFoodById(fid);
    org.junit.jupiter.api.Assertions.assertNotNull(fetched);
    org.junit.jupiter.api.Assertions.assertEquals(210, fetched.getCalories());
  }

  /**
   * Verifies API 404/200 edge behavior against emulator-backed lookups.
   */
  @Test
  @DisplayName("External: negatives for missing/nomatch")
  void externalNegatives() throws Exception {
    mockMvc.perform(get("/recipe/alternative").param("recipeId", "999998"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));

    final int fid = 99640;
    firestoreService.addFood(new Food("Only", fid, 300, "OnlyC"));
    String expected = String.format(
        "No lower calorie alternatives found for food ID %d.", fid);

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(fid)))
        .andExpect(status().isOk())
        .andExpect(content().string(expected));
  }

  /**
   * Seeds foods in the emulator and verifies /food/alternative uses the emulator
   * data (same category, lower calories, excludes target).
   */
  @Test
  @DisplayName("External: /food/alternative queries emulator data")
  void externalFoodAlternativeTopDown() throws Exception {
    final int targetId = 99400;
    firestoreService.addFood(new Food("Target", targetId, 500, "Cap"));
    firestoreService.addFood(new Food("C1", 99401, 100, "Cap"));
    firestoreService.addFood(new Food("C2", 99402, 110, "Cap"));

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(targetId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].foodId").value(hasItem(99401)))
        .andExpect(jsonPath("$[*].foodId").value(hasItem(99402)))
        .andExpect(jsonPath("$[*].foodId").value(not(hasItem(targetId))));
  }

  /**
   * Seeds recipes in the emulator and verifies /recipe/alternative derives
   * top/random alternatives using emulator data (base excluded).
   */
  @Test
  @DisplayName("External: /recipe/alternative queries emulator data")
  void externalRecipeAlternativeTopDown() throws Exception {
    final int baseId = 99500;
    ArrayList<Food> baseIng = new ArrayList<>();
    baseIng.add(new Food("FB", baseId * 10 + 1, 500, "Alt"));
    firestoreService.addRecipe(new Recipe("Base", baseId, "Alt", baseIng, 10, 0, 0));

    ArrayList<Food> r1Ing = new ArrayList<>();
    r1Ing.add(new Food("F1", 99501 * 10 + 1, 300, "Alt"));
    firestoreService.addRecipe(new Recipe("R1", 99501, "Alt", r1Ing, 200, 0, 0));

    ArrayList<Food> r2Ing = new ArrayList<>();
    r2Ing.add(new Food("F2", 99502 * 10 + 1, 250, "Alt"));
    firestoreService.addRecipe(new Recipe("R2", 99502, "Alt", r2Ing, 100, 0, 0));

    ArrayList<Food> r3Ing = new ArrayList<>();
    r3Ing.add(new Food("F3", 99503 * 10 + 1, 200, "Alt"));
    firestoreService.addRecipe(new Recipe("R3", 99503, "Alt", r3Ing, 50, 0, 0));

    mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(baseId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives[*].recipeId").value(hasItem(99501)))
        .andExpect(jsonPath("$.topAlternatives[*].recipeId").value(hasItem(99502)))
        .andExpect(jsonPath("$.topAlternatives[*].recipeId").value(hasItem(99503)))
        .andExpect(jsonPath("$.topAlternatives[*].recipeId").value(not(hasItem(baseId))))
        .andExpect(jsonPath("$.randomAlternatives[*].recipeId").value(not(hasItem(baseId))));
  }

  /**
   * Verifies Food add/get/delete against the emulator using the real client.
   */
  @Test
  @DisplayName("External: food add/get/delete persists")
  void externalFoodCrudPersists() throws Exception {
    final int fid = 99300;
    Food food = new Food("Ext Food", fid, 123, "Snack");
    boolean added = firestoreService.addFood(food);
    org.junit.jupiter.api.Assertions.assertTrue(added);

    Food fetched = firestoreService.getFoodById(fid);
    org.junit.jupiter.api.Assertions.assertNotNull(fetched);
    org.junit.jupiter.api.Assertions.assertEquals(123, fetched.getCalories());
    org.junit.jupiter.api.Assertions.assertEquals("Snack", fetched.getCategory());

    boolean deleted = firestoreService.deleteFood(fid);
    org.junit.jupiter.api.Assertions.assertTrue(deleted);
    org.junit.jupiter.api.Assertions.assertNull(firestoreService.getFoodById(fid));
  }

  /**
   * Verifies User add/get/delete against the emulator using the real client.
   */
  @Test
  @DisplayName("External: user add/get/delete persists")
  void externalUserCrudPersists() throws Exception {
    final int uid = 99301;
    User user = new User("ext-user", uid);
    boolean added = firestoreService.addUser(user);
    org.junit.jupiter.api.Assertions.assertTrue(added);

    User fetched = firestoreService.getUserById(uid);
    org.junit.jupiter.api.Assertions.assertNotNull(fetched);
    org.junit.jupiter.api.Assertions.assertEquals("ext-user", fetched.getUsername());

    boolean deleted = firestoreService.deleteUser(uid);
    org.junit.jupiter.api.Assertions.assertTrue(deleted);
    org.junit.jupiter.api.Assertions.assertNull(firestoreService.getUserById(uid));
  }

  /**
   * Seeds user/recipes in the emulator, likes one via API, and verifies the
   * healthy recommendation includes an equal-cap recipe but excludes over-cap
   * and the liked recipe.
   */
  @Test
  @DisplayName("External: like then recommendHealthy honors cap and excludes liked")
  void externalLikeThenRecommendHealthy() throws Exception {
    final int uid = 99100;
    User user = new User("ext", uid);
    firestoreService.addUser(user);

    Recipe liked = new Recipe("L", 99101, "Edge", new ArrayList<>(), 0, 0, 10);
    Recipe equalCap = new Recipe("E", 99102, "Edge", new ArrayList<>(), 0, 0, 230);
    Recipe overCap = new Recipe("O", 99103, "Edge", new ArrayList<>(), 0, 0, 231);
    firestoreService.addRecipe(liked);
    firestoreService.addRecipe(equalCap);
    firestoreService.addRecipe(overCap);

    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", String.valueOf(uid))
            .param("recipeId", String.valueOf(liked.getRecipeId())))
        .andExpect(status().isOk());

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", String.valueOf(uid))
            .param("calorieMax", "230"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].recipeId").value(hasItem(99102)))
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(99103))))
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(99101))));
  }
}
