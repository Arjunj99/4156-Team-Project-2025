package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.calorieservice.models.Client;
import dev.coms4156.project.calorieservice.models.Recipe;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * This class contains the unit tests for the Client class.
 */
public class ClientUnitTests {

  public static Client client;
  public static Client client1;
  public static Client client2;
  public static Client client3;
  public static Recipe recipe;
  public static Recipe recipe1;
  public static Recipe recipe2;

  /**
   * This method sets up our testing variables.
   */
  @BeforeAll
  public static void setUpClientForTesting() {
    client = new Client("Alice", 1);
    client1 = new Client("Bob", 2);
    client2 = new Client("Charlie", 3);
    client3 = new Client("Diana", 4);
    
    recipe = new Recipe("Pasta", 1, "Italian", new ArrayList<>(), 100, 5, 436);
    recipe1 = new Recipe("Pizza", 2, "Italian", new ArrayList<>(), 200, 10, 436);
    recipe2 = new Recipe("Salad", 3, "Healthy", new ArrayList<>(), 50, 2, 436);
  }

  @Test
  public void equalsBothAreTheSameTest() {
    Client cmpClient = client;
    assertEquals(cmpClient, client);
  }

  @Test
  public void equalsBothAreDifferentClientsTest() {
    Client cmpClient = client1;
    assertFalse(client.equals(cmpClient));
  }

  @Test
  public void equalsObjectIsNullTest() {
    assertFalse(client.equals(null));
  }

  @Test
  public void equalsBothAreDifferentClassTest() {
    String test = "test";
    assertFalse(client.equals(test));
  }

  @Test
  public void equalsBothAreSameClassTest() {
    Client cmpClient = client1;
    assertTrue(cmpClient.equals(client1));
  }

  @Test
  public void equalsSameClientIdTest() {
    Client cmpClient = new Client("Different Name", 1);
    assertTrue(client.equals(cmpClient));
  }

  @Test
  public void compareToLessThanTest() {
    assertTrue(client.compareTo(client1) < 0);
  }

  @Test
  public void compareToEqualTest() {
    assertTrue(client.compareTo(client) == 0);
  }

  @Test
  public void compareToGreaterThanTest() {
    assertTrue(client2.compareTo(client1) > 0);
  }

  @Test
  public void toStringTest() {
    String expected = "(1)\tAlice - 0 liked recipes";
    assertEquals(expected, client.toString());
  }

  @Test
  public void noArgsConstructorTest() {
    Client emptyClient = new Client();
    assertEquals("", emptyClient.getClientname());
    assertEquals(0, emptyClient.getClientId());
    assertNotNull(emptyClient.getLikedRecipes());
    assertTrue(emptyClient.getLikedRecipes().isEmpty());
  }

  @Test
  public void basicConstructorTest() {
    Client testClient = new Client("TestClient", 999);
    assertEquals("TestClient", testClient.getClientname());
    assertEquals(999, testClient.getClientId());
    assertNotNull(testClient.getLikedRecipes());
    assertTrue(testClient.getLikedRecipes().isEmpty());
  }

  @Test
  public void completeConstructorTest() {
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    recipes.add(recipe1);
    
    Client testClient = new Client("TestClient", 999, recipes);
    assertEquals("TestClient", testClient.getClientname());
    assertEquals(999, testClient.getClientId());
    assertEquals(2, testClient.getLikedRecipes().size());
    assertTrue(testClient.getLikedRecipes().contains(recipe));
    assertTrue(testClient.getLikedRecipes().contains(recipe1));
  }

  @Test
  public void gettersAndSettersTest() {
    Client testClient = new Client();
    
    testClient.setClientname("New Clientname");
    assertEquals("New Clientname", testClient.getClientname());
    
    testClient.setClientId(555);
    assertEquals(555, testClient.getClientId());
    
    ArrayList<Recipe> newRecipes = new ArrayList<>();
    newRecipes.add(recipe);
    testClient.setLikedRecipes(newRecipes);
    assertEquals(1, testClient.getLikedRecipes().size());
    assertTrue(testClient.getLikedRecipes().contains(recipe));
  }

  @Test
  public void setLikedRecipesWithNullTest() {
    Client testClient = new Client();
    testClient.setLikedRecipes(null);
    assertNotNull(testClient.getLikedRecipes());
    assertTrue(testClient.getLikedRecipes().isEmpty());
  }

  @Test
  public void likeRecipeSuccessTest() {
    Client testClient = new Client("TestClient", 999);
    int initialLikes = recipe.getLikes();
    
    assertTrue(testClient.likeRecipe(recipe));
    assertTrue(testClient.getLikedRecipes().contains(recipe));
    assertEquals(initialLikes + 1, recipe.getLikes());
  }

