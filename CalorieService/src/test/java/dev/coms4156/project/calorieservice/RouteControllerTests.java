package dev.coms4156.project.calorieservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.coms4156.project.calorieservice.service.MockApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for recipe endpoints exposed by RouteController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RouteControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockApiService mockApiService;

  @Test
  public void alternativeEndpointReturnsRecommendations() throws Exception {
    mockMvc.perform(get("/recipe/alternative").param("recipeId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives").isArray())
        .andExpect(jsonPath("$.randomAlternatives").isArray());
  }

  @Test
  public void totalCalorieEndpointReturnsAggregatedCalories() throws Exception {
    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recipeId").value(1001))
        .andExpect(jsonPath("$.totalCalories").value(488));
  }

  @Test
  public void calorieBreakdownEndpointReturnsIngredientCalories() throws Exception {
    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.['Chicken Breast']").value(165));
  }

  @Test
  public void addRecipeEndpointCreatesRecipeAndHandlesConflict() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayload(recipeId, "Snack")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.recipeId").value(recipeId));

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayload(recipeId, "Snack")))
        .andExpect(status().isConflict());
  }

  @Test
  public void viewRecipeEndpointIncrementsViews() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayload(recipeId, "Lunch")))
        .andExpect(status().isCreated());

    int initialViews = mockApiService.getRecipeById(recipeId).orElseThrow().getViews();

    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk());

    int updatedViews = mockApiService.getRecipeById(recipeId).orElseThrow().getViews();
    Assertions.assertEquals(initialViews + 1, updatedViews);
  }

  @Test
  public void likeRecipeEndpointIncrementsLikes() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayload(recipeId, "Dinner")))
        .andExpect(status().isCreated());

    int initialLikes = mockApiService.getRecipeById(recipeId).orElseThrow().getLikes();

    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk());

    int updatedLikes = mockApiService.getRecipeById(recipeId).orElseThrow().getLikes();
    Assertions.assertEquals(initialLikes + 1, updatedLikes);
  }

  @Test
  public void alternativeEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(get("/recipe/alternative").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void totalCalorieEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void calorieBreakdownEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void viewRecipeEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void likeRecipeEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void addRecipeEndpointReturns400ForMissingRecipeId() throws Exception {
    String payload = """
        {
          "recipeName": "Test Recipe",
          "recipeId": 0,
          "category": "Snack",
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """;
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Recipe id must be provided"));
  }

  @Test
  public void addRecipeEndpointReturns400ForMalformedJson() throws Exception {
    String malformedPayload = "{recipeName: Test, recipeId: }";
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(malformedPayload))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void alternativeEndpointReturnsEmptyListsWhenNoAlternativesExist() throws Exception {
    int recipeId = findUnusedRecipeId();
    String lowCalPayload = String.format("""
        {
          "recipeName": "Lowest Calorie Recipe",
          "recipeId": %d,
          "category": "UniqueCategory",
          "ingredients": [
            { "foodName": "Low Cal Food", "foodId": %d, "calories": 1, "category": "Test" }
          ],
          "views": 0,
          "likes": 0
        }
        """, recipeId, recipeId + 1000);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(lowCalPayload))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/recipe/alternative").param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives").isEmpty())
        .andExpect(jsonPath("$.randomAlternatives").isEmpty());
  }

  @Test
  public void recipeWithZeroIngredientsReturnsZeroCalories() throws Exception {
    int recipeId = findUnusedRecipeId();
    String emptyIngredients = String.format("""
        {
          "recipeName": "Empty Recipe",
          "recipeId": %d,
          "category": "Test",
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """, recipeId);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(emptyIngredients))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCalories").value(0));
  }

  @Test
  public void recipeWithNullCategoryCanBeAdded() throws Exception {
    int recipeId = findUnusedRecipeId();
    String nullCategoryPayload = String.format("""
        {
          "recipeName": "No Category Recipe",
          "recipeId": %d,
          "category": null,
          "ingredients": [
            { "foodName": "Test Food", "foodId": %d, "calories": 100, "category": "Test" }
          ],
          "views": 0,
          "likes": 0
        }
        """, recipeId, recipeId + 1000);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(nullCategoryPayload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.recipeId").value(recipeId));
  }

  private String recipePayload(int recipeId, String category) {
    return String.format("""
        {
          "recipeName": "Generated Recipe %d",
          "recipeId": %d,
          "category": "%s",
          "ingredients": [
            { "foodName": "Ingredient %d", "foodId": %d, "calories": 90, "category": "%s" }
          ],
          "views": 0,
          "likes": 0
        }
        """, recipeId, recipeId, category, recipeId, recipeId + 1000, category);
  }

  private int findUnusedRecipeId() {
    int candidate = 6000;
    while (mockApiService.getRecipeById(candidate).isPresent()) {
      candidate++;
    }
    return candidate;
  }
}
