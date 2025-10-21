package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the MockApiService class.
 */
@SpringBootTest
public class MockApiServiceTests {

  private static List<Food> foods;
  private static List<Recipe> recipes;
  private static List<User> users;
  public static MockApiService service;
  public static Food food1;
  public static Recipe recipe1;
  public static User user1;

  /**
   * This method sets up our testing variables.
   */
  @BeforeAll
  public static void setUpDataForTesting() {
    service = new MockApiService();
    foods = service.getFoods();
    recipes = service.getRecipes();
    users = service.getUsers();
    // Use very high IDs that are unlikely to exist in mock data
    food1 = new Food("Test Food", 99999, 100, "Test Category");
    recipe1 = new Recipe("Test Recipe", 99999, "Test Category", new ArrayList<>(), 50, 5);
    user1 = new User("Test User", 99999);
  }

  @Test
  public void getFoodsTest() {
    List<Food> serviceFoods = service.getFoods();
    assertNotNull(serviceFoods);
    assertTrue(serviceFoods.size() > 0);
  }

  @Test
  public void getRecipesTest() {
    List<Recipe> serviceRecipes = service.getRecipes();
    assertNotNull(serviceRecipes);
    assertTrue(serviceRecipes.size() > 0);
  }

  @Test
  public void getUsersTest() {
    List<User> serviceUsers = service.getUsers();
    assertNotNull(serviceUsers);
    assertTrue(serviceUsers.size() > 0);
  }

  @Test
  public void addFoodNullTest() {
    boolean result = service.addFood(null);
    assertFalse(result);
  }

  @Test
  public void getFoodAlternativesFoundTest() {
    if (!foods.isEmpty()) {
      Food targetFood = foods.get(0);
      List<Food> alternatives = service.getFoodAlternatives(targetFood.getFoodId());
      assertNotNull(alternatives);
      assertTrue(alternatives.size() <= 5);
    }
  }

  @Test
  public void getFoodAlternativesNotFoundTest() {
    List<Food> alternatives = service.getFoodAlternatives(999999);
    assertNull(alternatives);
  }

  @Test
  public void likeRecipeUserNotFoundTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      boolean result = service.likeRecipe(999999, recipe.getRecipeId());
      assertFalse(result);
    }
  }

  @Test
  public void likeRecipeRecipeNotFoundTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      boolean result = service.likeRecipe(user.getUserId(), 999999);
      assertFalse(result);
    }
  }

  @Test
  public void recommendHealthyUserFoundTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommendHealthy(user.getUserId(), 500);
      assertNotNull(recommendations);
      assertTrue(recommendations.size() <= 10);
    }
  }

  @Test
  public void recommendHealthyUserNotFoundTest() {
    List<Recipe> recommendations = service.recommendHealthy(999999, 500);
    assertNull(recommendations);
  }

  @Test
  public void recommendUserFoundTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommend(user.getUserId());
      assertNotNull(recommendations);
      assertTrue(recommendations.size() <= 10);
    }
  }

  @Test
  public void recommendUserNotFoundTest() {
    List<Recipe> recommendations = service.recommend(999999);
    assertNull(recommendations);
  }

  @Test
  public void recommendHealthyWithLowCalorieLimitTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommendHealthy(user.getUserId(), 50);
      assertNotNull(recommendations);
      assertTrue(recommendations.size() <= 10);
    }
  }

  @Test
  public void recommendHealthyWithHighCalorieLimitTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommendHealthy(user.getUserId(), 2000);
      assertNotNull(recommendations);
      assertTrue(recommendations.size() <= 10);
    }
  }

  @Test
  public void getFoodAlternativesWithNoAlternativesTest() {
    if (!foods.isEmpty()) {
      Food lowCalorieFood = foods.stream()
          .min((f1, f2) -> Integer.compare(f1.getCalories(), f2.getCalories()))
          .orElse(foods.get(0));
      
      List<Food> alternatives = service.getFoodAlternatives(lowCalorieFood.getFoodId());
      assertNotNull(alternatives);
    }
  }

  @Test
  public void getFoodAlternativesWithManyAlternativesTest() {
    if (!foods.isEmpty()) {
      Food highCalorieFood = foods.stream()
          .max((f1, f2) -> Integer.compare(f1.getCalories(), f2.getCalories()))
          .orElse(foods.get(0));
      
      List<Food> alternatives = service.getFoodAlternatives(highCalorieFood.getFoodId());
      assertNotNull(alternatives);
      assertTrue(alternatives.size() <= 5);
    }
  }

  /**
   * This method checks if a food is in the foods arraylist.
   */
  public boolean checkFoods(Food temp) {
    foods = service.getFoods();
    for (Food food : foods) {
      if (food.equals(temp)) {
        return true;
      }   
    }
    return false;
  }

  /**
   * This method checks if a recipe is in the recipes arraylist.
   */
  public boolean checkRecipes(Recipe temp) {
    recipes = service.getRecipes();
    for (Recipe recipe : recipes) {
      if (recipe.equals(temp)) {
        return true;
      }   
    }
    return false;
  }

  /**
   * This method checks if a user is in the users arraylist.
   */
  public boolean checkUsers(User temp) {
    users = service.getUsers();
    for (User user : users) {
      if (user.equals(temp)) {
        return true;
      }   
    }
    return false;
  }
}
