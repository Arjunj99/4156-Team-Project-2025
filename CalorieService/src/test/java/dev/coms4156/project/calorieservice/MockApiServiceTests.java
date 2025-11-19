package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the MockApiService class.
 */
public class MockApiServiceTests {

  private static List<Food> foods;
  private static List<Recipe> recipes;
  private static List<User> users;
  public static MockApiService service;
  public static Food food1;
  public static Recipe recipe1;
  public static User user1;
  private static FirestoreService firestoreService;

  /**
   * This method sets up our testing variables.
   */
  @BeforeAll
  public static void setUpDataForTesting() throws ExecutionException, InterruptedException {
    // Create mock FirestoreService manually since @BeforeAll is static
    firestoreService = org.mockito.Mockito.mock(FirestoreService.class);
    
    // Create some test data for tests that expect data to be present
    // Use instance variables that persist across method calls
    ArrayList<Food> testFoods = new ArrayList<>();
    testFoods.add(new Food("Apple", 1, 95, "Fruit"));
    testFoods.add(new Food("Banana", 2, 105, "Fruit"));
    
    ArrayList<Recipe> testRecipes = new ArrayList<>();
    // Recipe 1001 needs ingredients for calorie breakdown test
    ArrayList<Food> recipe1001Ingredients = new ArrayList<>();
    recipe1001Ingredients.add(new Food("Test Ingredient 1", 101, 200, "Test"));
    recipe1001Ingredients.add(new Food("Test Ingredient 2", 102, 288, "Test"));
    Recipe testRecipe = new Recipe("Test Recipe", 1001, "Dessert", 
        recipe1001Ingredients, 10, 5, 488);
    testRecipes.add(testRecipe);
    
    // Add another recipe in same category with lower calories for alternatives test
    ArrayList<Food> recipe2Ingredients = new ArrayList<>();
    recipe2Ingredients.add(new Food("Test Ingredient 3", 103, 100, "Test"));
    Recipe testRecipe2 = new Recipe("Lower Cal Recipe", 1002, "Dessert", 
        recipe2Ingredients, 5, 2, 100);
    testRecipes.add(testRecipe2);
    
    ArrayList<User> testUsers = new ArrayList<>();
    User testUser = new User("Test User", 1);
    testUser.getLikedRecipes().add(testRecipe);
    testUsers.add(testUser);
    
    // Mock FirestoreService to return test data - use thenAnswer to return current state
    when(firestoreService.getAllFoods()).thenAnswer(invocation -> new ArrayList<>(testFoods));
    when(firestoreService.getAllRecipes()).thenAnswer(invocation -> new ArrayList<>(testRecipes));
    when(firestoreService.getAllUsers()).thenAnswer(invocation -> new ArrayList<>(testUsers));
    
    // Use thenAnswer to handle specific IDs and general case - check the lists dynamically
    when(firestoreService.getFoodById(anyInt())).thenAnswer(invocation -> {
      int id = invocation.getArgument(0);
      return testFoods.stream()
          .filter(f -> f.getFoodId() == id)
          .findFirst()
          .orElse(null);
    });
    when(firestoreService.getRecipeById(anyInt())).thenAnswer(invocation -> {
      int id = invocation.getArgument(0);
      return testRecipes.stream()
          .filter(r -> r.getRecipeId() == id)
          .findFirst()
          .orElse(null);
    });
    when(firestoreService.getUserById(anyInt())).thenAnswer(invocation -> {
      int id = invocation.getArgument(0);
      return testUsers.stream()
          .filter(u -> u.getUserId() == id)
          .findFirst()
          .orElse(null);
    });
    
    // Mock query methods
    when(firestoreService.getFoodsByCategoryAndCalories(anyString(), anyInt()))
        .thenAnswer(invocation -> {
          String category = invocation.getArgument(0);
          int maxCalories = invocation.getArgument(1);
          return testFoods.stream()
              .filter(f -> f.getCategory().equals(category) && f.getCalories() < maxCalories)
              .collect(java.util.stream.Collectors.toList());
        });
    when(firestoreService.getRecipesByCategoryAndCalories(anyString(), anyInt()))
        .thenAnswer(invocation -> {
          String category = invocation.getArgument(0);
          int maxCalories = invocation.getArgument(1);
          return testRecipes.stream()
              .filter(r -> r.getCategory().equals(category) && r.getTotalCalories() <= maxCalories)
              .collect(java.util.stream.Collectors.toList());
        });
    when(firestoreService.getRecipesByCalories(anyInt()))
        .thenAnswer(invocation -> {
          int maxCalories = invocation.getArgument(0);
          return testRecipes.stream()
              .filter(r -> r.getTotalCalories() <= maxCalories)
              .collect(java.util.stream.Collectors.toList());
        });
    
    // Mock add methods - actually add to the lists and check for duplicates
    when(firestoreService.addFood(any(Food.class))).thenAnswer(invocation -> {
      Food food = invocation.getArgument(0);
      boolean exists = testFoods.stream().anyMatch(f -> f.getFoodId() == food.getFoodId());
      if (exists) {
        return false;
      }
      testFoods.add(food);
      return true;
    });
    when(firestoreService.addRecipe(any(Recipe.class))).thenAnswer(invocation -> {
      Recipe recipe = invocation.getArgument(0);
      boolean exists = testRecipes.stream().anyMatch(r -> r.getRecipeId() == recipe.getRecipeId());
      if (exists) {
        return false;
      }
      testRecipes.add(recipe);
      return true;
    });
    when(firestoreService.addUser(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      boolean exists = testUsers.stream().anyMatch(u -> u.getUserId() == user.getUserId());
      if (exists) {
        return false;
      }
      testUsers.add(user);
      return true;
    });
    
    // Mock update methods - objects are updated in-place, just return true
    when(firestoreService.updateRecipe(any(Recipe.class))).thenReturn(true);
    when(firestoreService.updateUser(any(User.class))).thenReturn(true);

    service = new MockApiService(firestoreService);
    service.setTestMode(true);
    foods = service.getFoods();
    recipes = service.getRecipes();
    users = service.getUsers();
    // Use very high IDs that are unlikely to exist in mock data
    food1 = new Food("Test Food", 99999, 100, "Test Category");
    recipe1 = new Recipe("Test Recipe", 99999, "Test Category", new ArrayList<>(), 50, 5, 0);
    user1 = new User("Test User", 99999);
  }

