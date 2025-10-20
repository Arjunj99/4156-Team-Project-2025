package dev.coms4156.project.calorieservice.controller;

import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes REST endpoints for recipe related operations.
 */
@RestController
@RequestMapping("/recipe")
public class RouteController {

  private final MockApiService mockApiService;

  public RouteController(MockApiService mockApiService) {
    this.mockApiService = mockApiService;
  }

  /**
   * Retrieve alternative recipes with lower calorie counts in the same category.
   *
   * @param recipeId identifier of the recipe to compare against.
   * @return lists of top-viewed and random alternatives when found.
   */
  @GetMapping("/alternative")
  public ResponseEntity<?> getRecipeAlternatives(@RequestParam("recipeId") int recipeId) {
    Optional<Map<String, List<Recipe>>> alternatives =
        mockApiService.getRecipeAlternatives(recipeId);
    if (alternatives.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
    }
    return ResponseEntity.ok(alternatives.get());
  }

  /**
   * Calculate the total calories for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return total calories when the recipe exists.
   */
  @GetMapping("/totalCalorie")
  public ResponseEntity<?> getTotalCalories(@RequestParam("recipeId") int recipeId) {
    Optional<Integer> totalCalories = mockApiService.getTotalCalories(recipeId);
    if (totalCalories.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
    }
    return ResponseEntity.ok(
        Map.of("recipeId", recipeId, "totalCalories", totalCalories.get()));
  }

  /**
   * Provide a calorie breakdown for each ingredient in a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return map of ingredient names to calorie values when the recipe exists.
   */
  @GetMapping("/calorieBreakdown")
  public ResponseEntity<?> getCalorieBreakdown(@RequestParam("recipeId") int recipeId) {
    Optional<Map<String, Integer>> breakdown = mockApiService.getCalorieBreakdown(recipeId);
    if (breakdown.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
    }
    return ResponseEntity.ok(breakdown.get());
  }

  /**
   * Persist a new recipe in the mock service.
   *
   * @param recipe recipe payload to store.
   * @return 201 when created, or an error response when validation fails.
   */
  @PostMapping("/addRecipe")
  public ResponseEntity<?> addRecipe(@RequestBody Recipe recipe) {
    if (recipe == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "Recipe payload is required"));
    }
    if (recipe.getRecipeId() == 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "Recipe id must be provided"));
    }
    boolean added = mockApiService.addRecipe(recipe);
    if (!added) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of(
              "message", "Recipe with id already exists",
              "recipeId", recipe.getRecipeId()));
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("message", "Recipe added", "recipeId", recipe.getRecipeId()));
  }

  /**
   * Record a view for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return confirmation when the recipe exists.
   */
  @PostMapping("/viewRecipe")
  public ResponseEntity<?> viewRecipe(@RequestParam("recipeId") int recipeId) {
    boolean updated = mockApiService.incrementViews(recipeId);
    if (!updated) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
    }
    return ResponseEntity.ok(Map.of("message", "Recipe view recorded", "recipeId", recipeId));
  }

  /**
   * Record a like for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return confirmation when the recipe exists.
   */
  @PostMapping("/likeRecipe")
  public ResponseEntity<?> likeRecipe(@RequestParam("recipeId") int recipeId) {
    boolean updated = mockApiService.incrementLikes(recipeId);
    if (!updated) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
    }
    return ResponseEntity.ok(Map.of("message", "Recipe like recorded", "recipeId", recipeId));
  }
}
