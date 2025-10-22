package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Food;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the Food class.
 */
@SpringBootTest
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
}
