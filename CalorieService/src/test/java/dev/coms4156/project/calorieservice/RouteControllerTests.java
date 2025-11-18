package dev.coms4156.project.calorieservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.coms4156.project.calorieservice.controller.RouteController;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;


/**
 * Integration tests for the API routes exposed by
 * {@link dev.coms4156.project.calorieservice.controller.RouteController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RouteControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockApiService mockApiService;

  @MockBean
  private FirestoreService firestoreService;

  // Instance variables to hold mock data that persists across method calls within a test
  private ArrayList<Food> mockFoods;
  private ArrayList<Recipe> mockRecipes;
  private ArrayList<User> mockUsers;

  /**
   * Sets up the mock FirestoreService with test data before each test.
   *
   * @throws ExecutionException if there's an error setting up the mock
   * @throws InterruptedException if the setup is interrupted
   */
  @BeforeEach
  public void setUpMockFirestore() throws ExecutionException, InterruptedException {
    // Set up initial mock data - use instance variables so they persist across method calls
    mockFoods = new ArrayList<>();
    // Food ID 1 - Apple (95 calories, Fruit) - needs lower calorie alternatives
    mockFoods.add(new Food("Apple", 1, 95, "Fruit"));
    // Add lower calorie fruit alternatives for food ID 1
    mockFoods.add(new Food("Strawberry", 3, 49, "Fruit")); // Lower than Apple (95)
    mockFoods.add(new Food("Orange", 4, 62, "Fruit")); // Lower than Apple (95)
    mockFoods.add(new Food("Banana", 2, 105, "Fruit")); // Higher than Apple, won't be alternative
    
    mockRecipes = new ArrayList<>();
    // Recipe 1001 needs ingredients with "Chicken Breast" and totalCalories of 488
    ArrayList<Food> recipe1001Ingredients = new ArrayList<>();
    Food chickenBreast = new Food("Chicken Breast", 13, 165, "Protein");
    Food brownRice = new Food("Brown Rice (1 cup)", 10, 216, "Grain");
    Food broccoli = new Food("Broccoli", 5, 55, "Vegetable");
    Food carrots = new Food("Carrots", 6, 52, "Vegetable");
    recipe1001Ingredients.add(chickenBreast);
    recipe1001Ingredients.add(brownRice);
    recipe1001Ingredients.add(broccoli);
    recipe1001Ingredients.add(carrots);
    // Total: 165 + 216 + 55 + 52 = 488
    Recipe mockRecipe = new Recipe("Test Recipe", 1001, "Dessert", 
        recipe1001Ingredients, 10, 5, 488);
    mockRecipes.add(mockRecipe);
    
    // Add more recipes for recommendations to work
    Recipe recipe2 = new Recipe("Another Recipe", 1002, "Dessert", 
        new ArrayList<>(), 5, 2, 300);
    mockRecipes.add(recipe2);
    Recipe recipe3 = new Recipe("Third Recipe", 1003, "Dessert", 
        new ArrayList<>(), 3, 1, 400);
    mockRecipes.add(recipe3);
    
    mockUsers = new ArrayList<>();
    User mockUser = new User("Test User", 501);
    mockUser.getLikedRecipes().add(mockRecipe);
    mockUsers.add(mockUser);
    
    // Add users 508 and 509 for tests that need them
    User user508 = new User("User 508", 508);
    mockUsers.add(user508);
    User user509 = new User("User 509", 509);
    mockUsers.add(user509);
    
    // Mock FirestoreService methods - return the lists so they can be modified
    when(firestoreService.getAllFoods()).thenAnswer(invocation -> new ArrayList<>(mockFoods));
    when(firestoreService.getAllRecipes()).thenAnswer(invocation -> new ArrayList<>(mockRecipes));
    when(firestoreService.getAllUsers()).thenAnswer(invocation -> new ArrayList<>(mockUsers));
    
    // Mock getById methods
    when(firestoreService.getFoodById(anyInt())).thenAnswer(invocation -> {
      int id = invocation.getArgument(0);
      return mockFoods.stream()
          .filter(f -> f.getFoodId() == id)
          .findFirst()
          .orElse(null);
    });
    
    when(firestoreService.getRecipeById(anyInt())).thenAnswer(invocation -> {
      int id = invocation.getArgument(0);
      return mockRecipes.stream()
          .filter(r -> r.getRecipeId() == id)
          .findFirst()
          .orElse(null);
    });
    
    when(firestoreService.getUserById(anyInt())).thenAnswer(invocation -> {
      int id = invocation.getArgument(0);
      return mockUsers.stream()
          .filter(u -> u.getUserId() == id)
          .findFirst()
          .orElse(null);
    });
    
    // Mock query methods
    when(firestoreService.getFoodsByCategoryAndCalories(anyString(), anyInt()))
        .thenAnswer(invocation -> {
          String category = invocation.getArgument(0);
          int maxCalories = invocation.getArgument(1);
          return mockFoods.stream()
              .filter(f -> f.getCategory().equals(category) && f.getCalories() < maxCalories)
              .collect(java.util.stream.Collectors.toList());
        });
    when(firestoreService.getRecipesByCategoryAndCalories(anyString(), anyInt()))
        .thenAnswer(invocation -> {
          String category = invocation.getArgument(0);
          int maxCalories = invocation.getArgument(1);
          return mockRecipes.stream()
              .filter(r -> r.getCategory().equals(category) && r.getTotalCalories() <= maxCalories)
              .collect(java.util.stream.Collectors.toList());
        });
    when(firestoreService.getRecipesByCalories(anyInt()))
        .thenAnswer(invocation -> {
          int maxCalories = invocation.getArgument(0);
          return mockRecipes.stream()
              .filter(r -> r.getTotalCalories() <= maxCalories)
              .collect(java.util.stream.Collectors.toList());
        });
    
    // Mock add methods - actually add to the lists and check for duplicates
    when(firestoreService.addFood(any(Food.class))).thenAnswer(invocation -> {
      Food food = invocation.getArgument(0);
      boolean exists = mockFoods.stream().anyMatch(f -> f.getFoodId() == food.getFoodId());
      if (exists) {
        return false;
      }
      mockFoods.add(food);
      return true;
    });
    
    when(firestoreService.addRecipe(any(Recipe.class))).thenAnswer(invocation -> {
      Recipe recipe = invocation.getArgument(0);
      boolean exists = mockRecipes.stream().anyMatch(r -> r.getRecipeId() == recipe.getRecipeId());
      if (exists) {
        return false;
      }
      mockRecipes.add(recipe);
      return true;
    });
    
    when(firestoreService.addUser(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      boolean exists = mockUsers.stream().anyMatch(u -> u.getUserId() == user.getUserId());
      if (exists) {
        return false;
      }
      mockUsers.add(user);
      return true;
    });
    
    // Mock update methods - the objects are already updated in-place, just return true
    when(firestoreService.updateRecipe(any(Recipe.class))).thenReturn(true);
    
    when(firestoreService.updateUser(any(User.class))).thenAnswer(invocation -> {
      User updatedUser = invocation.getArgument(0);
      // Update the user in the list to reflect changes to likedRecipes
      for (int i = 0; i < mockUsers.size(); i++) {
        if (mockUsers.get(i).getUserId() == updatedUser.getUserId()) {
          mockUsers.set(i, updatedUser);
          break;
        }
      }
      return true;
    });
  }
  
  private long testStartTime;
  
  /**
   * runs before each test to log test start and record start time.
   *
   * @param testInfo current test information
   */
  @BeforeEach
  public void setUp(TestInfo testInfo) {
    testStartTime = System.currentTimeMillis();
    System.out.println("\nstarting test: " + testInfo.getDisplayName());
  }

  /**
   * runs after each test to log test completion and execution time.
   *
   * @param testInfo current test informationx
   */
  @AfterEach
  public void tearDown(TestInfo testInfo) {
    long executionTime = System.currentTimeMillis() - testStartTime;
    System.out.println("finished test: " + testInfo.getDisplayName() 
        + " (execution time: " + executionTime + "ms)\n");
  }

  @Test
  public void foodAlternativeEndpointReturnsLowerCalorieOptions() throws Exception {
    mockMvc.perform(get("/food/alternative").param("foodId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  public void foodAlternativeEndpointReturns404WhenFoodMissing() throws Exception {
    mockMvc.perform(get("/food/alternative").param("foodId", "999999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Food with ID 999999 not found."));
  }

  @Test
  public void foodAlternativeEndpointReturnsMessageWhenNoAlternatives() throws Exception {
    int foodId = findUnusedFoodId();
    String payload = foodPayload(foodId, "UniqueCategory", 5);

    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk());

    mockMvc.perform(get("/food/alternative").param("foodId", String.valueOf(foodId)))
        .andExpect(status().isOk())
        .andExpect(content().string(
            "No lower calorie alternatives found for food ID " + foodId + "."));
  }

  @Test
  public void foodAddEndpointCreatesFood() throws Exception {
    int foodId = findUnusedFoodId();
    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(foodPayload(foodId, "Snack", 120)))
        .andExpect(status().isOk())
        .andExpect(content().string("Food added successfully."));
  }

  @Test
  public void foodAddEndpointRejectsDuplicateIds() throws Exception {
    int foodId = findUnusedFoodId();
    String payload = foodPayload(foodId, "Snack", 150);

    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isOk());

    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            "Food with ID " + foodId + " already exists or is invalid."));
  }

  @Test
  public void foodAddEndpointAcceptsZeroCalorieFoods() throws Exception {
    int foodId = findUnusedFoodId();
    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(foodPayload(foodId, "ZeroCal", 0)))
        .andExpect(status().isOk())
        .andExpect(content().string("Food added successfully."));
  }

  @Test
  public void alternativeEndpointReturnsRecommendations() throws Exception {
    mockMvc.perform(get("/recipe/alternative").param("recipeId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topAlternatives").isArray())
        .andExpect(jsonPath("$.randomAlternatives").isArray());
  }

  @Test
  public void alternativeEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(get("/recipe/alternative").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
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
  public void totalCalorieEndpointReturnsAggregatedCalories() throws Exception {
    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recipeId").value(1001))
        .andExpect(jsonPath("$.totalCalories").value(488));
  }

  @Test
  public void totalCalorieEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void totalCalorieEndpointHandlesZeroIngredientRecipes() throws Exception {
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
  public void calorieBreakdownEndpointReturnsIngredientCalories() throws Exception {
    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.['Chicken Breast']").value(165));
  }

  @Test
  public void calorieBreakdownEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void calorieBreakdownEndpointReturnsEmptyMapForIngredientlessRecipe() throws Exception {
    int recipeId = findUnusedRecipeId();
    String payload = String.format("""
        {
          "recipeName": "Ingredientless Recipe",
          "recipeId": %d,
          "category": "Test",
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """, recipeId);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/recipe/calorieBreakdown").param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(content().json("{}"));
  }

  @Test
  public void addRecipeEndpointCreatesRecipe() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayload(recipeId, "Snack")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.recipeId").value(recipeId));
  }

  @Test
  public void addRecipeInvalidRequest() throws Exception {
    String json = """
      {"recipeId":0,"name":"Test","ingredients":[]}
        """;

    mockMvc.perform(post("/recipe/addRecipe")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void addRecipeEndpointRejectsDuplicateIds() throws Exception {
    int recipeId = findUnusedRecipeId();
    String payload = recipePayload(recipeId, "Lunch");

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Recipe with id already exists"))
        .andExpect(jsonPath("$.recipeId").value(recipeId));
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
  public void addRecipeEndpointAllowsNullCategory() throws Exception {
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
  public void viewRecipeEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(post("/recipe/viewRecipe").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void viewRecipeEndpointHandlesLargeViewCounts() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayloadWithCounts(recipeId, "Dinner", 1_000_000, 0)))
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
  public void likeRecipeEndpointReturns404ForNonExistentRecipe() throws Exception {
    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Recipe not found"));
  }

  @Test
  public void likeRecipeEndpointHandlesLargeLikeCounts() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayloadWithCounts(recipeId, "Dinner", 0, 5_000)))
        .andExpect(status().isCreated());

    int initialLikes = mockApiService.getRecipeById(recipeId).orElseThrow().getLikes();

    mockMvc.perform(post("/recipe/likeRecipe").param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk());

    int updatedLikes = mockApiService.getRecipeById(recipeId).orElseThrow().getLikes();
    Assertions.assertEquals(initialLikes + 1, updatedLikes);
  }

  @Test
  public void recommendHealthyEndpointReturnsRecommendations() throws Exception {
    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", "501")
            .param("calorieMax", "600"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  public void recommendHealthyEndpointReturns404ForMissingUser() throws Exception {
    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", "99999")
            .param("calorieMax", "600"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User with ID 99999 not found."));
  }

  @Test
  public void recommendHealthyEndpointHandlesUsersWithNoPreferences() throws Exception {
    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", "509")
            .param("calorieMax", "400"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  public void recommendEndpointReturnsRecommendations() throws Exception {
    mockMvc.perform(get("/user/recommend").param("userId", "501"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].recipeId").exists());
  }

  @Test
  public void recommendEndpointReturns404ForMissingUser() throws Exception {
    mockMvc.perform(get("/user/recommend").param("userId", "99999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User with ID 99999 not found."));
  }

  @Test
  public void recommendEndpointLimitsResultsToTenWhenOversubscribed() throws Exception {
    for (int i = 0; i < 12; i++) {
      int recipeId = findUnusedRecipeId();
      mockMvc.perform(post("/recipe/addRecipe")
              .contentType(MediaType.APPLICATION_JSON)
              .content(recipePayload(recipeId, "Dinner")))
          .andExpect(status().isCreated());
    }

    mockMvc.perform(get("/user/recommend").param("userId", "501"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(10));
  }

  @Test
  public void userLikeRecipeEndpointLikesNewRecipe() throws Exception {
    int recipeId = findUnusedRecipeId();
    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(recipePayload(recipeId, "Breakfast")))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", "509")
            .param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(content().string("Recipe liked successfully."));

    User user = mockApiService.findUserById(509);
    Assertions.assertTrue(user.getLikedRecipes().stream()
        .anyMatch(recipe -> recipe.getRecipeId() == recipeId));
  }

  @Test
  public void userLikeRecipeEndpointReturns400ForMissingEntities() throws Exception {
    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", "99999")
            .param("recipeId", "1001"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            "User with ID 99999 or recipe with ID 1001 not found, or recipe already liked."));
  }

  @Test
  public void userLikeRecipeEndpointAllowsIngredientlessRecipes() throws Exception {
    int recipeId = findUnusedRecipeId();
    String payload = String.format("""
        {
          "recipeName": "Zero Ingredient Dessert",
          "recipeId": %d,
          "category": "Dessert",
          "ingredients": [],
          "views": 0,
          "likes": 0
        }
        """, recipeId);

    mockMvc.perform(post("/recipe/addRecipe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/user/likeRecipe")
            .param("userId", "508")
            .param("recipeId", String.valueOf(recipeId)))
        .andExpect(status().isOk())
        .andExpect(content().string("Recipe liked successfully."));
  }

  @Test
  public void loggingVerificationForIndexEndpoint() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    mockMvc.perform(get("/index"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logsList = listAppender.list;
    boolean logFound = logsList.stream()
        .anyMatch(event -> event.getFormattedMessage().contains("endpoint called: GET /index"));

    logger.detachAppender(listAppender);
    Assertions.assertTrue(logFound, "Expected log message for GET /index not found");
  }

  @Test
  public void loggingVerificationForGetEndpoints() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    mockMvc.perform(get("/food/alternative").param("foodId", "1"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logsList = listAppender.list;
    boolean logFound = logsList.stream()
        .anyMatch(event -> event.getFormattedMessage()
            .contains("endpoint called: GET /food/alternative with foodId=1"));

    logger.detachAppender(listAppender);
    Assertions.assertTrue(logFound, 
        "Expected log message for GET /food/alternative not found");
  }

  @Test
  public void loggingVerificationForPostEndpoints() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    int foodId = findUnusedFoodId();
    mockMvc.perform(post("/food/addFood")
            .contentType(MediaType.APPLICATION_JSON)
            .content(foodPayload(foodId, "Test", 100)))
        .andExpect(status().isOk());

    List<ILoggingEvent> logsList = listAppender.list;
    boolean logFound = logsList.stream()
        .anyMatch(event -> event.getFormattedMessage()
            .contains("endpoint called: POST /food/addFood"));

    logger.detachAppender(listAppender);
    Assertions.assertTrue(logFound, 
        "Expected log message for POST /food/addFood not found");
  }

  @Test
  public void loggingVerificationForRecipeEndpoints() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    mockMvc.perform(get("/recipe/totalCalorie").param("recipeId", "1001"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logsList = listAppender.list;
    boolean logFound = logsList.stream()
        .anyMatch(event -> event.getFormattedMessage()
            .contains("endpoint called: GET /recipe/totalCalorie with recipeId=1001"));

    logger.detachAppender(listAppender);
    Assertions.assertTrue(logFound,
        "Expected log message for GET /recipe/totalCalorie not found");
  }

  @Test
  public void loggingVerificationForUserEndpoints() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    mockMvc.perform(get("/user/recommend").param("userId", "501"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logsList = listAppender.list;
    boolean logFound = logsList.stream()
        .anyMatch(event -> event.getFormattedMessage()
            .contains("endpoint called: GET /user/recommend with userId=501"));

    logger.detachAppender(listAppender);
    Assertions.assertTrue(logFound,
        "Expected log message for GET /user/recommend not found");
  }

  @Test
  public void loggingVerificationForMultipleParameters() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    mockMvc.perform(get("/user/recommendHealthy")
            .param("userId", "501")
            .param("calorieMax", "600"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logsList = listAppender.list;
    boolean logFound = logsList.stream()
        .anyMatch(event -> event.getFormattedMessage()
            .contains("endpoint called: GET /user/recommendHealthy")
            && event.getFormattedMessage().contains("userId=501")
            && event.getFormattedMessage().contains("calorieMax=600"));

    logger.detachAppender(listAppender);
    Assertions.assertTrue(logFound,
        "Expected log message for GET /user/recommendHealthy with parameters not found");
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

  private int findUnusedRecipeId() {
    int candidate = 6000;
    while (mockApiService.getRecipeById(candidate).isPresent()) {
      candidate++;
    }
    return candidate;
  }

  private int findUnusedFoodId() {
    int candidate = 10_000;
    while (mockApiService.findFoodById(candidate) != null) {
      candidate++;
    }
    return candidate;
  }
}