  @Test
  public void likeRecipeAlreadyLikedTest() {
    Client testClient = new Client("TestClient", 999);
    testClient.likeRecipe(recipe);
    int likesAfterFirst = recipe.getLikes();
    
    assertFalse(testClient.likeRecipe(recipe));
    assertEquals(1, testClient.getLikedRecipes().size());
    assertEquals(likesAfterFirst, recipe.getLikes());
  }

  @Test
  public void unlikeRecipeSuccessTest() {
    Client testClient = new Client("TestClient", 999);
    testClient.likeRecipe(recipe);
    
    assertTrue(testClient.unlikeRecipe(recipe));
    assertFalse(testClient.getLikedRecipes().contains(recipe));
  }

  @Test
  public void unlikeRecipeNotLikedTest() {
    Client testClient = new Client("TestClient", 999);
    
    assertFalse(testClient.unlikeRecipe(recipe));
    assertFalse(testClient.getLikedRecipes().contains(recipe));
  }

  @Test
  public void likeRecipeWithMultipleRecipesTest() {
    Client testClient = new Client("TestClient", 999);
    
    assertTrue(testClient.likeRecipe(recipe));
    assertTrue(testClient.likeRecipe(recipe1));
    assertTrue(testClient.likeRecipe(recipe2));
    
    assertEquals(3, testClient.getLikedRecipes().size());
    assertTrue(testClient.getLikedRecipes().contains(recipe));
    assertTrue(testClient.getLikedRecipes().contains(recipe1));
    assertTrue(testClient.getLikedRecipes().contains(recipe2));
  }

  @Test
  public void toStringWithLikedRecipesTest() {
    Client testClient = new Client("TestClient", 999);
    testClient.likeRecipe(recipe);
    testClient.likeRecipe(recipe1);
    
    String expected = "(999)\tTestClient - 2 liked recipes";
    assertEquals(expected, testClient.toString());
  }

  @Test
  public void compareToWithSameIdTest() {
    Client sameIdClient = new Client("Different", 1);
    assertTrue(client.compareTo(sameIdClient) == 0);
  }

  @Test
  public void compareToWithDifferentIdTest() {
    assertTrue(client3.compareTo(client) > 0);
    assertTrue(client.compareTo(client3) < 0);
  }

