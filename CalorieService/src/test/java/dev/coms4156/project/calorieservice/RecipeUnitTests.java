package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for the Recipe model class.
 */
public class RecipeUnitTests {

  private static Recipe recipe;
  private static ArrayList<Food> ingredients;

  /**
   * Set up test fixtures before each test.
   */
  @BeforeEach
  public void setUp() {
    ingredients = new ArrayList<>();
    ingredients.add(new Food("Chicken Breast", 1, 165, "Protein"));
    ingredients.add(new Food("Brown Rice", 2, 216, "Grain"));
    ingredients.add(new Food("Broccoli", 3, 55, "Vegetable"));

    recipe = new Recipe("Test Recipe", 100, "Dinner", ingredients, 10, 5, 436);
  }

  @Test
  public void testNoArgsConstructor() {
    Recipe emptyRecipe = new Recipe();
    assertEquals("", emptyRecipe.getRecipeName());
    assertEquals(0, emptyRecipe.getRecipeId());
    assertEquals("", emptyRecipe.getCategory());
    assertNotNull(emptyRecipe.getIngredients());
    assertEquals(0, emptyRecipe.getIngredients().size());
    assertEquals(0, emptyRecipe.getViews());
    assertEquals(0, emptyRecipe.getLikes());
  }

  @Test
  public void testFullConstructor() {
    assertEquals("Test Recipe", recipe.getRecipeName());
    assertEquals(100, recipe.getRecipeId());
    assertEquals("Dinner", recipe.getCategory());
    assertEquals(3, recipe.getIngredients().size());
    assertEquals(10, recipe.getViews());
    assertEquals(5, recipe.getLikes());
  }

  @Test
  public void testGetTotalCaloriesReturnsSumOfIngredients() {
    int expectedTotal = 165 + 216 + 55;
    assertEquals(expectedTotal, recipe.getTotalCalories());
  }

  @Test
  public void testGetTotalCaloriesReturnsZeroForEmptyIngredients() {
    Recipe emptyRecipe = new Recipe("Empty", 101, "Test", new ArrayList<>(), 0, 0, 0);
    assertEquals(0, emptyRecipe.getTotalCalories());
  }

  @Test
  public void testGetTotalCaloriesWithSingleIngredient() {
    ArrayList<Food> singleIngredient = new ArrayList<>();
    singleIngredient.add(new Food("Apple", 1, 95, "Fruit"));
    Recipe singleItemRecipe = new Recipe("Single", 102, "Snack", singleIngredient, 0, 0, 95);
    assertEquals(95, singleItemRecipe.getTotalCalories());
  }

  @Test
  public void testIncrementViewsIncreasesCountByOne() {
    int initialViews = recipe.getViews();
    recipe.incrementViews();
    assertEquals(initialViews + 1, recipe.getViews());
  }

  @Test
  public void testIncrementViewsMultipleTimes() {
    final int initialViews = recipe.getViews();
    recipe.incrementViews();
    recipe.incrementViews();
    recipe.incrementViews();
    assertEquals(initialViews + 3, recipe.getViews());
  }

  @Test
  public void testIncrementLikesIncreasesCountByOne() {
    int initialLikes = recipe.getLikes();
    recipe.incrementLikes();
    assertEquals(initialLikes + 1, recipe.getLikes());
  }

  @Test
  public void testIncrementLikesMultipleTimes() {
    final int initialLikes = recipe.getLikes();
    recipe.incrementLikes();
    recipe.incrementLikes();
    recipe.incrementLikes();
    assertEquals(initialLikes + 3, recipe.getLikes());
  }

  @Test
  public void testEqualsReturnsTrueForSameObject() {
    assertTrue(recipe.equals(recipe));
  }

  @Test
  public void testEqualsReturnsTrueForSameRecipeId() {
    Recipe recipe2 = new Recipe("Different Name", 100, "Lunch", new ArrayList<>(), 20, 15, 436);
    assertTrue(recipe.equals(recipe2));
  }

  @Test
  public void testEqualsReturnsFalseForDifferentRecipeId() {
    Recipe recipe2 = new Recipe("Test Recipe", 101, "Dinner", ingredients, 10, 5, 436);
    assertFalse(recipe.equals(recipe2));
  }

  @Test
  public void testEqualsReturnsFalseForNull() {
    assertFalse(recipe.equals(null));
  }

  @Test
  public void testEqualsReturnsFalseForDifferentClass() {
    assertFalse(recipe.equals("Not a Recipe"));
  }

