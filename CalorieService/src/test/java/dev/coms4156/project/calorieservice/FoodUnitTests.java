package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * This class contains the unit tests for the Food class.
 */
public class FoodUnitTests {

  public static Food food;
  public static Food food1;
  public static Food food2;
  public static Food food3;

  /**
   * This method sets up our testing variables.
   */
  @BeforeAll
  public static void setUpFoodForTesting() {
    food = new Food("Apple", 1, 80, "Fruit");
    food1 = new Food("Banana", 2, 105, "Fruit");
    food2 = new Food("Chicken", 3, 200, "Protein");
    food3 = new Food("Rice", 4, 130, "Grain");
  }

  @Test
  public void equalsBothAreTheSameTest() {
    Food cmpFood = food;
    assertEquals(cmpFood, food);
  }

  @Test
  public void equalsBothAreDifferentFoodsTest() {
    Food cmpFood = food1;
    assertFalse(food.equals(cmpFood));
  }

  @Test
  public void equalsObjectIsNullTest() {
    assertFalse(food.equals(null));
  }

  @Test
  public void equalsBothAreDifferentClassTest() {
    String test = "test";
    assertFalse(food.equals(test));
  }

  @Test
  public void equalsBothAreSameClassTest() {
    Food cmpFood = food1;
    assertTrue(cmpFood.equals(food1));
  }

  @Test
  public void equalsSameFoodIdTest() {
    Food cmpFood = new Food("Different Name", 1, 50, "Different Category");
    assertTrue(food.equals(cmpFood));
  }

  @Test
  public void compareToLessThanTest() {
    assertTrue(food.compareTo(food1) < 0);
  }

  @Test
  public void compareToEqualTest() {
    assertTrue(food.compareTo(food) == 0);
  }

  @Test
  public void compareToGreaterThanTest() {
    assertTrue(food2.compareTo(food1) > 0);
  }

  @Test
  public void toStringTest() {
    String expected = "(1)\tApple - 80 cal";
    assertEquals(expected, food.toString());
  }

  @Test
  public void noArgsConstructorTest() {
    Food emptyFood = new Food();
    assertEquals("", emptyFood.getFoodName());
    assertEquals(0, emptyFood.getFoodId());
    assertEquals(0, emptyFood.getCalories());
    assertEquals("", emptyFood.getCategory());
  }

  @Test
  public void completeArgsConstructorTest() {
    Food testFood = new Food("Test Food", 999, 150, "Test Category");
    assertEquals("Test Food", testFood.getFoodName());
    assertEquals(999, testFood.getFoodId());
    assertEquals(150, testFood.getCalories());
    assertEquals("Test Category", testFood.getCategory());
  }

