package dev.coms4156.project.calorieservice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Internal integration tests that boot the full Spring context while replacing
 * the persistence layer with an in-memory implementation.
 */
@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "logging.file.name=target/internal-int.log"
})
@AutoConfigureMockMvc
@Import(InternalIntegrationTests.TestConfig.class)
public class InternalIntegrationTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InMemoryFirestoreService store;

  @Autowired
  private MockApiService mockApiService;

  @BeforeEach
  void reset() {
    store.reset();
  }

  @Test
  @DisplayName("Add recipe JSON then compute totals and breakdown")
  void addRecipeThenCompute() throws Exception {
    final int recipeId = 7020;
    String payload = String.format("""
        {
          "recipeName": "Ctx Bowl",
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

    mockMvc.perform(get("/recipe/calorieBreakdown")
            .param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.A").value(120))
        .andExpect(jsonPath("$.B").value(80));
  }

  @Test
  @DisplayName("Food alternatives integrate store->service->controller")
  void foodAlternativesFlow() throws Exception {
    store.addFood(new Food("Banana", 1, 105, "Fruit"));
    store.addFood(new Food("Apple", 2, 95, "Fruit"));
    store.addFood(new Food("Strawberries", 3, 49, "Fruit"));

    mockMvc.perform(get("/food/alternative").param("foodId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].foodId").value(not(hasItem(1))));
  }

  @Test
  @DisplayName(
      "Recipe alternatives return topAlternatives=3 and randomAlternatives<=3, base excluded")
  void recipeAlternativesFlow() throws Exception {
    final int baseId = 800;
    store.addRecipe(buildRecipe(baseId, "Dinner", new int[] {200, 200, 250}, 10));
    Recipe r1 = buildRecipe(801, "Dinner", new int[] {150, 200}, 200);
    Recipe r2 = buildRecipe(802, "Dinner", new int[] {180, 150}, 100);
    Recipe r3 = buildRecipe(803, "Dinner", new int[] {120, 230}, 50);
    Recipe r4 = buildRecipe(804, "Dinner", new int[] {100, 120}, 25);
    store.addRecipe(r1);
    store.addRecipe(r2);
    store.addRecipe(r3);
    store.addRecipe(r4);

    mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(baseId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives", hasSize(3)))
        .andExpect(jsonPath("$.randomAlternatives").isArray())
        .andExpect(jsonPath("$.randomAlternatives.length()")
            .value(lessThanOrEqualTo(3)))
        .andExpect(jsonPath("$.topAlternatives[*].recipeId")
            .value(not(hasItem(baseId))))
        .andExpect(jsonPath("$.randomAlternatives[*].recipeId")
            .value(not(hasItem(baseId))));
  }

  @Test
  @DisplayName("Total calorie and breakdown reflect ingredients")
  void calorieComputationsFlow() throws Exception {
    ArrayList<Food> ing = new ArrayList<>();
    ing.add(new Food("Chicken", 11, 165, "Protein"));
    ing.add(new Food("Rice", 12, 216, "Grain"));
    ing.add(new Food("Broccoli", 13, 55, "Vegetable"));
    final int id = 810;
    store.addRecipe(new Recipe("Bowl", id, "Dinner", ing, 0, 0, 0));

    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", String.valueOf(id)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recipeId").value(id))
        .andExpect(jsonPath("$.totalCalories").value(165 + 216 + 55));

    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", String.valueOf(id)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.Chicken").value(165))
        .andExpect(jsonPath("$.Rice").value(216))
        .andExpect(jsonPath("$.Broccoli").value(55));
  }

  @Test
  @DisplayName("Like then recommendHealthy honors calorie cap and excludes liked")
  void likeThenRecommendHealthy() throws Exception {
    final int uid = 7030;
    store.addUser(new User("ctx", uid));

    Recipe liked = buildRecipe(7031, "Edge", new int[] {10}, 0);
    Recipe equalCap = buildRecipe(7032, "Edge", new int[] {230}, 0);
    Recipe overCap = buildRecipe(7033, "Edge", new int[] {231}, 0);
    store.addRecipe(liked);
    store.addRecipe(equalCap);
    store.addRecipe(overCap);

    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", String.valueOf(uid))
            .param("recipeId", "7031"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", String.valueOf(uid))
            .param("calorieMax", "230"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].recipeId").value(hasItem(7032)))
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(7033))))
        .andExpect(jsonPath("$[*].recipeId").value(not(hasItem(7031))));
  }

  @Test
  @DisplayName("Recommend excludes liked and recommendHealthy respects cap")
  void recommendationFlows() throws Exception {
    Recipe dinner1 = buildRecipe(7201, "Dinner", new int[] {150, 200}, 0);
    Recipe dinner2 = buildRecipe(7202, "Dinner", new int[] {120, 120}, 0);
    Recipe snack1 = buildRecipe(7203, "Snack", new int[] {90, 80}, 0);
    store.addRecipe(dinner1);
    store.addRecipe(dinner2);
    store.addRecipe(snack1);

    final int uid = 7200;
    store.addUser(new User("alice", uid));
    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", String.valueOf(uid))
            .param("recipeId", String.valueOf(dinner1.getRecipeId())))
        .andExpect(status().isOk());

    mockMvc.perform(get("/user/recommend").param("userId", String.valueOf(uid)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].recipeId")
            .value(not(hasItem(dinner1.getRecipeId()))));

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", String.valueOf(uid))
            .param("calorieMax", "230"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].totalCalories")
            .value(not(hasItem(org.hamcrest.Matchers.greaterThan(230)))));
  }

  @Test
  @DisplayName("View and like mutations persist through service and store")
  void viewAndLikeMutations() throws Exception {
    int id = 7400;
    store.addRecipe(buildRecipe(id, "Dinner", new int[] {100, 120}, 0));
    final int initialViews = mockApiService.getRecipeById(id).orElseThrow().getViews();
    final int initialLikes = mockApiService.getRecipeById(id).orElseThrow().getLikes();

    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", String.valueOf(id)))
        .andExpect(status().isOk());
    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", String.valueOf(id)))
        .andExpect(status().isOk());

    int updatedViews = mockApiService.getRecipeById(id).orElseThrow().getViews();
    int updatedLikes = mockApiService.getRecipeById(id).orElseThrow().getLikes();
    org.junit.jupiter.api.Assertions.assertEquals(initialViews + 1, updatedViews);
    org.junit.jupiter.api.Assertions.assertEquals(initialLikes + 1, updatedLikes);
  }

  @Test
  @DisplayName(
      "JSON addFood then query alternatives excludes the target "
          + "and returns lower-calorie peers")
  void jsonAddFoodThenAlternativesFlow() throws Exception {
    int targetId = 7500;
    String target = String.format("""
        {
          "foodName": "Target Snack",
          "foodId": %d,
          "calories": 300,
          "category": "Snack"
        }
        """, targetId);
    String alt1 = String.format("""
        {
          "foodName": "Alt1",
          "foodId": %d,
          "calories": 150,
          "category": "Snack"
        }
        """, targetId + 1);
    String alt2 = String.format("""
        {
          "foodName": "Alt2",
          "foodId": %d,
          "calories": 100,
          "category": "Snack"
        }
        """, targetId + 2);

    mockMvc.perform(post("/food/addFood").contentType(MediaType.APPLICATION_JSON).content(target))
        .andExpect(status().isOk());
    mockMvc.perform(post("/food/addFood").contentType(MediaType.APPLICATION_JSON).content(alt1))
        .andExpect(status().isOk());
    mockMvc.perform(post("/food/addFood").contentType(MediaType.APPLICATION_JSON).content(alt2))
        .andExpect(status().isOk());

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(targetId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].foodId").value(not(hasItem(targetId))));
  }

  @Test
  @DisplayName("Food alternatives cap at five when more than five matches exist")
  void foodAlternativesCapAtFive() throws Exception {
    int targetId = 7600;
    store.addFood(new Food("Target", targetId, 500, "Cap"));
    store.addFood(new Food("C1", targetId + 1, 100, "Cap"));
    store.addFood(new Food("C2", targetId + 2, 110, "Cap"));
    store.addFood(new Food("C3", targetId + 3, 120, "Cap"));
    store.addFood(new Food("C4", targetId + 4, 130, "Cap"));
    store.addFood(new Food("C5", targetId + 5, 140, "Cap"));
    store.addFood(new Food("C6", targetId + 6, 150, "Cap"));

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(targetId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(5))
        .andExpect(jsonPath("$[*].foodId").value(not(hasItem(targetId))));
  }

  @Test
  @DisplayName("Index endpoint responds with welcome text")
  void indexEndpointRedundancyCheck() throws Exception {
    mockMvc.perform(get("/index"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Welcome to the home page")));
  }

  @Test
  @DisplayName("Root index endpoint responds with welcome text")
  void indexRootEndpointRespondsWithWelcome() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Welcome to the home page")));
  }

  @Test
  @DisplayName("Ingredientless recipe yields empty breakdown via REST")
  void ingredientlessRecipeEmptyBreakdown() throws Exception {
    final int rid = 7701;
    String payload = String.format("""
        {
          "recipeName": "Empty",
          "recipeId": %d,
          "category": "Test",
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """, rid);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", String.valueOf(rid)))
        .andExpect(status().isOk())
        .andExpect(content().json("{}"));
  }

  @Test
  @DisplayName("Food alternatives exclude equal-calorie items (strict < filter)")
  void foodAlternativesExcludeEqualCalories() throws Exception {
    int base = 7710;
    store.addFood(new Food("Target", base, 100, "EqCat"));
    store.addFood(new Food("EqualPeer", base + 1, 100, "EqCat"));
    store.addFood(new Food("LowerPeer", base + 2, 90, "EqCat"));

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(base)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].foodId").value(base + 2))
        .andExpect(jsonPath("$[*].foodId").value(not(hasItem(base))))
        .andExpect(jsonPath("$[*].foodId").value(not(hasItem(base + 1))));
  }

  @Test
  @DisplayName("Recipe alternatives exclude equal-calorie candidates (strict < base)")
  void recipeAlternativesExcludeEqualCalories() throws Exception {
    final int baseId = 88100;
    store.addRecipe(buildRecipe(baseId, "EqDinner", new int[] {200, 200}, 5));
    store.addRecipe(buildRecipe(baseId + 1, "EqDinner", new int[] {150, 250}, 100));
    store.addRecipe(buildRecipe(baseId + 2, "EqDinner", new int[] {120, 100}, 10));

    mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(baseId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives[*].recipeId")
            .value(not(hasItem(baseId + 1))))
        .andExpect(jsonPath("$.randomAlternatives[*].recipeId")
            .value(not(hasItem(baseId + 1))));
  }

  @Test
  @DisplayName("File logging writes expected endpoint line to configured log file")
  void fileLoggingWritesExpectedLine() throws Exception {
    mockMvc.perform(get("/index")).andExpect(status().isOk());

    java.nio.file.Path primary = java.nio.file.Paths.get("target", "internal-int.log");
    java.nio.file.Path fallback = java.nio.file.Paths.get("logs", "api-calls.log");
    long deadline = System.currentTimeMillis() + 5000;
    boolean found = false;
    while (System.currentTimeMillis() < deadline) {
      if (java.nio.file.Files.exists(primary)) {
        String content = java.nio.file.Files.readString(primary);
        if (content.contains("endpoint called: GET /index")) {
          found = true;
          break;
        }
      }
      if (!found && java.nio.file.Files.exists(fallback)) {
        String content2 = java.nio.file.Files.readString(fallback);
        if (content2.contains("endpoint called: GET /index")) {
          found = true;
          break;
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    org.junit.jupiter.api.Assertions.assertTrue(found,
        "Expected log line not found in files: " + primary + " or " + fallback);
  }

  @Test
  @DisplayName("Food alternatives returns message when no matches exist")
  void foodAlternativesNoMatchesMessage() throws Exception {
    final int fid = 7800;
    store.addFood(new Food("Solo", fid, 200, "UniqueCat"));
    String expected = String.format(
        "No lower calorie alternatives found for food ID %d.", fid);

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(fid)))
        .andExpect(status().isOk())
        .andExpect(content().string(expected));
  }

  @Test
  @DisplayName("Recipe alternatives return empty arrays when no alternatives exist")
  void recipeAlternativesEmptyArrays() throws Exception {
    final int base = 7900;
    store.addRecipe(buildRecipe(base, "SoloCat", new int[] {500}, 0));

    mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(base)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives", hasSize(0)))
        .andExpect(jsonPath("$.randomAlternatives", hasSize(0)));
  }

  @Test
  @DisplayName("Recipe alternatives topAlternatives sorted by views desc")
  void recipeAlternativesTopSortedByViews() throws Exception {
    final int base = 7905;
    store.addRecipe(buildRecipe(base, "SortCat", new int[] {200, 200}, 0));
    store.addRecipe(buildRecipe(base + 1, "SortCat", new int[] {100, 120}, 10));
    store.addRecipe(buildRecipe(base + 2, "SortCat", new int[] {150, 120}, 90));
    store.addRecipe(buildRecipe(base + 3, "SortCat", new int[] {120, 120}, 50));
    store.addRecipe(buildRecipe(base + 4, "SortCat", new int[] {100, 100}, 70));

    org.springframework.test.web.servlet.MvcResult mvcResult =
        mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(base)))
        .andExpect(status().isOk())
        .andReturn();

    String json = mvcResult.getResponse().getContentAsString();
    Map<String, List<Map<String, Object>>> body = new ObjectMapper().readValue(
        json, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
    List<Map<String, Object>> top = body.get("topAlternatives");
    org.junit.jupiter.api.Assertions.assertEquals(3, top.size());
    int v0 = ((Number) top.get(0).get("views")).intValue();
    int v1 = ((Number) top.get(1).get("views")).intValue();
    int v2 = ((Number) top.get(2).get("views")).intValue();
    org.junit.jupiter.api.Assertions.assertTrue(v0 >= v1 && v1 >= v2,
        "topAlternatives not sorted desc by views: " + v0 + "," + v1 + "," + v2);
  }

  @Test
  @DisplayName("Recipe alternatives groups are disjoint (no overlap)")
  void recipeAlternativesGroupsDisjoint() throws Exception {
    final int base = 7906;
    store.addRecipe(buildRecipe(base, "DisjointCat", new int[] {300, 120}, 0));
    for (int i = 1; i <= 6; i++) {
      store.addRecipe(buildRecipe(base + i, "DisjointCat", new int[] {100, 100}, i * 10));
    }

    org.springframework.test.web.servlet.MvcResult mvcResult =
        mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(base)))
        .andExpect(status().isOk())
        .andReturn();

    String json = mvcResult.getResponse().getContentAsString();
    Map<String, List<Map<String, Object>>> body = new ObjectMapper().readValue(
        json, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
    List<Map<String, Object>> top = body.get("topAlternatives");
    List<Map<String, Object>> rnd = body.get("randomAlternatives");
    java.util.Set<Integer> topIds = top.stream()
        .map(m -> ((Number) m.get("recipeId")).intValue())
        .collect(java.util.stream.Collectors.toSet());
    java.util.Set<Integer> rndIds = rnd.stream()
        .map(m -> ((Number) m.get("recipeId")).intValue())
        .collect(java.util.stream.Collectors.toSet());
    topIds.retainAll(rndIds);
    assertTrue(topIds.isEmpty(), "Overlap between groups detected");
  }

  @Test
  @DisplayName(
      "User with no likes: recommend 404, recommendHealthy returns under-cap list")
  void userNoLikesPaths() throws Exception {
    final int uid = 7950;
    store.addUser(new User("nolikes", uid));
    store.addRecipe(buildRecipe(7951, "Any", new int[] {90}, 0));

    mockMvc.perform(get("/user/recommend").param("userId", String.valueOf(uid)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            String.format("User with ID %d not found.", uid)));

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", String.valueOf(uid))
            .param("calorieMax", "120"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].totalCalories").value(90));
  }

  @Test
  @DisplayName("Add recipe duplicate id returns 409 with message")
  void addRecipeDuplicateIdReturns409() throws Exception {
    final int rid = 8801;
    String payload = String.format("""
        {
          "recipeName": "Dup",
          "recipeId": %d,
          "category": "Snack",
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """, rid);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Recipe with id already exists"))
        .andExpect(jsonPath("$.recipeId").value(rid));
  }

  @Test
  @DisplayName("Add recipe malformed JSON returns 400")
  void addRecipeMalformedJsonReturns400() throws Exception {
    String badJson = "{ \"recipeId\": }";
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(badJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Add food duplicate id returns 400 with message")
  void addFoodDuplicateIdReturns400() throws Exception {
    final int fid = 8850;
    String payload = String.format("""
        {
          "foodName": "Bar",
          "foodId": %d,
          "calories": 100,
          "category": "Snack"
        }
        """, fid);

    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk());

    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            String.format("Food with ID %d already exists or is invalid.", fid)));
  }

  @Test
  @DisplayName("Food alternative 404 when food not found")
  void foodAlternativeNotFoundReturns404() throws Exception {
    final int fid = 999999;
    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(fid)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            String.format("Food with ID %d not found.", fid)));
  }

  @Test
  @DisplayName("Recipe alternative 404 when base recipe missing")
  void recipeAlternativeNotFoundReturns404() throws Exception {
    mockMvc.perform(get("/recipe/alternative").param("recipeId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  @DisplayName("Total calorie returns 404 when recipe missing")
  void totalCalorieNotFoundReturns404() throws Exception {
    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  @DisplayName("Calorie breakdown returns 404 when recipe missing")
  void calorieBreakdownNotFoundReturns404() throws Exception {
    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  @DisplayName("View recipe returns 404 when recipe missing")
  void viewRecipeNotFoundReturns404() throws Exception {
    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  @DisplayName("Like recipe returns 404 when recipe missing")
  void likeRecipeNotFoundReturns404() throws Exception {
    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  @DisplayName("Recommend endpoints return 404 when user missing")
  void recommendMissingUserReturns404() throws Exception {
    final int uid = 999999;
    mockMvc.perform(get("/user/recommend").param("userId", String.valueOf(uid)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            String.format("User with ID %d not found.", uid)));

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", String.valueOf(uid))
            .param("calorieMax", "230"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(
            String.format("User with ID %d not found.", uid)));
  }

  @Test
  @DisplayName("Add recipe allows null category")
  void addRecipeAllowsNullCategory() throws Exception {
    final int rid = 8920;
    String payload = String.format("""
        {
          "recipeName": "NoCat",
          "recipeId": %d,
          "category": null,
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """, rid);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.recipeId").value(rid));
  }

  private Recipe buildRecipe(int id, String category, int[] ingredientCalories, int views) {
    ArrayList<Food> ings = new ArrayList<>();
    Deque<Integer> ids = new ArrayDeque<>();
    for (int i = 0; i < ingredientCalories.length; i++) {
      ids.add(id * 1000 + i + 1);
    }
    for (int cal : ingredientCalories) {
      ings.add(new Food("I" + cal, ids.removeFirst(), cal, "T"));
    }
    return new Recipe("R" + id, id, category, ings, views, 0,
        java.util.Arrays.stream(ingredientCalories).sum());
  }

  /**
   * Test configuration that overrides FirestoreService with an in-memory
   * implementation to keep tests internal.
   */
  @TestConfiguration
  static class TestConfig {
    @Bean(name = "firestoreService")
    @Primary
    InMemoryFirestoreService firestoreService() {
      return new InMemoryFirestoreService();
    }
  }

  /**
   * In-memory replacement for FirestoreService used in full-context tests.
   */
  static class InMemoryFirestoreService extends FirestoreService {
    private final Map<Integer, Food> foods = new ConcurrentHashMap<>();
    private final Map<Integer, Recipe> recipes = new ConcurrentHashMap<>();
    private final Map<Integer, User> users = new ConcurrentHashMap<>();

    void reset() {
      foods.clear();
      recipes.clear();
      users.clear();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public ArrayList<Food> getAllFoods() {
      return new ArrayList<>(foods.values());
    }

    @Override
    public Food getFoodById(int foodId) {
      return foods.get(foodId);
    }

    @Override
    public boolean addFood(Food food) {
      if (food == null) {
        return false;
      }
      return foods.putIfAbsent(food.getFoodId(), food) == null;
    }

    @Override
    public List<Food> getFoodsByCategoryAndCalories(String category, int maxCalories) {
      return foods.values().stream()
          .filter(f -> category == null
              ? f.getCategory() == null
              : category.equals(f.getCategory()))
          .filter(f -> f.getCalories() < maxCalories)
          .collect(Collectors.toList());
    }

    @Override
    public ArrayList<Recipe> getAllRecipes() {
      return new ArrayList<>(recipes.values());
    }

    @Override
    public Recipe getRecipeById(int recipeId) {
      return recipes.get(recipeId);
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
      if (recipe == null) {
        return false;
      }
      recipe.setTotalCalories(recipe.getTotalCalories());
      return recipes.putIfAbsent(recipe.getRecipeId(), recipe) == null;
    }

    @Override
    public boolean updateRecipe(Recipe recipe) {
      if (recipe == null) {
        return false;
      }
      recipes.put(recipe.getRecipeId(), recipe);
      return true;
    }

    @Override
    public List<Recipe> getRecipesByCategoryAndCalories(String category, int maxCalories) {
      return recipes.values().stream()
          .filter(r -> category == null
              ? r.getCategory() == null
              : category.equals(r.getCategory()))
          .filter(r -> r.getTotalCalories() <= maxCalories)
          .collect(Collectors.toList());
    }

    @Override
    public List<Recipe> getRecipesByCalories(int maxCalories) {
      return recipes.values().stream()
          .filter(r -> r.getTotalCalories() <= maxCalories)
          .collect(Collectors.toList());
    }

    @Override
    public ArrayList<User> getAllUsers() {
      return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int userId) {
      return users.get(userId);
    }

    @Override
    public boolean addUser(User user) {
      if (user == null) {
        return false;
      }
      user.setLikedRecipes(user.getLikedRecipes());
      return users.putIfAbsent(user.getUserId(), user) == null;
    }

    @Override
    public boolean updateUser(User user) {
      if (user == null) {
        return false;
      }
      users.put(user.getUserId(), user);
      return true;
    }

    @Override
    public boolean deleteFood(int foodId) {
      foods.remove(foodId);
      return true;
    }

    @Override
    public boolean deleteRecipe(int recipeId) {
      recipes.remove(recipeId);
      return true;
    }

    @Override
    public boolean deleteUser(int userId) {
      users.remove(userId);
      return true;
    }
  }
}