  @Test
  public void testHashCodeConsistentWithEquals() {
    Recipe recipe2 = new Recipe("Different Name", 100, "Lunch", new ArrayList<>(), 20, 15, 436);
    assertEquals(recipe.hashCode(), recipe2.hashCode());
  }

  @Test
  public void testHashCodeDifferentForDifferentRecipeIds() {
    Recipe recipe2 = new Recipe("Test Recipe", 101, "Dinner", ingredients, 10, 5, 436);
    assertNotEquals(recipe.hashCode(), recipe2.hashCode());
  }

  @Test
  public void testCompareToReturnZeroForEqualRecipeIds() {
    Recipe recipe2 = new Recipe("Different Name", 100, "Lunch", new ArrayList<>(), 20, 15, 436);
    assertEquals(0, recipe.compareTo(recipe2));
  }

  @Test
  public void testCompareToReturnNegativeForSmallerRecipeId() {
    Recipe recipe2 = new Recipe("Another Recipe", 101, "Dinner", ingredients, 10, 5, 436);
    assertTrue(recipe.compareTo(recipe2) < 0);
  }

  @Test
  public void testCompareToReturnPositiveForLargerRecipeId() {
    Recipe recipe2 = new Recipe("Another Recipe", 99, "Dinner", ingredients, 10, 5, 436);
    assertTrue(recipe.compareTo(recipe2) > 0);
  }

  @Test
  public void testSetIngredientsHandlesNull() {
    recipe.setIngredients(null);
    assertNotNull(recipe.getIngredients());
    assertEquals(0, recipe.getIngredients().size());
  }

  @Test
  public void testSetIngredientsHandlesValidList() {
    ArrayList<Food> newIngredients = new ArrayList<>();
    newIngredients.add(new Food("Salmon", 4, 208, "Protein"));
    recipe.setIngredients(newIngredients);
    assertEquals(1, recipe.getIngredients().size());
    assertEquals("Salmon", recipe.getIngredients().get(0).getFoodName());
  }

  @Test
  public void testSetRecipeName() {
    recipe.setRecipeName("Updated Recipe");
    assertEquals("Updated Recipe", recipe.getRecipeName());
  }

  @Test
  public void testSetRecipeId() {
    recipe.setRecipeId(200);
    assertEquals(200, recipe.getRecipeId());
  }

  @Test
  public void testSetCategory() {
    recipe.setCategory("Lunch");
    assertEquals("Lunch", recipe.getCategory());
  }

  @Test
  public void testSetViews() {
    recipe.setViews(100);
    assertEquals(100, recipe.getViews());
  }

  @Test
  public void testSetLikes() {
    recipe.setLikes(50);
    assertEquals(50, recipe.getLikes());
  }

  @Test
  public void testToStringContainsRecipeInfo() {
    String result = recipe.toString();
    assertTrue(result.contains("100"));
    assertTrue(result.contains("Test Recipe"));
    assertTrue(result.contains("10"));
    assertTrue(result.contains("5"));
  }

  @Test
  public void testGetTotalCaloriesWithZeroCalorieIngredients() {
    ArrayList<Food> zeroCalIngredients = new ArrayList<>();
    zeroCalIngredients.add(new Food("Water", 1, 0, "Beverage"));
    zeroCalIngredients.add(new Food("Ice", 2, 0, "Beverage"));
    Recipe zeroCalRecipe = new Recipe("Zero Cal", 103, "Beverage", zeroCalIngredients, 0, 0, 0);
    assertEquals(0, zeroCalRecipe.getTotalCalories());
  }

  @Test
  public void testRecipeWithNullCategory() {
    Recipe nullCategoryRecipe = new Recipe("No Category", 104, null, ingredients, 0, 0, 0);
    assertEquals(null, nullCategoryRecipe.getCategory());
  }

  @Test
  public void testIncrementViewsFromZero() {
    Recipe newRecipe = new Recipe();
    assertEquals(0, newRecipe.getViews());
    newRecipe.incrementViews();
    assertEquals(1, newRecipe.getViews());
  }

  @Test
  public void testIncrementLikesFromZero() {
    Recipe newRecipe = new Recipe();
    assertEquals(0, newRecipe.getLikes());
    newRecipe.incrementLikes();
    assertEquals(1, newRecipe.getLikes());
  }

  /**
   * Clean up all test variables after all tests.
   */
  @AfterAll
  public static void tearDownRecipeAfterTesting() {
    recipe = null;
    ingredients = null;
  }
}
