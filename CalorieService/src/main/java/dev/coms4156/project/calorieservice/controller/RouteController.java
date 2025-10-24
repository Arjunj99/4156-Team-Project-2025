package dev.coms4156.project.calorieservice.controller;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class contains all the API routes for the application.
 */
@RestController
public class RouteController {

  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
  private final MockApiService mockApiService;

  /**
   * Constructs a new {@code RouteController} with the specified service.
   *
   * @param mockApiService The {@code MockApiService} to use for data operations
   */
  public RouteController(MockApiService mockApiService) {
    this.mockApiService = mockApiService;
  }

  /**
   * Returns a welcome message for the home page.
   *
   * @return A {@code String} containing the welcome message
   */
  @GetMapping({"/", "/index"})
  public String index() {
    logger.info("endpoint called: GET /index");
    return "Welcome to the home page! In order to make an API call direct your browser"
        + "or Postman to an endpoint.";
  }

  /**
   * Returns up to 5 random foods of the same category with lower calorie count
   * than the specified food.
   *
   * @param foodId The ID of the food to find alternatives for
   * @return A {@code ResponseEntity} containing either a list of up to 5 
   *         alternative {@code Food} objects with HTTP 200 if successful, or 
   *         an error message with HTTP 404 if food not found, or HTTP 500 
   *         for server errors
   */
  @GetMapping("/food/alternative")
  public ResponseEntity<?> getFoodAlternatives(@RequestParam int foodId) {
    logger.info("endpoint called: GET /food/alternative with foodId={}", foodId);
    try {
      List<Food> alternatives = mockApiService.getFoodAlternatives(foodId);
      
      if (alternatives == null) {
        return new ResponseEntity<>("Food with ID " + foodId + " not found.", 
            HttpStatus.NOT_FOUND);
      }
      
      if (alternatives.isEmpty()) {
        return new ResponseEntity<>(
            "No lower calorie alternatives found for food ID " + foodId + ".", 
            HttpStatus.OK);
      }
      
      return new ResponseEntity<>(alternatives, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting food alternatives.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Adds a new food to the service.
   *
   * @param food The {@code Food} object to add
   * @return A {@code ResponseEntity} containing a success message with HTTP 200 if successful,
   *         or an error message with HTTP 400 if food already exists or is invalid,
   *         or HTTP 500 for server errors
   */
  @PostMapping("/food/addFood")
  public ResponseEntity<?> addFood(@RequestBody Food food) {
    logger.info("endpoint called: POST /food/addFood");
    try {
      if (food == null) {
        return new ResponseEntity<>("Food object cannot be null.", HttpStatus.BAD_REQUEST);
      }
      
      boolean success = mockApiService.addFood(food);
      
      if (success) {
        return new ResponseEntity<>("Food added successfully.", HttpStatus.OK);
      } else {
        return new ResponseEntity<>("Food with ID " + food.getFoodId() 
            + " already exists or is invalid.", HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when adding food.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns a list of recommended recipes based on user's liked recipes 
   * under calorieMax.
   *
   * @param userId The ID of the user
   * @param calorieMax Maximum calorie count for recommendations
   * @return A {@code ResponseEntity} containing a list of up to 10 
   *         recommended {@code Recipe} objects with HTTP 200 if successful, 
   *         or an error message with HTTP 404 if user not found, or HTTP 500 
   *         for server errors
   */
  @GetMapping("/user/recommendHealthy")
  public ResponseEntity<?> recommendHealthy(@RequestParam int userId, 
      @RequestParam int calorieMax) {
    logger.info("endpoint called: GET /user/recommendHealthy with userId={}, calorieMax={}",
        userId, calorieMax);
    try {
      List<Recipe> recommendations = mockApiService.recommendHealthy(userId, calorieMax);
      
      if (recommendations == null) {
        return new ResponseEntity<>("User with ID " + userId + " not found.", 
            HttpStatus.NOT_FOUND);
      }
      
      if (recommendations.isEmpty()) {
        return new ResponseEntity<>("No healthy recipes found under " + calorieMax 
            + " calories for user " + userId + ".", HttpStatus.OK);
      }
      
      return new ResponseEntity<>(recommendations, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting healthy recommendations.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Returns a list of recommended recipes based on user's liked recipes.
   *
   * @param userId The ID of the user
   * @return A {@code ResponseEntity} containing a list of up to 10 
   *         recommended {@code Recipe} objects with HTTP 200 if successful, 
   *         or an error message with HTTP 404 if user not found, or HTTP 500 
   *         for server errors
   */
  @GetMapping("/user/recommend")
  public ResponseEntity<?> recommend(@RequestParam int userId) {
    logger.info("endpoint called: GET /user/recommend with userId={}", userId);
    try {
      List<Recipe> recommendations = mockApiService.recommend(userId);
      
      if (recommendations == null) {
        return new ResponseEntity<>("User with ID " + userId + " not found.", 
            HttpStatus.NOT_FOUND);
      }
      
      if (recommendations.isEmpty()) {
        return new ResponseEntity<>("No recommendations found for user " + userId + ".", 
            HttpStatus.OK);
      }
      
      return new ResponseEntity<>(recommendations, HttpStatus.OK);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting recommendations.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Retrieve alternative recipes with lower calorie counts in the same category.
   *
   * @param recipeId identifier of the recipe to compare against.
   * @return A {@code ResponseEntity} containing a map of up to 3 top-viewed 
   *         {@code Recipe} objects and up to 3 random {@code Recipe} objects 
   *         with HTTP 200 if successful, or HTTP 404 if the recipe was not found,
   *         or HTTP 500 for server errors.
   */
  @GetMapping("/recipe/alternative")
  public ResponseEntity<?> getRecipeAlternatives(@RequestParam("recipeId") int recipeId) {
    logger.info("endpoint called: GET /recipe/alternative with recipeId={}", recipeId);
    try {
      Optional<Map<String, List<Recipe>>> alternatives =
          mockApiService.getRecipeAlternatives(recipeId);
      if (alternatives.isEmpty()) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", "Recipe not found"));
      }
      return ResponseEntity.ok(alternatives.get());
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting alternatives.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Calculate the total calories for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return A {@code ResponseEntity} containing a map with the recipe ID and total calories 
   *         with HTTP 200 if successful, or HTTP 404 if the recipe was not found,
   *         or HTTP 500 for server errors.
   */
  @GetMapping("/recipe/totalCalorie")
  public ResponseEntity<?> getTotalCalories(@RequestParam("recipeId") int recipeId) {
    logger.info("endpoint called: GET /recipe/totalCalorie with recipeId={}", recipeId);
    try {
      Optional<Integer> totalCalories = mockApiService.getTotalCalories(recipeId);
      if (totalCalories.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
      }
      return ResponseEntity.ok(
        Map.of("recipeId", recipeId, "totalCalories", totalCalories.get()));
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting total calorie.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Provide a calorie breakdown for each ingredient in a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return A {@code ResponseEntity} containing a map of ingredient names to calorie values 
   *         with HTTP 200 if successful, or HTTP 404 if the recipe was not found,
   *         or HTTP 500 for server errors.
   */
  @GetMapping("/recipe/calorieBreakdown")
  public ResponseEntity<?> getCalorieBreakdown(@RequestParam("recipeId") int recipeId) {
    logger.info("endpoint called: GET /recipe/calorieBreakdown with recipeId={}", recipeId);
    try {
      Optional<Map<String, Integer>> breakdown = mockApiService.getCalorieBreakdown(recipeId);
      if (breakdown.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
      }
      return ResponseEntity.ok(breakdown.get());
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when getting calorie breakdown.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Persist a new recipe in the mock service.
   *
   * @param recipe {@code Recipe} payload to store.
   * @return A {@code ResponseEntity} containing a success message with 
   *         HTTP 201 when created, or an error message with HTTP 400 if 
   *         recipe payload is invalid or recipe ID is missing, or HTTP 409 
   *         if the recipe ID already exists,
   *         or HTTP 500 for server errors.
   */
  @PostMapping("/recipe/addRecipe")
  public ResponseEntity<?> addRecipe(@RequestBody Recipe recipe) {
    logger.info("endpoint called: POST /recipe/addRecipe");
    try {
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
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when adding a recipe.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Record a view for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return A {@code ResponseEntity} containing a confirmation message with 
   *         HTTP 200 if successful, or HTTP 404 if the recipe was not found,
   *         or HTTP 500 for server errors.
   */
  @PostMapping("/recipe/viewRecipe")
  public ResponseEntity<?> viewRecipe(@RequestParam("recipeId") int recipeId) {
    logger.info("endpoint called: POST /recipe/viewRecipe with recipeId={}", recipeId);
    try {
      boolean updated = mockApiService.incrementViews(recipeId);
      if (!updated) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
      }
      return ResponseEntity.ok(
        Map.of("message", "Recipe view recorded", "recipeId", recipeId));
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when viewing a recipe.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Record a like for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return A {@code ResponseEntity} containing a confirmation message with 
   *         HTTP 200 if successful, or HTTP 404 if the recipe was not found,
   *         or HTTP 500 for server errors.
   */
  @PostMapping("/recipe/likeRecipe")
  public ResponseEntity<?> likeRecipe(@RequestParam("recipeId") int recipeId) {
    logger.info("endpoint called: POST /recipe/likeRecipe with recipeId={}", recipeId);
    try {
      boolean updated = mockApiService.incrementLikes(recipeId);
      if (!updated) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Recipe not found"));
      }
      return ResponseEntity.ok(
        Map.of("message", "Recipe like recorded", "recipeId", recipeId));
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when liking a recipe.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Adds a recipe to a user's liked recipes.
   *
   * @param userId The ID of the user
   * @param recipeId The ID of the recipe to like
   * @return A {@code ResponseEntity} containing a success message with 
   *         HTTP 200 if successful, or an error message with HTTP 400 if 
   *         user/recipe not found or already liked, or HTTP 500 for server 
   *         errors
   */
  @PostMapping("/user/likeRecipe")
  public ResponseEntity<?> likeRecipe(@RequestParam int userId, @RequestParam int recipeId) {
    logger.info("endpoint called: POST /user/likeRecipe with userId={}, recipeId={}",
        userId, recipeId);
    try {
      boolean success = mockApiService.likeRecipe(userId, recipeId);
      
      if (success) {
        return new ResponseEntity<>("Recipe liked successfully.", HttpStatus.OK);
      } else {
        return new ResponseEntity<>("User with ID " + userId + " or recipe with ID " 
            + recipeId + " not found, or recipe already liked.", HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>("Error occurred when liking recipe.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}




