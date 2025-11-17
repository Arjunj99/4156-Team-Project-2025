package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Tests for MockApiService data persistence functionality.
 * This test file specifically tests data loading and saving without test mode.
 */
@SpringBootTest
public class MockApiServiceDataPersistenceTests {

  @MockBean
  private FirestoreService firestoreService;

  private MockApiService service;

  @BeforeEach
  public void setUp() throws ExecutionException, InterruptedException {
    // Set up mock to return some initial data
    ArrayList<Food> initialFoods = new ArrayList<>();
    ArrayList<Recipe> initialRecipes = new ArrayList<>();
    ArrayList<User> initialUsers = new ArrayList<>();
    
    when(firestoreService.getAllFoods()).thenReturn(initialFoods);
    when(firestoreService.getAllRecipes()).thenReturn(initialRecipes);
    when(firestoreService.getAllUsers()).thenReturn(initialUsers);
    when(firestoreService.getFoodById(anyInt())).thenReturn(null);
    when(firestoreService.getRecipeById(anyInt())).thenReturn(null);
    when(firestoreService.getUserById(anyInt())).thenReturn(null);
    when(firestoreService.addFood(any(Food.class))).thenAnswer(invocation -> {
      Food food = invocation.getArgument(0);
      initialFoods.add(food);
      return true;
    });
    when(firestoreService.addRecipe(any(Recipe.class))).thenAnswer(invocation -> {
      Recipe recipe = invocation.getArgument(0);
      initialRecipes.add(recipe);
      return true;
    });
    when(firestoreService.addUser(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      initialUsers.add(user);
      return true;
    });
    when(firestoreService.getFoodsByCategoryAndCalories(any(), anyInt()))
        .thenReturn(new ArrayList<>());
    when(firestoreService.getRecipesByCategoryAndCalories(any(), anyInt()))
        .thenReturn(new ArrayList<>());
    when(firestoreService.getRecipesByCalories(anyInt())).thenReturn(new ArrayList<>());
    when(firestoreService.updateRecipe(any(Recipe.class))).thenReturn(true);
    when(firestoreService.updateUser(any(User.class))).thenReturn(true);
    
    service = new MockApiService(firestoreService);
    service.setTestMode(false);
  }

  @AfterEach
  public void tearDown() {
    service.cleanupTestData(90001);
  }

  @Test
  public void testDataSavesToFiles() {
    // Test data loads
    assertFalse(service.getFoods().isEmpty());
    assertFalse(service.getRecipes().isEmpty());
    assertFalse(service.getUsers().isEmpty());
    
    // Add test data
    Food testFood = new Food("Test Food", 90001, 300, "Test");
    Recipe testRecipe = new Recipe("Test Recipe", 90001, "Test", 
        new ArrayList<>(), 0, 0, 600);
    User testUser = new User("Test User", 90001, new ArrayList<>());
    
    assertTrue(service.addFood(testFood));
    assertTrue(service.addRecipe(testRecipe));
    assertTrue(service.addUser(testUser));
    
    // Check if data was added to the service
    // Note: With Firestore, we're testing that the service correctly calls FirestoreService
    // The actual persistence is handled by FirestoreService, which is mocked here
    assertTrue(service.getFoods().stream()
        .anyMatch(food -> food.getFoodId() == 90001), "Food should be added");
    assertTrue(service.getRecipes().stream()
        .anyMatch(recipe -> recipe.getRecipeId() == 90001), "Recipe should be added");
    assertTrue(service.getUsers().stream()
        .anyMatch(user -> user.getUserId() == 90001), "User should be added");
  }
}
