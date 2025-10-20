package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit tests for {@link MockApiService}.
 */
@SpringBootTest
public class MockApiServiceTests {

  @Autowired
  private MockApiService mockApiService;

  @Test
  public void totalCaloriesMatchesMockData() {
    Optional<Integer> totalCalories = mockApiService.getTotalCalories(1001);
    assertTrue(totalCalories.isPresent());
    assertEquals(488, totalCalories.get());
  }

  @Test
  public void calorieBreakdownReturnsIngredientCalories() {
    Optional<Map<String, Integer>> breakdown = mockApiService.getCalorieBreakdown(1001);
    assertTrue(breakdown.isPresent());
    Map<String, Integer> breakdownMap = breakdown.get();
    assertEquals(4, breakdownMap.size());
    assertEquals(165, breakdownMap.get("Chicken Breast"));
  }

  @Test
  public void addRecipePersistsNewEntry() {
    int recipeId = findUnusedRecipeId();
    Recipe recipe = buildTestRecipe(recipeId, "Integration Test Recipe");

    boolean added = mockApiService.addRecipe(recipe);
    assertTrue(added);

    Optional<Recipe> storedRecipe = mockApiService.getRecipeById(recipeId);
    assertTrue(storedRecipe.isPresent());
    assertEquals("Integration Test Recipe", storedRecipe.get().getRecipeName());
  }

  @Test
  public void incrementViewAndLikeUpdateCounters() {
    int recipeId = findUnusedRecipeId();
    Recipe recipe = buildTestRecipe(recipeId, "Counter Test Recipe");
    assertTrue(mockApiService.addRecipe(recipe));

    Optional<Recipe> storedRecipe = mockApiService.getRecipeById(recipeId);
    assertTrue(storedRecipe.isPresent());
    Recipe baseline = storedRecipe.get();
    assertEquals(0, baseline.getViews());
    assertEquals(0, baseline.getLikes());

    assertTrue(mockApiService.incrementViews(recipeId));
    assertTrue(mockApiService.incrementLikes(recipeId));

    Recipe updated = mockApiService.getRecipeById(recipeId).orElseThrow();
    assertEquals(1, updated.getViews());
    assertEquals(1, updated.getLikes());
  }

  @Test
  public void alternativesRespectCategoryAndCalories() {
    Optional<Map<String, List<Recipe>>> alternatives = mockApiService.getRecipeAlternatives(1001);
    assertTrue(alternatives.isPresent());
    Map<String, List<Recipe>> alternativeMap = alternatives.get();

    assertNotNull(alternativeMap.get("topAlternatives"));
    assertNotNull(alternativeMap.get("randomAlternatives"));

    Recipe baseRecipe = mockApiService.getRecipeById(1001).orElseThrow();
    int baseCalories = baseRecipe.getTotalCalories();
    String baseCategory = baseRecipe.getCategory();

    alternativeMap.values().forEach(list -> list.forEach(recipe -> {
      assertFalse(recipe.getRecipeId() == 1001);
      assertEquals(baseCategory, recipe.getCategory());
      assertTrue(recipe.getTotalCalories() < baseCalories);
    }));
  }

  private Recipe buildTestRecipe(int recipeId, String name) {
    Recipe recipe = new Recipe();
    recipe.setRecipeId(recipeId);
    recipe.setRecipeName(name);
    recipe.setCategory("Test");
    recipe.setIngredients(new ArrayList<>());
    recipe.getIngredients().add(new Food("Test Ingredient", recipeId, 50, "Test"));
    recipe.setLikes(0);
    recipe.setViews(0);
    return recipe;
  }

  private int findUnusedRecipeId() {
    int candidate = 2000;
    while (mockApiService.getRecipeById(candidate).isPresent()) {
      candidate++;
    }
    return candidate;
  }
}