  @Test
  public void initializeEmptyMockApiServiceValidTest()
      throws ExecutionException, InterruptedException {
    // Create a new mock for this test
    FirestoreService mockFirestore = org.mockito.Mockito.mock(FirestoreService.class);
    when(mockFirestore.getAllFoods()).thenReturn(new ArrayList<>());
    when(mockFirestore.getAllRecipes()).thenReturn(new ArrayList<>());
    when(mockFirestore.getAllUsers()).thenReturn(new ArrayList<>());
    
    MockApiService emptyService = new MockApiService(mockFirestore);
    assertNotNull(emptyService);
    assertNotNull(emptyService.getFoods());
    assertNotNull(emptyService.getRecipes());
    assertNotNull(emptyService.getUsers());
    assertTrue(emptyService.getFoods().size() >= 0);
    assertTrue(emptyService.getRecipes().size() >= 0);
    assertTrue(emptyService.getUsers().size() >= 0);
  }

  @Test
  public void loadUsersWithRecipeIdsValidTest() {
    assertNotNull(users);
    assertTrue(users.size() > 0);

    User userWithLikedRecipes = users.stream()
        .filter(u -> u.getLikedRecipes().size() > 0)
        .findFirst()
        .orElse(null);

    if (userWithLikedRecipes != null) {
      assertNotNull(userWithLikedRecipes.getLikedRecipes());
      assertTrue(userWithLikedRecipes.getLikedRecipes().size() > 0);

      for (Recipe likedRecipe : userWithLikedRecipes.getLikedRecipes()) {
        assertNotNull(likedRecipe);
        assertNotNull(service.findRecipeById(likedRecipe.getRecipeId()));
      }
    }
  }