  @Test
  public void completeArgsConstructorInvalidInputTest() {
    try {
      new Food("Test", -1, 100, "Test");
      assertTrue(false, "Should throw exception for negative food ID");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
    try {
      new Food("Test", 1, -100, "Test");
      assertTrue(false, "Should throw exception for negative calories");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
  }

  @Test
  public void completeArgsConstructorAtypicalInputTest() {
    try {
      new Food("Test", Integer.MIN_VALUE, 100, "Test");
      assertTrue(false, "Should throw exception for Integer.MIN_VALUE");
    } catch (IllegalArgumentException e) {
      // Expected exception
    }
    Food extremeFood = new Food("Extreme", Integer.MAX_VALUE, 9999, "Test");
    assertEquals(Integer.MAX_VALUE, extremeFood.getFoodId());
    assertEquals(9999, extremeFood.getCalories());
  }

  @Test
  public void gettersAndSettersTest() {
    Food testFood = new Food();
    
    testFood.setFoodName("New Name");
    assertEquals("New Name", testFood.getFoodName());
    
    testFood.setFoodId(555);
    assertEquals(555, testFood.getFoodId());
    
    testFood.setCalories(300);
    assertEquals(300, testFood.getCalories());
    
    testFood.setCategory("New Category");
    assertEquals("New Category", testFood.getCategory());
  }

  @Test
  public void compareToWithSameIdTest() {
    Food sameIdFood = new Food("Different", 1, 100, "Different");
    assertTrue(food.compareTo(sameIdFood) == 0);
  }

  @Test
  public void compareToWithDifferentIdTest() {
    assertTrue(food3.compareTo(food) > 0);
    assertTrue(food.compareTo(food3) < 0);
  }

  @Test
  public void setFoodNameValidInputTest() {
    Food testFood = new Food();
    testFood.setFoodName("Valid Food Name");
    assertEquals("Valid Food Name", testFood.getFoodName());
  }
  
  @Test
  public void setFoodNameInvalidInputTest() {
    Food testFood = new Food();
    testFood.setFoodName(null);
    assertEquals(null, testFood.getFoodName());
  }
  
  @Test
  public void setFoodNameAtypicalInputTest() {
    Food testFood = new Food();
    testFood.setFoodName("");
    assertEquals("", testFood.getFoodName());
    
    testFood.setFoodName("   ");
    assertEquals("   ", testFood.getFoodName());
    
    testFood.setFoodName("Food-With-Special@Characters#123");
    assertEquals("Food-With-Special@Characters#123", testFood.getFoodName());
  }

  @Test
  public void setFoodIdValidInputTest() {
    Food testFood = new Food();
    testFood.setFoodId(123);
    assertEquals(123, testFood.getFoodId());
  }
  
  @Test
  public void setFoodIdInvalidInputTest() {
    Food testFood = new Food();
    try {
      testFood.setFoodId(-1);
      assertTrue(false, "Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true, "IllegalArgumentException thrown as expected");
    }
  }
  
  @Test
  public void setFoodIdAtypicalInputTest() {
    Food testFood = new Food();
    testFood.setFoodId(0);
    assertEquals(0, testFood.getFoodId());
    
    testFood.setFoodId(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testFood.getFoodId());
    
  }

  @Test
  public void setCaloriesValidInputTest() {
    Food testFood = new Food();
    testFood.setCalories(150);
    assertEquals(150, testFood.getCalories());
  }
  
  @Test
  public void setCaloriesInvalidInputTest() {
    Food testFood = new Food();
    try {
      testFood.setCalories(-50);
      assertTrue(false, "Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true, "IllegalArgumentException thrown as expected");
    }
  }
  
  @Test
  public void setCaloriesAtypicalInputTest() {
    Food testFood = new Food();
    testFood.setCalories(0);
    assertEquals(0, testFood.getCalories());
    
    testFood.setCalories(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testFood.getCalories());
    
    testFood.setCalories(999999);
    assertEquals(999999, testFood.getCalories());
  }

  @Test
  public void setCategoryValidInputTest() {
    Food testFood = new Food();
    testFood.setCategory("Valid Category");
    assertEquals("Valid Category", testFood.getCategory());
  }
  
  @Test
  public void setCategoryInvalidInputTest() {
    Food testFood = new Food();
    testFood.setCategory(null);
    assertEquals(null, testFood.getCategory());
  }
  
  @Test
  public void setCategoryAtypicalInputTest() {
    Food testFood = new Food();
    testFood.setCategory("");
    assertEquals("", testFood.getCategory());
    
    testFood.setCategory("   ");
    assertEquals("   ", testFood.getCategory());
    
    testFood.setCategory("Category-With-Special@Characters#123");
    assertEquals("Category-With-Special@Characters#123", testFood.getCategory());
  }

  @Test
  public void compareToValidInputTest() {
    Food food1 = new Food("Apple", 1, 80, "Fruit");
    Food food2 = new Food("Banana", 2, 105, "Fruit");
    assertTrue(food1.compareTo(food2) < 0);
    assertTrue(food2.compareTo(food1) > 0);
    assertTrue(food1.compareTo(food1) == 0);
  }
  
  @Test
  public void compareToInvalidInputTest() {
    Food food1 = new Food("Apple", 1, 80, "Fruit");
    try {
      food1.compareTo(null);
      assertTrue(false, "Expected NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(true, "NullPointerException thrown as expected");
    }
  }
  
  @Test
  public void compareToAtypicalInputTest() {
    Food food1 = new Food("Apple", 1, 80, "Fruit");
    Food food2 = new Food("Banana", 0, 105, "Fruit");
    Food food3 = new Food("Cherry", Integer.MAX_VALUE, 50, "Fruit");
    Food food4 = new Food("Date", 2, Integer.MAX_VALUE, "Fruit");
    
    assertTrue(food1.compareTo(food2) > 0);
    assertTrue(food1.compareTo(food3) < 0); 
    assertTrue(food1.compareTo(food4) < 0); 
  }

  @Test
  public void equalsValidInputTest() {
    Food food1 = new Food("Apple", 1, 80, "Fruit");
    Food food2 = new Food("Apple", 1, 80, "Fruit");
    Food food3 = new Food("Banana", 2, 105, "Fruit");
    
    assertTrue(food1.equals(food2));
    assertFalse(food1.equals(food3));
    assertTrue(food1.equals(food1));
  }
  
  @Test
  public void equalsInvalidInputTest() {
    Food food1 = new Food("Apple", 1, 80, "Fruit");
    assertFalse(food1.equals(null));
    assertFalse(food1.equals("Not a Food"));
    assertFalse(food1.equals(Integer.valueOf(1)));
  }
  
  @Test
  public void equalsAtypicalInputTest() {
    Food food1 = new Food("Apple", 1, 80, "Fruit");
    Food food2 = new Food("", 1, 0, "");
    Food food3 = new Food(null, 1, 0, null);
    Food food5 = new Food("Test", Integer.MAX_VALUE, 100, "Test");
    
    assertTrue(food1.equals(food2));
    assertTrue(food1.equals(food3));
    assertFalse(food1.equals(food5));
    Food food6 = new Food("Test", 0, 0, "");
    assertFalse(food1.equals(food6));
  }

  /**
   * Clean up all test variables after all tests.
   */
  @AfterAll
  public static void tearDownFoodAfterTesting() {
    food = null;
    food1 = null;
    food2 = null;
    food3 = null;
  }
}
