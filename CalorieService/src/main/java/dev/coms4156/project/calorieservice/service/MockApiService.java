package dev.coms4156.project.calorieservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Service that simulates access to recipe data via the mock JSON assets.
 */
@Service
public class MockApiService {

  private final Map<Integer, Recipe> recipes = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public MockApiService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Load recipe data from the mock JSON file into memory.
   */
  @PostConstruct
  public void initialize() {
    Resource recipeResource = new ClassPathResource("mockdata/recipe.json");
    try (InputStream stream = recipeResource.getInputStream()) {
      Recipe[] recipeArray = objectMapper.readValue(stream, Recipe[].class);
      for (Recipe recipe : recipeArray) {
        if (recipe.getIngredients() == null) {
          recipe.setIngredients(new ArrayList<>());
        }
        recipes.put(recipe.getRecipeId(), recipe);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to load mock recipe data", exception);
    }
  }

  /**
   * Retrieve a recipe by its identifier.
   *
   * @param recipeId identifier of the recipe to fetch.
   * @return optional recipe if present.
   */
  public Optional<Recipe> getRecipeById(int recipeId) {
    return Optional.ofNullable(recipes.get(recipeId));
  }

  /**
   * Find alternate recipes in the same category with lower total calories.
   *
   * @param recipeId identifier of the recipe to compare against.
   * @return optional map containing two lists: topAlternatives and randomAlternatives.
   */
  public Optional<Map<String, List<Recipe>>> getRecipeAlternatives(int recipeId) {
    Recipe baseRecipe = recipes.get(recipeId);
    if (baseRecipe == null) {
      return Optional.empty();
    }

    int baseCalories = baseRecipe.getTotalCalories();
    String baseCategory = baseRecipe.getCategory();

    List<Recipe> candidates = recipes.values().stream()
        .filter(recipe -> recipe.getRecipeId() != recipeId)
        .filter(recipe -> sameCategory(baseCategory, recipe.getCategory()))
        .filter(recipe -> recipe.getTotalCalories() < baseCalories)
        .collect(Collectors.toList());

    List<Recipe> topAlternatives = candidates.stream()
        .sorted((first, second) -> Integer.compare(second.getViews(), first.getViews()))
        .limit(3)
        .collect(Collectors.toList());

    List<Recipe> randomPool = new ArrayList<>(candidates);
    randomPool.removeAll(topAlternatives);
    Collections.shuffle(randomPool);

    List<Recipe> randomAlternatives = randomPool.stream()
        .limit(3)
        .collect(Collectors.toList());

    Map<String, List<Recipe>> response = new HashMap<>();
    response.put("topAlternatives", topAlternatives);
    response.put("randomAlternatives", randomAlternatives);
    return Optional.of(response);
  }

  private boolean sameCategory(String first, String second) {
    if (first == null && second == null) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    return first.equalsIgnoreCase(second);
  }

  /**
   * Calculate the total calorie count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return optional total calories if the recipe exists.
   */
  public Optional<Integer> getTotalCalories(int recipeId) {
    return Optional.ofNullable(recipes.get(recipeId))
        .map(Recipe::getTotalCalories);
  }

  /**
   * Produce a calorie breakdown for each ingredient within a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return optional ordered map of ingredient names to calorie counts.
   */
  public Optional<Map<String, Integer>> getCalorieBreakdown(int recipeId) {
    Recipe recipe = recipes.get(recipeId);
    if (recipe == null) {
      return Optional.empty();
    }
    Map<String, Integer> breakdown = new LinkedHashMap<>();
    for (Food ingredient : recipe.getIngredients()) {
      breakdown.put(ingredient.getFoodName(), ingredient.getCalories());
    }
    return Optional.of(breakdown);
  }

  /**
   * Store a new recipe if the identifier has not been used.
   *
   * @param recipe recipe to persist.
   * @return true when the recipe is added; false if the id already exists or payload invalid.
   */
  public boolean addRecipe(Recipe recipe) {
    if (recipe == null || recipes.containsKey(recipe.getRecipeId())) {
      return false;
    }
    if (recipe.getIngredients() == null) {
      recipe.setIngredients(new ArrayList<>());
    }
    recipes.put(recipe.getRecipeId(), recipe);
    return true;
  }

  /**
   * Increment the recorded view count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return true when the recipe exists and the view is recorded.
   */
  public boolean incrementViews(int recipeId) {
    Recipe recipe = recipes.get(recipeId);
    if (recipe == null) {
      return false;
    }
    recipe.incrementViews();
    return true;
  }

  /**
   * Increment the recorded like count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return true when the recipe exists and the like is recorded.
   */
  public boolean incrementLikes(int recipeId) {
    Recipe recipe = recipes.get(recipeId);
    if (recipe == null) {
      return false;
    }
    recipe.incrementLikes();
    return true;
  }
}
