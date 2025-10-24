package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Recipe model class.
 */
public class RecipeUnitTests {

  private Recipe recipe;
  private ArrayList<Food> ingredients;

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
  public void testFullConstructorInvalidInputTest() {
    try {
      new Recipe("Test", -1, "Test", new ArrayList<>(), 0, 0, 0);
      assertTrue(false, "Should throw exception for negative recipe ID");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
    try {
      new Recipe("Test", 1, "Test", new ArrayList<>(), -1, 0, 0);
      assertTrue(false, "Should throw exception for negative views");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
    try {
      new Recipe("Test", 1, "Test", new ArrayList<>(), 0, -1, 0);
      assertTrue(false, "Should throw exception for negative likes");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
  }

  @Test
  public void testFullConstructorAtypicalInputTest() {
    try {
      new Recipe("Test", Integer.MIN_VALUE, "Test", new ArrayList<>(), 0, 0, 0);
      assertTrue(false, "Should throw exception for Integer.MIN_VALUE");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
    Recipe extremeRecipe = new Recipe("Extreme", Integer.MAX_VALUE, "Test", 
        new ArrayList<>(), Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
    assertEquals(Integer.MAX_VALUE, extremeRecipe.getRecipeId());
    assertEquals(Integer.MAX_VALUE, extremeRecipe.getViews());
    assertEquals(Integer.MAX_VALUE, extremeRecipe.getLikes());
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

  @Test
  public void incrementViewsValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.incrementViews();
    assertEquals(1, testRecipe.getViews());
  }
  
  @Test
  public void incrementViewsInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setViews(Integer.MAX_VALUE);
    testRecipe.incrementViews();
    assertEquals(Integer.MIN_VALUE, testRecipe.getViews());
  }
  
  @Test
  public void incrementViewsAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setViews(0);
    testRecipe.incrementViews();
    assertEquals(1, testRecipe.getViews());
    
    testRecipe.setViews(999);
    testRecipe.incrementViews();
    assertEquals(1000, testRecipe.getViews());
  }

  @Test
  public void setRecipeNameValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setRecipeName("Valid Recipe Name");
    assertEquals("Valid Recipe Name", testRecipe.getRecipeName());
  }
  
  @Test
  public void setRecipeNameInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setRecipeName(null);
    assertEquals(null, testRecipe.getRecipeName());
  }
  
  @Test
  public void setRecipeNameAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setRecipeName("");
    assertEquals("", testRecipe.getRecipeName());
    
    testRecipe.setRecipeName("   ");
    assertEquals("   ", testRecipe.getRecipeName());
    
    testRecipe.setRecipeName("Recipe-With-Special@Characters#123");
    assertEquals("Recipe-With-Special@Characters#123", testRecipe.getRecipeName());
  }

