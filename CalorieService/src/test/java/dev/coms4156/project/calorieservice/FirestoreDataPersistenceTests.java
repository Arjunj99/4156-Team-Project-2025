package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Client;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests for FirestoreService data persistence functionality.
 * This test adds objects to the database, verifies they exist, and then removes them.
 */
@SpringBootTest
public class FirestoreDataPersistenceTests {

  @Autowired
  private FirestoreService firestoreService;

  // Use high IDs to avoid conflicts with production data
  private static final int TEST_FOOD_ID = 999991;
  private static final int TEST_RECIPE_ID = 999991;
  private static final int TEST_USER_ID = 999991;
  private static final int TEST_INGREDIENT_FOOD_ID_1 = 999992;
  private static final int TEST_INGREDIENT_FOOD_ID_2 = 999993;

  /**
   * Cleans up any existing test data before each test.
   */
  @BeforeEach
  public void setUp() throws ExecutionException, InterruptedException {
    cleanupTestData();
  }

  /**
   * Cleans up test data after each test.
   */
  @AfterEach
  public void tearDown() throws ExecutionException, InterruptedException {
    cleanupTestData();
  }

  /**
   * Helper method to clean up all test data.
   */
  private void cleanupTestData() throws ExecutionException, InterruptedException {
    try {
      firestoreService.deleteClient(TEST_USER_ID);
    } catch (Exception e) {
      // Ignore if doesn't exist
    }
    try {
      firestoreService.deleteRecipe(TEST_RECIPE_ID);
    } catch (Exception e) {
      // Ignore if doesn't exist
    }
    try {
      firestoreService.deleteFood(TEST_INGREDIENT_FOOD_ID_2);
    } catch (Exception e) {
      // Ignore if doesn't exist
    }
    try {
      firestoreService.deleteFood(TEST_INGREDIENT_FOOD_ID_1);
    } catch (Exception e) {
      // Ignore if doesn't exist
    }
    try {
      firestoreService.deleteFood(TEST_FOOD_ID);
    } catch (Exception e) {
      // Ignore if doesn't exist
    }
  }

  /**
   * Tests complete data persistence workflow: add objects, verify they exist, and delete them.
   */
  @Test
  public void testDataPersistence() throws ExecutionException, InterruptedException {
    // Add a food
    Food testFood = new Food("Persistence Test Food", TEST_FOOD_ID, 250, "Test");
    assertTrue(firestoreService.addFood(testFood), "Food should be added successfully");

    // Verify food exists
    Food retrievedFood = firestoreService.getFoodById(TEST_FOOD_ID);
    assertNotNull(retrievedFood, "Food should exist after adding");
    assertEquals("Persistence Test Food", retrievedFood.getFoodName());
    assertEquals(250, retrievedFood.getCalories());
    assertEquals("Test", retrievedFood.getCategory());

    // Add ingredient foods for recipe
    Food ingredient1 = new Food("Test Ingredient 1", TEST_INGREDIENT_FOOD_ID_1, 100, "Test");
    Food ingredient2 = new Food("Test Ingredient 2", TEST_INGREDIENT_FOOD_ID_2, 150, "Test");
    assertTrue(firestoreService.addFood(ingredient1), "Ingredient 1 should be added");
    assertTrue(firestoreService.addFood(ingredient2), "Ingredient 2 should be added");

    // Add a recipe
    ArrayList<Food> ingredients = new ArrayList<>();
    ingredients.add(ingredient1);
    ingredients.add(ingredient2);
    Recipe testRecipe = new Recipe("Persistence Test Recipe", TEST_RECIPE_ID, 
        "Test", ingredients, 10, 5, 250);
    assertTrue(firestoreService.addRecipe(testRecipe), "Recipe should be added successfully");

    // Verify recipe exists
    Recipe retrievedRecipe = firestoreService.getRecipeById(TEST_RECIPE_ID);
    assertNotNull(retrievedRecipe, "Recipe should exist after adding");
    assertEquals("Persistence Test Recipe", retrievedRecipe.getRecipeName());
    assertEquals(250, retrievedRecipe.getTotalCalories());
    assertEquals(2, retrievedRecipe.getIngredients().size());

    // Add a client
    Client testClient = new Client("Persistence Test Client", TEST_USER_ID);
    assertTrue(firestoreService.addClient(testClient), "Client should be added successfully");

    // Verify client exists
    Client retrievedClient = firestoreService.getClientById(TEST_USER_ID);
    assertNotNull(retrievedClient, "Client should exist after adding");
    assertEquals("Persistence Test Client", retrievedClient.getClientname());
    assertEquals(TEST_USER_ID, retrievedClient.getClientId());

    // Delete all objects
    assertTrue(firestoreService.deleteClient(TEST_USER_ID), "Client should be deleted");
    assertTrue(firestoreService.deleteRecipe(TEST_RECIPE_ID), "Recipe should be deleted");
    assertTrue(
        firestoreService.deleteFood(TEST_INGREDIENT_FOOD_ID_2),
        "Ingredient 2 should be deleted");
    assertTrue(
        firestoreService.deleteFood(TEST_INGREDIENT_FOOD_ID_1),
        "Ingredient 1 should be deleted");
    assertTrue(firestoreService.deleteFood(TEST_FOOD_ID), "Food should be deleted");

    // Verify all objects are deleted
    assertNull(
        firestoreService.getClientById(TEST_USER_ID), "Client should not exist after deletion");
    assertNull(
        firestoreService.getRecipeById(TEST_RECIPE_ID),
        "Recipe should not exist after deletion");
    assertNull(
        firestoreService.getFoodById(TEST_FOOD_ID), "Food should not exist after deletion");
    assertNull(
        firestoreService.getFoodById(TEST_INGREDIENT_FOOD_ID_1),
        "Ingredient 1 should not exist after deletion");
    assertNull(
        firestoreService.getFoodById(TEST_INGREDIENT_FOOD_ID_2),
        "Ingredient 2 should not exist after deletion");
  }
}
