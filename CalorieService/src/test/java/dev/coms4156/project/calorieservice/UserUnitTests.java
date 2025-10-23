package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the User class.
 */
@SpringBootTest
public class UserUnitTests {

  public static User user;
  public static User user1;
  public static User user2;
  public static User user3;
  public static Recipe recipe;
  public static Recipe recipe1;
  public static Recipe recipe2;

  /**
   * This method sets up our testing variables.
   */
  @BeforeAll
  public static void setUpUserForTesting() {
    user = new User("Alice", 1);
    user1 = new User("Bob", 2);
    user2 = new User("Charlie", 3);
    user3 = new User("Diana", 4);
    
    recipe = new Recipe("Pasta", 1, "Italian", new ArrayList<>(), 100, 5, 436);
    recipe1 = new Recipe("Pizza", 2, "Italian", new ArrayList<>(), 200, 10, 436);
    recipe2 = new Recipe("Salad", 3, "Healthy", new ArrayList<>(), 50, 2, 436);
  }

  @Test
  public void equalsBothAreTheSameTest() {
    User cmpUser = user;
    assertEquals(cmpUser, user);
  }

  @Test
  public void equalsBothAreDifferentUsersTest() {
    User cmpUser = user1;
    assertFalse(user.equals(cmpUser));
  }

  @Test
  public void equalsObjectIsNullTest() {
    assertFalse(user.equals(null));
  }

  @Test
  public void equalsBothAreDifferentClassTest() {
    String test = "test";
    assertFalse(user.equals(test));
  }

  @Test
  public void equalsBothAreSameClassTest() {
    User cmpUser = user1;
    assertTrue(cmpUser.equals(user1));
  }

  @Test
  public void equalsSameUserIdTest() {
    User cmpUser = new User("Different Name", 1);
    assertTrue(user.equals(cmpUser));
  }

  @Test
  public void compareToLessThanTest() {
    assertTrue(user.compareTo(user1) < 0);
  }

  @Test
  public void compareToEqualTest() {
    assertTrue(user.compareTo(user) == 0);
  }

  @Test
  public void compareToGreaterThanTest() {
    assertTrue(user2.compareTo(user1) > 0);
  }

  @Test
  public void toStringTest() {
    String expected = "(1)\tAlice - 0 liked recipes";
    assertEquals(expected, user.toString());
  }

  @Test
  public void noArgsConstructorTest() {
    User emptyUser = new User();
    assertEquals("", emptyUser.getUsername());
    assertEquals(0, emptyUser.getUserId());
    assertNotNull(emptyUser.getLikedRecipes());
    assertTrue(emptyUser.getLikedRecipes().isEmpty());
  }

  @Test
  public void basicConstructorTest() {
    User testUser = new User("TestUser", 999);
    assertEquals("TestUser", testUser.getUsername());
    assertEquals(999, testUser.getUserId());
    assertNotNull(testUser.getLikedRecipes());
    assertTrue(testUser.getLikedRecipes().isEmpty());
  }

  @Test
  public void completeConstructorTest() {
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    recipes.add(recipe1);
    
    User testUser = new User("TestUser", 999, recipes);
    assertEquals("TestUser", testUser.getUsername());
    assertEquals(999, testUser.getUserId());
    assertEquals(2, testUser.getLikedRecipes().size());
    assertTrue(testUser.getLikedRecipes().contains(recipe));
    assertTrue(testUser.getLikedRecipes().contains(recipe1));
  }

  @Test
  public void gettersAndSettersTest() {
    User testUser = new User();
    
    testUser.setUsername("New Username");
    assertEquals("New Username", testUser.getUsername());
    
    testUser.setUserId(555);
    assertEquals(555, testUser.getUserId());
    
    ArrayList<Recipe> newRecipes = new ArrayList<>();
    newRecipes.add(recipe);
    testUser.setLikedRecipes(newRecipes);
    assertEquals(1, testUser.getLikedRecipes().size());
    assertTrue(testUser.getLikedRecipes().contains(recipe));
  }

  @Test
  public void setLikedRecipesWithNullTest() {
    User testUser = new User();
    testUser.setLikedRecipes(null);
    assertNotNull(testUser.getLikedRecipes());
    assertTrue(testUser.getLikedRecipes().isEmpty());
  }

  @Test
  public void likeRecipeSuccessTest() {
    User testUser = new User("TestUser", 999);
    int initialLikes = recipe.getLikes();
    
    assertTrue(testUser.likeRecipe(recipe));
    assertTrue(testUser.getLikedRecipes().contains(recipe));
    assertEquals(initialLikes + 1, recipe.getLikes());
  }

  @Test
  public void likeRecipeAlreadyLikedTest() {
    User testUser = new User("TestUser", 999);
    testUser.likeRecipe(recipe);
    int likesAfterFirst = recipe.getLikes();
    
    assertFalse(testUser.likeRecipe(recipe));
    assertEquals(1, testUser.getLikedRecipes().size());
    assertEquals(likesAfterFirst, recipe.getLikes());
  }

  @Test
  public void unlikeRecipeSuccessTest() {
    User testUser = new User("TestUser", 999);
    testUser.likeRecipe(recipe);
    
    assertTrue(testUser.unlikeRecipe(recipe));
    assertFalse(testUser.getLikedRecipes().contains(recipe));
  }

  @Test
  public void unlikeRecipeNotLikedTest() {
    User testUser = new User("TestUser", 999);
    
    assertFalse(testUser.unlikeRecipe(recipe));
    assertFalse(testUser.getLikedRecipes().contains(recipe));
  }

  @Test
  public void likeRecipeWithMultipleRecipesTest() {
    User testUser = new User("TestUser", 999);
    
    assertTrue(testUser.likeRecipe(recipe));
    assertTrue(testUser.likeRecipe(recipe1));
    assertTrue(testUser.likeRecipe(recipe2));
    
    assertEquals(3, testUser.getLikedRecipes().size());
    assertTrue(testUser.getLikedRecipes().contains(recipe));
    assertTrue(testUser.getLikedRecipes().contains(recipe1));
    assertTrue(testUser.getLikedRecipes().contains(recipe2));
  }

  @Test
  public void toStringWithLikedRecipesTest() {
    User testUser = new User("TestUser", 999);
    testUser.likeRecipe(recipe);
    testUser.likeRecipe(recipe1);
    
    String expected = "(999)\tTestUser - 2 liked recipes";
    assertEquals(expected, testUser.toString());
  }

  @Test
  public void compareToWithSameIdTest() {
    User sameIdUser = new User("Different", 1);
    assertTrue(user.compareTo(sameIdUser) == 0);
  }

  @Test
  public void compareToWithDifferentIdTest() {
    assertTrue(user3.compareTo(user) > 0);
    assertTrue(user.compareTo(user3) < 0);
  }
}