  @Test
  public void setRecipeIdValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setRecipeId(123);
    assertEquals(123, testRecipe.getRecipeId());
  }
  
  @Test
  public void setRecipeIdInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    try {
      testRecipe.setRecipeId(-1);
      assertTrue(false, "Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true, "IllegalArgumentException thrown as expected");
    }
  }
  
  @Test
  public void setRecipeIdAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setRecipeId(0);
    assertEquals(0, testRecipe.getRecipeId());
    
    testRecipe.setRecipeId(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testRecipe.getRecipeId());
    
  }

  @Test
  public void setCategoryValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setCategory("Valid Category");
    assertEquals("Valid Category", testRecipe.getCategory());
  }
  
  @Test
  public void setCategoryInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setCategory(null);
    assertEquals(null, testRecipe.getCategory());
  }
  
  @Test
  public void setCategoryAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setCategory("");
    assertEquals("", testRecipe.getCategory());
    
    testRecipe.setCategory("   ");
    assertEquals("   ", testRecipe.getCategory());
    
    testRecipe.setCategory("Category-With-Special@Characters#123");
    assertEquals("Category-With-Special@Characters#123", testRecipe.getCategory());
  }

  @Test
  public void setIngredientsValidInputTest() {
    Recipe testRecipe = new Recipe();
    ArrayList<Food> ingredients = new ArrayList<>();
    ingredients.add(new Food("Test Ingredient", 1, 100, "Test"));
    testRecipe.setIngredients(ingredients);
    assertEquals(1, testRecipe.getIngredients().size());
  }
  
  @Test
  public void setIngredientsInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setIngredients(null);
    assertNotNull(testRecipe.getIngredients());
    assertEquals(0, testRecipe.getIngredients().size());
  }
  
  @Test
  public void setIngredientsAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setIngredients(new ArrayList<>());
    assertEquals(0, testRecipe.getIngredients().size());
    
    ArrayList<Food> manyIngredients = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      manyIngredients.add(new Food("Ingredient" + i, i, i * 10, "Test"));
    }
    testRecipe.setIngredients(manyIngredients);
    assertEquals(100, testRecipe.getIngredients().size());
  }

  @Test
  public void setViewsValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setViews(150);
    assertEquals(150, testRecipe.getViews());
  }
  
  @Test
  public void setViewsInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    try {
      testRecipe.setViews(-1);
      assertTrue(false, "Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true, "IllegalArgumentException thrown as expected");
    }
  }
  
  @Test
  public void setViewsAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setViews(0);
    assertEquals(0, testRecipe.getViews());
    
    testRecipe.setViews(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testRecipe.getViews());
    
  }

  @Test
  public void getLikesValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setLikes(25);
    assertEquals(25, testRecipe.getLikes());
  }
  
  @Test
  public void getLikesInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    try {
      testRecipe.setLikes(-5);
      assertTrue(false, "Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true, "IllegalArgumentException thrown as expected");
    }
  }
  
  @Test
  public void getLikesAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setLikes(0);
    assertEquals(0, testRecipe.getLikes());
    
    testRecipe.setLikes(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testRecipe.getLikes());
    
  }

  @Test
  public void setLikesValidInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setLikes(75);
    assertEquals(75, testRecipe.getLikes());
  }
  
  @Test
  public void setLikesInvalidInputTest() {
    Recipe testRecipe = new Recipe();
    try {
      testRecipe.setLikes(-10);
      assertTrue(false, "Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true, "IllegalArgumentException thrown as expected");
    }
  }
  
  @Test
  public void setLikesAtypicalInputTest() {
    Recipe testRecipe = new Recipe();
    testRecipe.setLikes(0);
    assertEquals(0, testRecipe.getLikes());
    
    testRecipe.setLikes(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testRecipe.getLikes());
    
  }

  @Test
  public void compareToValidInputTest() {
    Recipe recipe1 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    Recipe recipe2 = new Recipe("Recipe2", 2, "Category", new ArrayList<>(), 0, 0, 200);
    assertTrue(recipe1.compareTo(recipe2) < 0);
    assertTrue(recipe2.compareTo(recipe1) > 0);
    assertTrue(recipe1.compareTo(recipe1) == 0);
  }
  
  @Test
  public void compareToInvalidInputTest() {
    Recipe recipe1 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    try {
      recipe1.compareTo(null);
      assertTrue(false, "Expected NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(true, "NullPointerException thrown as expected");
    }
  }
  
  @Test
  public void compareToAtypicalInputTest() {
    Recipe recipe1 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    Recipe recipe2 = new Recipe("Recipe2", 0, "Category", new ArrayList<>(), 0, 0, 200);
    Recipe recipe3 = new Recipe("Recipe3", Integer.MAX_VALUE, "Category", new ArrayList<>(), 0, 0, 
        300);
    Recipe recipe4 = new Recipe("Different Name", 1, "Different Category", 
        new ArrayList<>(), 999, 999, 999);
    
    assertTrue(recipe1.compareTo(recipe2) > 0);  // 1 > 0
    assertTrue(recipe1.compareTo(recipe3) < 0);  // 1 < Integer.MAX_VALUE
    assertTrue(recipe1.compareTo(recipe4) == 0); // 1 == 1
  }

  @Test
  public void equalsValidInputTest() {
    Recipe recipe1 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    Recipe recipe2 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    Recipe recipe3 = new Recipe("Recipe2", 2, "Category", new ArrayList<>(), 0, 0, 200);
    
    assertTrue(recipe1.equals(recipe2));
    assertFalse(recipe1.equals(recipe3));
    assertTrue(recipe1.equals(recipe1));
  }
  
  @Test
  public void equalsInvalidInputTest() {
    Recipe recipe1 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    assertFalse(recipe1.equals(null));
    assertFalse(recipe1.equals("Not a Recipe"));
    assertFalse(recipe1.equals(Integer.valueOf(1)));
  }
  
  @Test
  public void equalsAtypicalInputTest() {
    Recipe recipe1 = new Recipe("Recipe1", 1, "Category", new ArrayList<>(), 0, 0, 100);
    Recipe recipe2 = new Recipe("", 1, "", new ArrayList<>(), 0, 0, 0);
    Recipe recipe3 = new Recipe(null, 1, null, new ArrayList<>(), 0, 0, 0);
    Recipe recipe4 = new Recipe("Different Name", 1, "Different Category", 
        new ArrayList<>(), 999, 999, 999);
    
    assertTrue(recipe1.equals(recipe2));
    assertTrue(recipe1.equals(recipe3));
    assertTrue(recipe1.equals(recipe4));
    
    Recipe recipe5 = new Recipe("Test", 0, "Test", new ArrayList<>(), 0, 0, 0);
    Recipe recipe6 = new Recipe("Test", 0, "Different", new ArrayList<>(), 100, 100, 100);
    assertTrue(recipe5.equals(recipe6));
  }

}
