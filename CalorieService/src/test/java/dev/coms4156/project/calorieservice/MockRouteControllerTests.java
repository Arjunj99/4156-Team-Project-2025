package dev.coms4156.project.calorieservice;

import dev.coms4156.project.calorieservice.controller.RouteController;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.MockApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mocking Framework tests for the API routes exposed by
 * {@link dev.coms4156.project.calorieservice.controller.RouteController}.
 */
@WebMvcTest(RouteController.class)
public class MockRouteControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MockApiService mockApiService;

  /**
   * runs before each test to log test start and record start time.
   *
   * @param testInfo current test information
   */
  @BeforeEach
  public void setUp() {
    // No Setup Required for Now
  }

  /**
   * runs after each test to log test completion and execution time.
   *
   * @param testInfo current test information
   */
  @AfterEach
  public void tearDown() {
    // No Tear Down Required for Now
  }

  private String foodPayload(int foodId, String category, int calories) {
    return String.format("""
        {
          "foodName": "Generated Food %d",
          "foodId": %d,
          "calories": %d,
          "category": "%s"
        }
        """, foodId, foodId, calories, category);
  }

  private String recipePayload(int recipeId, String category) {
    return recipePayloadWithCounts(recipeId, category, 0, 0);
  }

  private String recipePayloadWithCounts(int recipeId, String category, int views, int likes) {
    return String.format("""
        {
          "recipeName": "Generated Recipe %d",
          "recipeId": %d,
          "category": "%s",
          "ingredients": [
            { "foodName": "Ingredient %d", "foodId": %d, "calories": 90, "category": "%s" }
          ],
          "views": %d,
          "likes": %d
        }
        """, recipeId, recipeId, category, recipeId, recipeId + 1000, category, views, likes);
  }

  // ======= 500 Tests - Invalid Inputs ====================================

  /**
   * Ensures {@code GET /food/alternative}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void foodAlternativeReturns500() throws Exception {
    when(mockApiService.getFoodAlternatives(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(get("/food/alternative").param("foodId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).getFoodAlternatives(1);
  }

  /**
   * Ensures {@code POST /food/addFood}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void addFoodReturns500() throws Exception {
    when(mockApiService.addFood(any(Food.class)))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(post("/food/addFood")
        .contentType(MediaType.APPLICATION_JSON)
        .content(foodPayload(0, "Garbage", 0)))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).addFood(any(Food.class));
  }

  /**
   * Ensures {@code GET /recipe/alternative}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void recipeAlternativeReturns500() throws Exception {
    when(mockApiService.getRecipeAlternatives(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(get("/recipe/alternative").param("recipeId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).getRecipeAlternatives(1);
  }

  /**
   * Ensures {@code POST /recipe/addRecipe}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void addRecipeReturns500() throws Exception {
    when(mockApiService.addRecipe(any(Recipe.class)))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(post("/recipe/addRecipe")
        .contentType(MediaType.APPLICATION_JSON)
        .content(recipePayload(1, "Garbage")))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).addRecipe(any(Recipe.class));
  }

  /**
   * Ensures {@code GET /recipe/totalCalorie}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void totalCalorieReturns500() throws Exception {
    when(mockApiService.getTotalCalories(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).getTotalCalories(1);
  }

  /**
   * Ensures {@code GET /recipe/calorieBreakdown}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void calorieBreakdownReturns500() throws Exception {
    when(mockApiService.getCalorieBreakdown(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).getCalorieBreakdown(1);
  }

  /**
   * Ensures {@code POST /user/recommend}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void recommendReturns500() throws Exception {
    when(mockApiService.recommend(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(get("/user/recommend").param("userId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).recommend(1);
  }

  /**
   * Ensures {@code GET /user/recommendHealthy}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void recommendHealthyReturns500() throws Exception {
    when(mockApiService.recommendHealthy(anyInt(), anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(get("/user/recommendHealthy")
        .param("userId", "1")
        .param("calorieMax", "0"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).recommendHealthy(1, 0);
  }

  /**
   * Ensures {@code POST /recipe/likeRecipe}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void recipeLikeRecipeReturns500() throws Exception {
    when(mockApiService.incrementLikes(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(post("/recipe/likeRecipe")
        .param("recipeId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).incrementLikes(1);
  }

  /**
   * Ensures {@code POST /user/likeRecipe}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void userLikeRecipeReturns500() throws Exception {
    when(mockApiService.likeRecipe(anyInt(), anyInt()))
      .thenThrow(new RuntimeException("DB down"));

    mockMvc.perform(post("/user/likeRecipe")
        .param("userId", "1")
        .param("recipeId", "2"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).likeRecipe(1, 2);
  }

  /**
   * Ensures {@code POST /recipe/viewRecipe}
   * returns HTTP 500 when service fails.
   */
  @Test
  public void viewRecipeReturns500() throws Exception {
    when(mockApiService.incrementViews(anyInt()))
      .thenThrow(new RuntimeException("DB is down"));

    mockMvc.perform(post("/recipe/viewRecipe")
        .param("recipeId", "1"))
      .andExpect(status().isInternalServerError());

    verify(mockApiService, times(1)).incrementViews(1);
  }

  // ======= 200 Tests - Valid Inputs ====================================
  /**
   * Ensures {@code GET /food/alternative}
   * returns HTTP 200 when service suceeds.
   */
  @Test
  void foodAlternativeReturns200() throws Exception {
    var foods = java.util.List.of(new Food(), new Food());
    when(mockApiService.getFoodAlternatives(1)).thenReturn(foods);

    mockMvc.perform(get("/food/alternative").param("foodId", "1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(2));

    verify(mockApiService, times(1)).getFoodAlternatives(1);
  }

  /**
   * Ensures {@code POST /food/addFood}
   * returns HTTP 200 when service suceeds.
   */
  @Test
  void addFoodReturns200() throws Exception {
    when(mockApiService.addFood(any(Food.class))).thenReturn(true);

    mockMvc.perform(post("/food/addFood")
        .contentType(MediaType.APPLICATION_JSON)
        .content(foodPayload(101, "Fruit", 95)))
      .andExpect(status().isOk())
      .andExpect(content().string("Food added successfully."));

    verify(mockApiService, times(1)).addFood(any(Food.class));
  }

  /**
   * Ensures {@code GET /recipe/alternative}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void recipeAlternativeReturns200() throws Exception {
    var r1 = new Recipe();
    var r2 = new Recipe();
    var r3 = new Recipe();

    when(mockApiService.getRecipeAlternatives(1))
      .thenReturn(java.util.Optional.of(java.util.Map.of(
        "alternatives", java.util.List.of(r1, r2, r3)
      )));

    mockMvc.perform(get("/recipe/alternative").param("recipeId", "1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.alternatives").isArray())
      .andExpect(jsonPath("$.alternatives.length()").value(3));

    verify(mockApiService, times(1)).getRecipeAlternatives(1);
  }

  /**
   * Ensures {@code POST /recipe/addRecipe}
   * returns HTTP 201 when service succeeds.
   */
  @Test
  void addRecipeReturns201() throws Exception {
    when(mockApiService.addRecipe(any(Recipe.class))).thenReturn(true);

    mockMvc.perform(post("/recipe/addRecipe")
        .contentType(MediaType.APPLICATION_JSON)
        .content(recipePayload(123, "Dinner")))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.message").value("Recipe added"))
      .andExpect(jsonPath("$.recipeId").value(123));

    verify(mockApiService, times(1)).addRecipe(any(Recipe.class));
  }

  /**
   * Ensures {@code GET /recipe/totalCalorie}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void totalCalorieReturns200() throws Exception {
    when(mockApiService.getTotalCalories(7))
      .thenReturn(java.util.Optional.of(450));

    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "7"))
      .andExpect(status().isOk());

    verify(mockApiService, times(1)).getTotalCalories(7);
  }

  /**
   * Ensures {@code GET /recipe/calorieBreakdown}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void calorieBreakdownReturns200() throws Exception {
    when(mockApiService.getCalorieBreakdown(7))
      .thenReturn(java.util.Optional.of(java.util.Map.of(
        "carbs", 100,
        "protein", 200,
        "fat", 150
      )));

    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "7"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.carbs").value(100))
      .andExpect(jsonPath("$.protein").value(200))
      .andExpect(jsonPath("$.fat").value(150));

    verify(mockApiService, times(1)).getCalorieBreakdown(7);
  }

  /**
   * Ensures {@code GET /user/recommend}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void recommendReturns200() throws Exception {
    var recs = java.util.List.of(new Recipe(), new Recipe());
    when(mockApiService.recommend(1)).thenReturn(recs);

    mockMvc.perform(get("/user/recommend").param("userId", "1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(2));

    verify(mockApiService, times(1)).recommend(1);
  }

  /**
   * Ensures {@code GET /user/recommendHealthy}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void recommendHealthyReturns200() throws Exception {
    var recs = java.util.List.of(new Recipe());
    when(mockApiService.recommendHealthy(1, 500)).thenReturn(recs);

    mockMvc.perform(get("/user/recommendHealthy")
        .param("userId", "1")
        .param("calorieMax", "500"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.length()").value(1));

    verify(mockApiService, times(1)).recommendHealthy(1, 500);
  }

  /**
   * Ensures {@code POST /recipe/likeRecipe}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void recipeLikeRecipeReturns200() throws Exception {
    when(mockApiService.incrementLikes(1)).thenReturn(true);

    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", "1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.message").value("Recipe like recorded"))
      .andExpect(jsonPath("$.recipeId").value(1));

    verify(mockApiService, times(1)).incrementLikes(1);
  }

  /**
   * Ensures {@code POST /user/likeRecipe}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void userLikeRecipeReturns200() throws Exception {
    when(mockApiService.likeRecipe(1, 2)).thenReturn(true);

    mockMvc.perform(post("/user/likeRecipe")
        .param("userId", "1")
        .param("recipeId", "2"))
      .andExpect(status().isOk())
      .andExpect(content().string("Recipe liked successfully."));

    verify(mockApiService, times(1)).likeRecipe(1, 2);
  }

  /**
   * Ensures {@code POST /recipe/viewRecipe}
   * returns HTTP 200 when service succeeds.
   */
  @Test
  void viewRecipeReturns200() throws Exception {
    when(mockApiService.incrementViews(1)).thenReturn(true);

    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", "1"))
      .andExpect(status().isOk());

    verify(mockApiService, times(1)).incrementViews(1);
  }
}
