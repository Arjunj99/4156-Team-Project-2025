package dev.coms4156.project.calorieservice.controller;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.List;
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

  private final MockApiService mockApiService;

  public RouteController(MockApiService mockApiService) {
    this.mockApiService = mockApiService;
  }

  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the home page! In order to make an API call direct your browser"
        + "or Postman to an endpoint.";
  }

  /**
   * Returns 5 random foods of the same category with lower calorie count than the specified food.
   *
   * @param foodId The ID of the food to find alternatives for
   * @return A ResponseEntity containing either a list of alternative foods with HTTP 200,
   *         or an error message with HTTP 404 if food not found, or HTTP 500 for server errors
   */
  @GetMapping("/food/alternative")
  public ResponseEntity<?> getFoodAlternatives(@RequestParam int foodId) {
    try {
      List<Food> alternatives = mockApiService.getFoodAlternatives(foodId);
      
      if (alternatives == null) {
        return new ResponseEntity<>("Food with ID " + foodId + " not found.", HttpStatus.NOT_FOUND);
      }
      
      if (alternatives.isEmpty()) {
        return new ResponseEntity<>("No lower calorie alternatives found for food ID " + foodId 
            + ".", HttpStatus.OK);
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
   * @param food The food object to add
   * @return A ResponseEntity containing a success message with HTTP 200 if successful,
   *         or an error message with HTTP 400 if food already exists or is invalid,
   *         or HTTP 500 for server errors
   */
  @PostMapping("/food/addFood")
  public ResponseEntity<?> addFood(@RequestBody Food food) {
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
   * Adds a recipe to a user's liked recipes.
   *
   * @param userId The ID of the user
   * @param recipeId The ID of the recipe to like
   * @return A ResponseEntity containing a success message with HTTP 200 if successful,
   *         or an error message with HTTP 400 if user/recipe not found or already liked,
   *         or HTTP 500 for server errors
   */
  @PostMapping("/user/likeRecipe")
  public ResponseEntity<?> likeRecipe(@RequestParam int userId, @RequestParam int recipeId) {
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

  /**
   * Returns a list of recommended recipes based on user's liked recipes under calorieMax.
   *
   * @param userId The ID of the user
   * @param calorieMax Maximum calorie count for recommendations
   * @return A ResponseEntity containing either a list of recommended recipes with HTTP 200,
   *         or an error message with HTTP 404 if user not found, or HTTP 500 for server errors
   */
  @GetMapping("/user/recommendHealthy")
  public ResponseEntity<?> recommendHealthy(@RequestParam int userId, 
      @RequestParam int calorieMax) {
    try {
      List<Recipe> recommendations = mockApiService.recommendHealthy(userId, calorieMax);
      
      if (recommendations == null) {
        return new ResponseEntity<>("User with ID " + userId + " not found.", HttpStatus.NOT_FOUND);
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
   * @return A ResponseEntity containing either a list of recommended recipes with HTTP 200,
   *         or an error message with HTTP 404 if user not found, or HTTP 500 for server errors
   */
  @GetMapping("/user/recommend")
  public ResponseEntity<?> recommend(@RequestParam int userId) {
    try {
      List<Recipe> recommendations = mockApiService.recommend(userId);
      
      if (recommendations == null) {
        return new ResponseEntity<>("User with ID " + userId + " not found.", HttpStatus.NOT_FOUND);
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
}