  @Test
  public void loadUsersWithRecipeIdsInvalidTest() throws ExecutionException, InterruptedException {
    // Create a new mock for this test
    FirestoreService mockFirestore = org.mockito.Mockito.mock(FirestoreService.class);
    when(mockFirestore.getAllUsers()).thenReturn(new ArrayList<>());
    when(mockFirestore.getRecipeById(anyInt())).thenReturn(null);
    
    MockApiService serviceWithMissingFile = new MockApiService(mockFirestore);

    assertNotNull(serviceWithMissingFile.getUsers());
    assertTrue(serviceWithMissingFile.getUsers().size() >= 0);
  }

  @Test
  public void findRecipeByIdValidTest() {
    if (!recipes.isEmpty()) {
      Recipe expectedRecipe = recipes.get(0);
      Recipe foundRecipe = service.findRecipeById(expectedRecipe.getRecipeId());
      assertNotNull(foundRecipe);
      assertEquals(expectedRecipe.getRecipeId(), foundRecipe.getRecipeId());
      assertEquals(expectedRecipe.getRecipeName(), foundRecipe.getRecipeName());
    }
  }

  @Test
  public void findRecipeByIdInvalidTest() {
    Recipe foundRecipe = service.findRecipeById(999999);
    assertNull(foundRecipe);
  }

  @Test
  public void findRecipeByIdAtypicalTest() {
    Recipe foundRecipe = service.findRecipeById(0);
    assertNull(foundRecipe);
  }

  @Test
  public void getRecipeByIdValidTest() {
    if (!recipes.isEmpty()) {
      Recipe expectedRecipe = recipes.get(0);
      Optional<Recipe> foundRecipe = service.getRecipeById(expectedRecipe.getRecipeId());
      assertTrue(foundRecipe.isPresent());
      assertEquals(expectedRecipe.getRecipeId(), foundRecipe.get().getRecipeId());
      assertEquals(expectedRecipe.getRecipeName(), foundRecipe.get().getRecipeName());
    }
  }

  @Test
  public void getRecipeByIdInvalidTest() {
    Optional<Recipe> foundRecipe = service.getRecipeById(999999);
    assertFalse(foundRecipe.isPresent());
  }

  @Test
  public void getRecipeByIdAtypicalTest() {
    Optional<Recipe> foundRecipe = service.getRecipeById(-1);
    assertFalse(foundRecipe.isPresent());
  }

  @Test
  public void findUserByIdValidTest() {
    if (!users.isEmpty()) {
      User expectedUser = users.get(0);
      User foundUser = service.findUserById(expectedUser.getUserId());
      assertNotNull(foundUser);
      assertEquals(expectedUser.getUserId(), foundUser.getUserId());
      assertEquals(expectedUser.getUsername(), foundUser.getUsername());
    }
  }

  @Test
  public void findUserByIdInvalidTest() {
    User foundUser = service.findUserById(999999);
    assertNull(foundUser);
  }

  @Test
  public void findUserByIdAtypicalTest() {
    User foundUser = service.findUserById(0);
    assertNull(foundUser);
  }

  @Test
  public void findFoodByIdValidTest() {
    if (!foods.isEmpty()) {
      Food expectedFood = foods.get(0);
      Food foundFood = service.findFoodById(expectedFood.getFoodId());
      assertNotNull(foundFood);
      assertEquals(expectedFood.getFoodId(), foundFood.getFoodId());
      assertEquals(expectedFood.getFoodName(), foundFood.getFoodName());
    }
  }

  @Test
  public void findFoodByIdInvalidTest() {
    Food foundFood = service.findFoodById(999999);
    assertNull(foundFood);
  }

  @Test
  public void findFoodByIdAtypicalTest() {
    Food foundFood = service.findFoodById(-100);
    assertNull(foundFood);
  }

  @Test
  public void getFoodsValidTest() {
    List<Food> serviceFoods = service.getFoods();
    assertNotNull(serviceFoods);
    assertTrue(serviceFoods.size() > 0);
  }

  @Test
  public void getRecipesValidTest() {
    List<Recipe> serviceRecipes = service.getRecipes();
    assertNotNull(serviceRecipes);
    assertTrue(serviceRecipes.size() > 0);
  }

  @Test
  public void getUsersValidTest() {
    List<User> serviceUsers = service.getUsers();
    assertNotNull(serviceUsers);
    assertTrue(serviceUsers.size() > 0);
  }