  @Test
  public void noArgsConstructorValidInputTest() {
    Client emptyClient = new Client();
    assertEquals("", emptyClient.getClientname());
    assertEquals(0, emptyClient.getClientId());
    assertNotNull(emptyClient.getLikedRecipes());
    assertTrue(emptyClient.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void noArgsConstructorInvalidInputTest() {
    Client emptyClient = new Client();
    assertNotNull(emptyClient.getLikedRecipes());
    assertTrue(emptyClient.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void noArgsConstructorAtypicalInputTest() {
    Client emptyClient = new Client();
    assertEquals("", emptyClient.getClientname());
    assertEquals(0, emptyClient.getClientId());
    assertNotNull(emptyClient.getLikedRecipes());
    assertTrue(emptyClient.getLikedRecipes().isEmpty());
  }

  @Test
  public void basicConstructorValidInputTest() {
    Client testClient = new Client("ValidClient", 123);
    assertEquals("ValidClient", testClient.getClientname());
    assertEquals(123, testClient.getClientId());
    assertNotNull(testClient.getLikedRecipes());
    assertTrue(testClient.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void basicConstructorInvalidInputTest() {
    Client testClient = new Client(null, -1, null);
    assertEquals(null, testClient.getClientname());
    assertEquals(-1, testClient.getClientId());
    assertNotNull(testClient.getLikedRecipes());
    assertTrue(testClient.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void basicConstructorAtypicalInputTest() {
    Client testClient = new Client("", 0);
    assertEquals("", testClient.getClientname());
    assertEquals(0, testClient.getClientId());
    assertNotNull(testClient.getLikedRecipes());
    assertTrue(testClient.getLikedRecipes().isEmpty());
    
    Client testClient2 = new Client("   ", Integer.MAX_VALUE);
    assertEquals("   ", testClient2.getClientname());
    assertEquals(Integer.MAX_VALUE, testClient2.getClientId());
  }

  @Test
  public void completeConstructorValidInputTest() {
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    recipes.add(recipe1);
    
    Client testClient = new Client("ValidClient", 123, recipes);
    assertEquals("ValidClient", testClient.getClientname());
    assertEquals(123, testClient.getClientId());
    assertEquals(2, testClient.getLikedRecipes().size());
    assertTrue(testClient.getLikedRecipes().contains(recipe));
    assertTrue(testClient.getLikedRecipes().contains(recipe1));
  }
  
  @Test
  public void completeConstructorAtypicalInputTest() {
    ArrayList<Recipe> emptyRecipes = new ArrayList<>();
    Client testClient = new Client("", 0, emptyRecipes);
    assertEquals("", testClient.getClientname());
    assertEquals(0, testClient.getClientId());
    assertEquals(0, testClient.getLikedRecipes().size());
    
    ArrayList<Recipe> manyRecipes = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      manyRecipes.add(new Recipe("Recipe" + i, i, "Category", new ArrayList<>(), 0, 0, 100));
    }
    Client testClient2 = new Client("ClientWithManyRecipes", Integer.MAX_VALUE, 
        manyRecipes);
    assertEquals("ClientWithManyRecipes", testClient2.getClientname());
    assertEquals(Integer.MAX_VALUE, testClient2.getClientId());
    assertEquals(10, testClient2.getLikedRecipes().size());
  }

  @Test
  public void likeRecipeValidInputTest() {
    Client testClient = new Client("TestClient", 999);
    Recipe testRecipe = new Recipe("Test Recipe", 999, "Test", 
        new ArrayList<>(), 0, 0, 100);
    int initialLikes = testRecipe.getLikes();
    
    assertTrue(testClient.likeRecipe(testRecipe));
    assertTrue(testClient.getLikedRecipes().contains(testRecipe));
    assertEquals(initialLikes + 1, testRecipe.getLikes());
  }
  
  @Test
  public void likeRecipeInvalidInputTest() {
    Client testClient = new Client("TestClient", 999);
    try {
      testClient.likeRecipe(null);
      assertTrue(false, "Expected NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(true, "NullPointerException thrown as expected");
    }
  }
  
  @Test
  public void likeRecipeAtypicalInputTest() {
    Client testClient = new Client("TestClient", 999);
    Recipe alreadyLikedRecipe = new Recipe("Already Liked", 998, "Test", 
        new ArrayList<>(), 0, 0, 100);
    testClient.likeRecipe(alreadyLikedRecipe);
    
    assertFalse(testClient.likeRecipe(alreadyLikedRecipe));
    assertEquals(1, testClient.getLikedRecipes().size());
    
    Recipe sameIdDifferentRecipe = new Recipe("Different Name", 998, 
        "Different Category", new ArrayList<>(), 0, 0, 200);
    assertFalse(testClient.likeRecipe(sameIdDifferentRecipe));
  }

  @Test
  public void unlikeRecipeValidInputTest() {
    Client testClient = new Client("TestClient", 999);
    Recipe testRecipe = new Recipe("Test Recipe", 999, "Test", new ArrayList<>(), 0, 0, 100);
    testClient.likeRecipe(testRecipe);
    
    assertTrue(testClient.unlikeRecipe(testRecipe));
    assertFalse(testClient.getLikedRecipes().contains(testRecipe));
  }
  
  @Test
  public void unlikeRecipeInvalidInputTest() {
    Client testClient = new Client("TestClient", 999);
    assertFalse(testClient.unlikeRecipe(null));
  }
  
  @Test
  public void unlikeRecipeAtypicalInputTest() {
    Client testClient = new Client("TestClient", 999);
    Recipe notLikedRecipe = new Recipe("Not Liked", 999, "Test", new ArrayList<>(), 0, 0, 100);
    
    assertFalse(testClient.unlikeRecipe(notLikedRecipe));
    assertFalse(testClient.getLikedRecipes().contains(notLikedRecipe));
    
    Recipe sameIdDifferentRecipe = new Recipe("Different Name", 999, 
        "Different Category", new ArrayList<>(), 0, 0, 200);
    assertFalse(testClient.unlikeRecipe(sameIdDifferentRecipe));
  }

  @Test
  public void setClientnameValidInputTest() {
    Client testClient = new Client();
    testClient.setClientname("Valid Clientname");
    assertEquals("Valid Clientname", testClient.getClientname());
  }
  
  @Test
  public void setClientnameInvalidInputTest() {
    Client testClient = new Client();
    testClient.setClientname(null);
    assertEquals(null, testClient.getClientname());
  }
  
  @Test
  public void setClientnameAtypicalInputTest() {
    Client testClient = new Client();
    testClient.setClientname("");
    assertEquals("", testClient.getClientname());
    
    testClient.setClientname("   ");
    assertEquals("   ", testClient.getClientname());
    
    testClient.setClientname("Clientname-With-Special@Characters#123");
    assertEquals("Clientname-With-Special@Characters#123", testClient.getClientname());
  }

  @Test
  public void setClientIdValidInputTest() {
    Client testClient = new Client();
    testClient.setClientId(123);
    assertEquals(123, testClient.getClientId());
  }
  
  @Test
  public void setClientIdInvalidInputTest() {
    Client testClient = new Client();
    testClient.setClientId(-1);
    assertEquals(-1, testClient.getClientId());
  }
  
  @Test
  public void setClientIdAtypicalInputTest() {
    Client testClient = new Client();
    testClient.setClientId(0);
    assertEquals(0, testClient.getClientId());
    
    testClient.setClientId(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, testClient.getClientId());
    
    testClient.setClientId(Integer.MIN_VALUE);
    assertEquals(Integer.MIN_VALUE, testClient.getClientId());
  }

  @Test
  public void setLikedRecipesValidInputTest() {
    Client testClient = new Client();
    ArrayList<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    recipes.add(recipe1);
    
    testClient.setLikedRecipes(recipes);
    assertEquals(2, testClient.getLikedRecipes().size());
    assertTrue(testClient.getLikedRecipes().contains(recipe));
    assertTrue(testClient.getLikedRecipes().contains(recipe1));
  }
  
  @Test
  public void setLikedRecipesInvalidInputTest() {
    Client testClient = new Client();
    testClient.setLikedRecipes(null);
    assertNotNull(testClient.getLikedRecipes());
    assertTrue(testClient.getLikedRecipes().isEmpty());
  }
  
  @Test
  public void setLikedRecipesAtypicalInputTest() {
    Client testClient = new Client();
    testClient.setLikedRecipes(new ArrayList<>());
    assertEquals(0, testClient.getLikedRecipes().size());
    
    ArrayList<Recipe> manyRecipes = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      manyRecipes.add(new Recipe("Recipe" + i, i, "Category", new ArrayList<>(), 0, 0, 100));
    }
    testClient.setLikedRecipes(manyRecipes);
    assertEquals(50, testClient.getLikedRecipes().size());
  }

  @Test
  public void compareToValidInputTest() {
    Client client1 = new Client("Client1", 1);
    Client client2 = new Client("Client2", 2);
    assertTrue(client1.compareTo(client2) < 0);
    assertTrue(client2.compareTo(client1) > 0);
    assertTrue(client1.compareTo(client1) == 0);
  }
  
  @Test
  public void compareToInvalidInputTest() {
    Client client1 = new Client("Client1", 1);
    try {
      client1.compareTo(null);
      assertTrue(false, "Expected NullPointerException");
    } catch (NullPointerException e) {
      assertTrue(true, "NullPointerException thrown as expected");
    }
  }
  
  @Test
  public void compareToAtypicalInputTest() {
    Client client1 = new Client("Client1", 1);
    Client client2 = new Client("Client2", 0);
    Client client3 = new Client("Client3", -1);
    Client client4 = new Client("Client4", Integer.MAX_VALUE);
    
    assertTrue(client1.compareTo(client2) > 0);
    assertTrue(client1.compareTo(client3) > 0);
    assertTrue(client1.compareTo(client4) < 0);
  }

  @Test
  public void equalsValidInputTest() {
    Client client1 = new Client("Client1", 1);
    Client client2 = new Client("Client1", 1);
    Client client3 = new Client("Client2", 2);
    
    assertTrue(client1.equals(client2));
    assertFalse(client1.equals(client3));
    assertTrue(client1.equals(client1));
  }
  
  @Test
  public void equalsInvalidInputTest() {
    Client client1 = new Client("Client1", 1);
    assertFalse(client1.equals(null));
    assertFalse(client1.equals("Not a Client"));
    assertFalse(client1.equals(Integer.valueOf(1)));
  }
  
  @Test
  public void equalsAtypicalInputTest() {
    Client client1 = new Client("Client1", 1);
    Client client2 = new Client("", 1);
    Client client3 = new Client(null, 1);
    Client client4 = new Client("Different Name", 1);
    
    assertTrue(client1.equals(client2));
    assertTrue(client1.equals(client3));
    assertTrue(client1.equals(client4));
    
    Client client5 = new Client("Test", 0);
    Client client6 = new Client("Test", 0);
    assertTrue(client5.equals(client6));
  }
  /**
   * Clean up all test variables after all tests.
   */
  
  @AfterAll
  public static void tearDownClientAfterTesting() {
    client = null;
    client1 = null;
    client2 = null;
    client3 = null;
    recipe = null;
    recipe1 = null;
    recipe2 = null;
  }
}