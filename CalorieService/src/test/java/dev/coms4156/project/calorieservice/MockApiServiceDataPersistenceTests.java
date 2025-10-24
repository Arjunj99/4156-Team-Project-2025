package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests for MockApiService data persistence functionality.
 * This test file specifically tests data loading and saving without test mode.
 */
@SpringBootTest
public class MockApiServiceDataPersistenceTests {

  private MockApiService service;

  @BeforeEach
  public void setUp() {
    service = new MockApiService();
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
    
    //initialize new service to test if data was added
    MockApiService newService = new MockApiService();
    
    assertTrue(newService.getFoods().stream()
        .anyMatch(food -> food.getFoodId() == 90001), "Food should be in file");
    assertTrue(newService.getRecipes().stream()
        .anyMatch(recipe -> recipe.getRecipeId() == 90001), "Recipe should be in file");
    assertTrue(newService.getUsers().stream()
        .anyMatch(user -> user.getUserId() == 90001), "User should be in file");
  }
}
