package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
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

  @Test
  public void noArgsConstructorValidInputTest() {
    User emptyUser = new User();
    assertEquals("", emptyUser.getUsername());
    assertEquals(0, emptyUser.getUserId());
    assertNotNull(emptyUser.getLikedRecipes());
    assertTrue(emptyUser.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void noArgsConstructorInvalidInputTest() {
    User emptyUser = new User();
    assertNotNull(emptyUser.getLikedRecipes());
    assertTrue(emptyUser.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void noArgsConstructorAtypicalInputTest() {
    User emptyUser = new User();
    assertEquals("", emptyUser.getUsername());
    assertEquals(0, emptyUser.getUserId());
    assertNotNull(emptyUser.getLikedRecipes());
    assertTrue(emptyUser.getLikedRecipes().isEmpty());
  }

  @Test
  public void basicConstructorValidInputTest() {
    User testUser = new User("ValidUser", 123);
    assertEquals("ValidUser", testUser.getUsername());
    assertEquals(123, testUser.getUserId());
    assertNotNull(testUser.getLikedRecipes());
    assertTrue(testUser.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void basicConstructorInvalidInputTest() {
    User testUser = new User(null, -1, null);
    assertEquals(null, testUser.getUsername());
    assertEquals(-1, testUser.getUserId());
    assertNotNull(testUser.getLikedRecipes());
    assertTrue(testUser.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void basicConstructorAtypicalInputTest() {
    User testUser = new User("", 0);
    assertEquals("", testUser.getUsername());
    assertEquals(0, testUser.getUserId());
    assertNotNull(testUser.getLikedRecipes());
    assertTrue(testUser.getLikedRecipes().isEmpty());
    
    User testUser2 = new User("   ", Integer.MAX_VALUE);
    assertEquals("   ", testUser2.getUsername());
    assertEquals(Integer.MAX_VALUE, testUser2.getUserId());
  }

  @Test
  public void completeConstructorValidInputTest() {
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    recipes.add(recipe1);
    
    User testUser = new User("ValidUser", 123, recipes);
    assertEquals("ValidUser", testUser.getUsername());
    assertEquals(123, testUser.getUserId());
    assertEquals(2, testUser.getLikedRecipes().size());
    assertTrue(testUser.getLikedRecipes().contains(recipe));
    assertTrue(testUser.getLikedRecipes().contains(recipe1));
  }
  
  @Test
  public void completeConstructorAtypicalInputTest() {
    ArrayList<Recipe> emptyRecipes = new ArrayList<>();
    User testUser = new User("", 0, emptyRecipes);
    assertEquals("", testUser.getUsername());
    assertEquals(0, testUser.getUserId());
    assertEquals(0, testUser.getLikedRecipes().size());
    
    ArrayList<Recipe> manyRecipes = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      manyRecipes.add(new Recipe("Recipe" + i, i, "Category", new ArrayList<>(), 0, 0, 100));
    }
    User testUser2 = new User("UserWithManyRecipes", Integer.MAX_VALUE, 
        manyRecipes);
    assertEquals("UserWithManyRecipes", testUser2.getUsername());
    assertEquals(Integer.MAX_VALUE, testUser2.getUserId());
    assertEquals(10, testUser2.getLikedRecipes().size());
  }

  @Test
  public void likeRecipeValidInputTest() {
    User testUser = new User("TestUser", 999);
    Recipe testRecipe = new Recipe("Test Recipe", 999, "Test", 
        new ArrayList<>(), 0, 0, 100);
    int initialLikes = testRecipe.getLikes();
    
    assertTrue(testUser.likeRecipe(testRecipe));
    assertTrue(testUser.getLikedRecipes().contains(testRecipe));
    assertEquals(initialLikes + 1, testRecipe.getLikes());
  }
  
  @Test
  public void likeRecipeInvalidInputTest() {
    User testUser = new User("TestUser", 999);
    try {
      testUser.likeRecipe(null);
      assertTrue(false, "Expected NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(true, "NullPointerException thrown as expected");
    }
  }
  
  @Test
  public void likeRecipeAtypicalInputTest() {
    User testUser = new User("TestUser", 999);
    Recipe alreadyLikedRecipe = new Recipe("Already Liked", 998, "Test", 
        new ArrayList<>(), 0, 0, 100);
    testUser.likeRecipe(alreadyLikedRecipe);
    
    assertFalse(testUser.likeRecipe(alreadyLikedRecipe));
    assertEquals(1, testUser.getLikedRecipes().size());
    
    Recipe sameIdDifferentRecipe = new Recipe("Different Name", 998, 
        "Different Category", new ArrayList<>(), 0, 0, 200);
    assertFalse(testUser.likeRecipe(sameIdDifferentRecipe));
  }

  @Test
  public void unlikeRecipeValidInputTest() {
    User testUser = new User("TestUser", 999);
    Recipe testRecipe = new Recipe("Test Recipe", 999, "Test", new ArrayList<>(), 0, 0, 100);
    testUser.likeRecipe(testRecipe);
    
    assertTrue(testUser.unlikeRecipe(testRecipe));
    assertFalse(testUser.getLikedRecipes().contains(testRecipe));
  }
  
  @Test
  public void unlikeRecipeInvalidInputTest() {
    User testUser = new User("TestUser", 999);
    assertFalse(testUser.unlikeRecipe(null));
  }
  
  @Test
  public void unlikeRecipeAtypicalInputTest() {
    User testUser = new User("TestUser", 999);
    Recipe notLikedRecipe = new Recipe("Not Liked", 999, "Test", new ArrayList<>(), 0, 0, 100);
    
    assertFalse(testUser.unlikeRecipe(notLikedRecipe));
    assertFalse(testUser.getLikedRecipes().contains(notLikedRecipe));
    
    Recipe sameIdDifferentRecipe = new Recipe("Different Name", 999, 
        "Different Category", new ArrayList<>(), 0, 0, 200);
    assertFalse(testUser.unlikeRecipe(sameIdDifferentRecipe));
  }

  @Test
  public void setUsernameValidInputTest() {
    User testUser = new User();
    testUser.setUsername("Valid Username");
    assertEquals("Valid Username", testUser.getUsername());
  }
  
  @Test
  public void setUsernameInvalidInputTest() {
    User testUser = new User();
    testUser.setUsername(null);
    assertEquals(null, testUser.getUsername());
  }
  
  @Test
  public void setUsernameAtypicalInputTest() {
    User testUser = new User();
    testUser.setUsername("");
    assertEquals("", testUser.getUsername());
    
    testUser.setUsername("   ");
    assertEquals("   ", testUser.getUsername());
    
    testUser.setUsername("Username-With-Special@Characters#123");
    assertEquals("Username-With-Special@Characters#123", testUser.getUsername());
  }

  @Test
  public void setUserIdValidInputTest() {
    User testUser = new User();
    testUser.setUserId(123);
    assertEquals(123, testUser.getUserId());
  }
  
  @Test
  public void setUserIdInvalidInputTest() {
    User testUser = new User();
    testUser.setUserId(-1);
    assertEquals(-1, testUser.getUserId());
  }
  
  @Test
  public void setUserIdAtypicalInputTest() {
    User testUser = new User();
    testUser.setUserId(0);
    assertEquals(0, testUser.getUserId());
    
    testUser.setUserId(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testUser.getUserId());
    
    testUser.setUserId(Integer.MIN_VALUE);
    assertEquals(Integer.MIN_VALUE, testUser.getUserId());
  }

  @Test
  public void setLikedRecipesValidInputTest() {
    User testUser = new User();
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    recipes.add(recipe1);
    
    testUser.setLikedRecipes(recipes);
    assertEquals(2, testUser.getLikedRecipes().size());
    assertTrue(testUser.getLikedRecipes().contains(recipe));
    assertTrue(testUser.getLikedRecipes().contains(recipe1));
  }
  
  @Test
  public void setLikedRecipesInvalidInputTest() {
    User testUser = new User();
    testUser.setLikedRecipes(null);
    assertNotNull(testUser.getLikedRecipes());
    assertTrue(testUser.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void setLikedRecipesAtypicalInputTest() {
    User testUser = new User();
    testUser.setLikedRecipes(new ArrayList<>());
    assertEquals(0, testUser.getLikedRecipes().size());
    
    ArrayList<Recipe> manyRecipes = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      manyRecipes.add(new Recipe("Recipe" + i, i, "Category", new ArrayList<>(), 0, 0, 100));
    }
    testUser.setLikedRecipes(manyRecipes);
    assertEquals(50, testUser.getLikedRecipes().size());
  }

  @Test
  public void compareToValidInputTest() {
    User user1 = new User("User1", 1);
    User user2 = new User("User2", 2);
    assertTrue(user1.compareTo(user2) < 0);
    assertTrue(user2.compareTo(user1) > 0);
    assertTrue(user1.compareTo(user1) == 0);
  }
  
  @Test
  public void compareToInvalidInputTest() {
    User user1 = new User("User1", 1);
    try {
      user1.compareTo(null);
      assertTrue(false, "Expected NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(true, "NullPointerException thrown as expected");
    }
  }
  
  @Test
  public void compareToAtypicalInputTest() {
    User user1 = new User("User1", 1);
    User user2 = new User("User2", 0);
    User user3 = new User("User3", -1);
    User user4 = new User("User4", Integer.MAX_VALUE);
    
    assertTrue(user1.compareTo(user2) > 0);
    assertTrue(user1.compareTo(user3) > 0);
    assertTrue(user1.compareTo(user4) < 0);
  }

  @Test
  public void equalsValidInputTest() {
    User user1 = new User("User1", 1);
    User user2 = new User("User1", 1);
    User user3 = new User("User2", 2);
    
    assertTrue(user1.equals(user2));
    assertFalse(user1.equals(user3));
    assertTrue(user1.equals(user1));
  }
  
  @Test
  public void equalsInvalidInputTest() {
    User user1 = new User("User1", 1);
    assertFalse(user1.equals(null));
    assertFalse(user1.equals("Not a User"));
    assertFalse(user1.equals(Integer.valueOf(1)));
  }
  
  @Test
  public void equalsAtypicalInputTest() {
    User user1 = new User("User1", 1);
    User user2 = new User("", 1);
    User user3 = new User(null, 1);
    User user4 = new User("Different Name", 1);
    
    assertTrue(user1.equals(user2));
    assertTrue(user1.equals(user3));
    assertTrue(user1.equals(user4));
    
    User user5 = new User("Test", 0);
    User user6 = new User("Test", 0);
    assertTrue(user5.equals(user6));
  }
  /**
   * Clean up all test variables after all tests.
   */
  
  @AfterAll
  public static void tearDownUserAfterTesting() {
    user = null;
    user1 = null;
    user2 = null;
    user3 = null;
    recipe = null;
    recipe1 = null;
    recipe2 = null;
  }
}