  @Test
  public void getFoodAlternativesValidTest() {
    if (!foods.isEmpty()) {
      Food targetFood = foods.get(0);
      List<Food> alternatives = service.getFoodAlternatives(targetFood.getFoodId());
      assertNotNull(alternatives);
      assertTrue(alternatives.size() <= 5);
    }
  }

  @Test
  public void getFoodAlternativesInvalidTest() {
    List<Food> alternatives = service.getFoodAlternatives(999999);
    assertNull(alternatives);
  }

  @Test
  public void getFoodAlternativesAtypicalTest() {
    List<Food> alternatives = service.getFoodAlternatives(-1);
    assertNull(alternatives);
  }

  // helper method to find unique food id
  private int findUnusedFoodId() {
    int candidate = 10000;
    while (service.findFoodById(candidate) != null) {
      candidate++;
    }
    return candidate;
  }

  @Test
  public void addFoodValidTest() {
    int foodId = findUnusedFoodId();
    Food validFood = new Food("Quinoa", foodId, 222, "Grain");
    int initialSize = service.getFoods().size();

    boolean result = service.addFood(validFood);
    assertTrue(result);

    assertEquals(initialSize + 1, service.getFoods().size());

    Food retrievedFood = service.findFoodById(foodId);
    assertNotNull(retrievedFood);
    assertEquals("Quinoa", retrievedFood.getFoodName());
    assertEquals(222, retrievedFood.getCalories());
    assertEquals("Grain", retrievedFood.getCategory());
  }

  @Test
  public void addFoodInvalidTest() {
    if (!foods.isEmpty()) {
      Food existingFood = foods.get(0);
      Food duplicateFood = new Food("Duplicate Food", existingFood.getFoodId(), 500, "Test");

      boolean result = service.addFood(duplicateFood);
      assertFalse(result);
    }
  }

  @Test
  public void addFoodAtypicalTest() {
    int foodId = findUnusedFoodId();
    Food extremeCalorieFood = new Food("Super High Calorie Item", foodId, 9999, "Special");
    int initialSize = service.getFoods().size();

    boolean result = service.addFood(extremeCalorieFood);
    assertTrue(result);

    assertEquals(initialSize + 1, service.getFoods().size());

    Food retrievedFood = service.findFoodById(foodId);
    assertNotNull(retrievedFood);
    assertEquals(9999, retrievedFood.getCalories());
    assertEquals("Special", retrievedFood.getCategory());
  }

  @Test
  public void likeRecipeValidTest() {
    if (!users.isEmpty() && !recipes.isEmpty()) {
      User user = users.get(0);

      // Find a recipe the user hasn't liked yet
      Recipe recipe = null;
      for (Recipe r : recipes) {
        if (!user.getLikedRecipes().contains(r)) {
          recipe = r;
          break;
        }
      }

      // If user has liked all recipes, create a new one
      if (recipe == null) {
        int recipeId = findUnusedRecipeId();
        recipe = buildTestRecipe(recipeId, "Test Recipe for Like");
        service.addRecipe(recipe);
      }

      int initialLikes = recipe.getLikes();
      final int initialUserLikedCount = user.getLikedRecipes().size();

      boolean result = service.likeRecipe(user.getUserId(), recipe.getRecipeId());
      assertTrue(result);

      assertEquals(initialLikes + 1, recipe.getLikes());
      assertTrue(user.getLikedRecipes().contains(recipe));
      assertEquals(initialUserLikedCount + 1, user.getLikedRecipes().size());
    }
  }

  @Test
  public void likeRecipeInvalidTest() {
    boolean result = service.likeRecipe(-1, -1);
    assertFalse(result);
  }

