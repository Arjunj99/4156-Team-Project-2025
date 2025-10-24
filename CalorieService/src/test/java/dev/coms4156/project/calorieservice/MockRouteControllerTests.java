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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
  }
}