  @Test
  public void likeRecipeAtypicalTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);

      // Create a new recipe specifically for this test
      int recipeId = findUnusedRecipeId();
      Recipe recipe = buildTestRecipe(recipeId, "Test Recipe for Duplicate Like");
      service.addRecipe(recipe);

      // First like should succeed
      service.likeRecipe(user.getUserId(), recipe.getRecipeId());
      int likesAfterFirst = recipe.getLikes();
      int likedCountAfterFirst = user.getLikedRecipes().size();

      // Second like of same recipe should fail
      boolean result = service.likeRecipe(user.getUserId(), recipe.getRecipeId());
      assertFalse(result);

      assertEquals(likesAfterFirst, recipe.getLikes());
      assertEquals(likedCountAfterFirst, user.getLikedRecipes().size());
    }
  }

  @Test
  public void recommendHealthyValidTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommendHealthy(user.getUserId(), 500);
      assertNotNull(recommendations);
      assertTrue(recommendations.size() <= 10);
      for (Recipe recipe : recommendations) {
        assertTrue(recipe.getTotalCalories() <= 500);
      }
    }
  }

  @Test
  public void recommendHealthyInvalidTest() {
    List<Recipe> recommendations = service.recommendHealthy(999999, 500);
    assertNull(recommendations);
  }

  @Test
  public void recommendHealthyAtypicalTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommendHealthy(user.getUserId(), -100);
      assertNotNull(recommendations);
      assertTrue(recommendations.isEmpty());
    }
  }

  @Test
  public void recommendValidTest() {
    if (!users.isEmpty()) {
      User user = users.get(0);
      List<Recipe> recommendations = service.recommend(user.getUserId());
      assertNotNull(recommendations);
      assertTrue(recommendations.size() <= 10);
    }
  }

  @Test
  public void recommendInvalidTest() {
    List<Recipe> recommendations = service.recommend(999999);
    assertNull(recommendations);
  }

  @Test
  public void recommendAtypicalTest() {
    if (!users.isEmpty()) {
      User userWithNoLikes = new User("No Likes User", findUnusedUserId());
      users.add(userWithNoLikes);
      List<Recipe> recommendations = service.recommend(userWithNoLikes.getUserId());
      assertNull(recommendations);
    }
  }

  @Test
  public void getRecipeAlternativesValidTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      Optional<Map<String, List<Recipe>>> alternatives = 
          service.getRecipeAlternatives(recipe.getRecipeId());
      assertTrue(alternatives.isPresent());
      Map<String, List<Recipe>> alternativeMap = alternatives.get();
      assertNotNull(alternativeMap.get("topAlternatives"));
      assertNotNull(alternativeMap.get("randomAlternatives"));
    }
  }

  @Test
  public void getRecipeAlternativesInvalidTest() {
    Optional<Map<String, List<Recipe>>> alternatives = service.getRecipeAlternatives(999999);
    assertFalse(alternatives.isPresent());
  }

  @Test
  public void getRecipeAlternativesAtypicalTest() {
    int recipeId = findUnusedRecipeId();
    Recipe highCalRecipe = buildTestRecipe(recipeId, "Highest Calorie Recipe");
    highCalRecipe.getIngredients().get(0).setCalories(10000);
    service.addRecipe(highCalRecipe);

    Optional<Map<String, List<Recipe>>> alternatives = service.getRecipeAlternatives(recipeId);
    assertTrue(alternatives.isPresent());
    Map<String, List<Recipe>> alternativeMap = alternatives.get();
    assertTrue(alternativeMap.get("topAlternatives").size() >= 0);
    assertTrue(alternativeMap.get("randomAlternatives").size() >= 0);
  }

  @Test
  public void getTotalCaloriesValidTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      Optional<Integer> totalCalories = service.getTotalCalories(recipe.getRecipeId());
      assertTrue(totalCalories.isPresent());
      assertTrue(totalCalories.get() >= 0);
    }
  }

  @Test
  public void getTotalCaloriesInvalidTest() {
    Optional<Integer> totalCalories = service.getTotalCalories(999999);
    assertFalse(totalCalories.isPresent());
  }

  @Test
  public void getTotalCaloriesAtypicalTest() {
    int recipeId = findUnusedRecipeId();
    Recipe emptyRecipe = buildTestRecipe(recipeId, "Empty Ingredient Recipe");
    emptyRecipe.setIngredients(new ArrayList<>());
    service.addRecipe(emptyRecipe);

    Optional<Integer> totalCalories = service.getTotalCalories(recipeId);
    assertTrue(totalCalories.isPresent());
    assertEquals(0, totalCalories.get());
  }

  @Test
  public void getCalorieBreakdownValidTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      Optional<Map<String, Integer>> breakdown = service.getCalorieBreakdown(recipe.getRecipeId());
      assertTrue(breakdown.isPresent());
      assertFalse(breakdown.get().isEmpty());
    }
  }

  @Test
  public void getCalorieBreakdownInvalidTest() {
    Optional<Map<String, Integer>> breakdown = service.getCalorieBreakdown(999999);
    assertFalse(breakdown.isPresent());
  }

  @Test
  public void getCalorieBreakdownAtypicalTest() {
    int recipeId = findUnusedRecipeId();
    Recipe emptyRecipe = buildTestRecipe(recipeId, "No Ingredients Recipe");
    emptyRecipe.setIngredients(new ArrayList<>());
    service.addRecipe(emptyRecipe);

    Optional<Map<String, Integer>> breakdown = service.getCalorieBreakdown(recipeId);
    assertTrue(breakdown.isPresent());
    assertTrue(breakdown.get().isEmpty());
  }

  @Test
  public void addRecipeValidTest() {
    int recipeId = findUnusedRecipeId();
    Recipe newRecipe = buildTestRecipe(recipeId, "Valid Test Recipe");
    boolean result = service.addRecipe(newRecipe);
    assertTrue(result);
    assertNotNull(service.findRecipeById(recipeId));
  }

  @Test
  public void addRecipeInvalidTest() {
    boolean result = service.addRecipe(null);
    assertFalse(result);
  }

  @Test
  public void addRecipeAtypicalTest() {
    if (!recipes.isEmpty()) {
      Recipe existingRecipe = recipes.get(0);
      Recipe duplicateRecipe = buildTestRecipe(existingRecipe.getRecipeId(), "Duplicate Recipe");
      boolean result = service.addRecipe(duplicateRecipe);
      assertFalse(result);
    }
  }

  @Test
  public void incrementViewsValidTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      int initialViews = recipe.getViews();
      boolean result = service.incrementViews(recipe.getRecipeId());
      assertTrue(result);
      assertEquals(initialViews + 1, recipe.getViews());
    }
  }

  @Test
  public void incrementViewsInvalidTest() {
    boolean result = service.incrementViews(999999);
    assertFalse(result);
  }

  @Test
  public void incrementViewsAtypicalTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      service.incrementViews(recipe.getRecipeId());
      service.incrementViews(recipe.getRecipeId());
      int viewsAfterTwo = recipe.getViews();
      service.incrementViews(recipe.getRecipeId());
      assertEquals(viewsAfterTwo + 1, recipe.getViews());
    }
  }

  @Test
  public void incrementLikesValidTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      int initialLikes = recipe.getLikes();
      boolean result = service.incrementLikes(recipe.getRecipeId());
      assertTrue(result);
      assertEquals(initialLikes + 1, recipe.getLikes());
    }
  }

  @Test
  public void incrementLikesInvalidTest() {
    boolean result = service.incrementLikes(999999);
    assertFalse(result);
  }

  @Test
  public void incrementLikesAtypicalTest() {
    if (!recipes.isEmpty()) {
      Recipe recipe = recipes.get(0);
      service.incrementLikes(recipe.getRecipeId());
      service.incrementLikes(recipe.getRecipeId());
      int likesAfterTwo = recipe.getLikes();
      service.incrementLikes(recipe.getRecipeId());
      assertEquals(likesAfterTwo + 1, recipe.getLikes());
    }
  }

  private int findUnusedUserId() {
    int candidate = 9000;
    while (service.findUserById(candidate) != null) {
      candidate++;
    }
    return candidate;
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

  @Test
  public void totalCaloriesMatchesMockData() {
    Optional<Integer> totalCalories = service.getTotalCalories(1001);
    assertTrue(totalCalories.isPresent());
    assertEquals(488, totalCalories.get());
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
    while (service.getRecipeById(candidate).isPresent()) {
      candidate++;
    }
    return candidate;
  }

  /**
   * Clean up all test variables after all tests.
   */
  @AfterAll
  public static void tearDownServiceAfterTesting() {
    foods = null;
    recipes = null;
    users = null;
    service = null;
    food1 = null;
    recipe1 = null;
    user1 = null;
  }
}
