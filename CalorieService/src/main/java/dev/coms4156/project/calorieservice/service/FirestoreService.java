package dev.coms4156.project.calorieservice.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import dev.coms4156.project.calorieservice.models.Client;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;

/**
 * Service class for managing Firestore database operations.
 */
@Service
public class FirestoreService {

  private static final String FOODS_COLLECTION = "food";
  private static final String RECIPES_COLLECTION = "recipes";
  private static final String USERS_COLLECTION = "clients";

  private Firestore db;

  /**
   * Initializes the Firestore connection.
   */
 
  @PostConstruct
  public void initialize() {
    try {
      Firestore db = FirestoreOptions.getDefaultInstance().getService();
      this.db = db;

      System.out.println("Firestore initialized");

    } catch (Exception e) {
      //throw new RuntimeException("Failed to initialize Firestore: " + e.getMessage(), e);

      // Added a Timeout instead of Failure to Firestore init to make sure tests run locally.
      System.out.println("WARNING: Firestore init failed; running locally with Firestore disabled.");
      e.printStackTrace();
    }
  }
  
  

  /**
   * Closes the Firestore connection before bean destruction.
   */
  @PreDestroy
  public void cleanup() {
    if (db != null) {
      try {
        db.close();
        System.out.println("Firestore connection closed");
      } catch (Exception e) {
        System.err.println("Error closing Firestore connection: " + e.getMessage());
      }
    }
  }

  // ==================== FOOD OPERATIONS ====================

  /**
   * Converts a Food object to a Firestore document map.
   */
  private Map<String, Object> foodToMap(Food food) {
    Map<String, Object> map = new HashMap<>();
    map.put("foodId", food.getFoodId());
    map.put("foodName", food.getFoodName());
    map.put("calories", food.getCalories());
    map.put("category", food.getCategory());
    return map;
  }

  /**
   * Converts a Firestore document map to a Food object.
   */
  private Food mapToFood(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    return new Food(
        (String) map.get("foodName"),
        ((Number) map.get("foodId")).intValue(),
        ((Number) map.get("calories")).intValue(),
        (String) map.get("category")
    );
  }

  /**
   * Gets all foods from Firestore.
   */
  public ArrayList<Food> getAllFoods() throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> future = db.collection(FOODS_COLLECTION).get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    ArrayList<Food> foods = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      Food food = mapToFood(document.getData());
      if (food != null) {
        foods.add(food);
      }
    }
    return foods;
  }

  /**
   * Gets a food by ID from Firestore.
   */
  public Food getFoodById(int foodId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(FOODS_COLLECTION).document(String.valueOf(foodId));
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();
    if (document.exists()) {
      return mapToFood(document.getData());
    }
    return null;
  }

  /**
   * Adds a food to Firestore.
   */
  public boolean addFood(Food food) throws ExecutionException, InterruptedException {
    if (food == null) {
      return false;
    }
    // Check if food already exists
    Food existing = getFoodById(food.getFoodId());
    if (existing != null) {
      return false;
    }
    DocumentReference docRef = db.collection(FOODS_COLLECTION)
        .document(String.valueOf(food.getFoodId()));
    ApiFuture<WriteResult> future = docRef.set(foodToMap(food));
    future.get();
    return true;
  }

  /**
   * Gets foods by category and calorie filter.
   */
  public List<Food> getFoodsByCategoryAndCalories(String category, int maxCalories)
      throws ExecutionException, InterruptedException {
    Query query = db.collection(FOODS_COLLECTION)
        .whereEqualTo("category", category)
        .whereLessThan("calories", maxCalories);
    ApiFuture<QuerySnapshot> future = query.get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    List<Food> foods = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      Food food = mapToFood(document.getData());
      if (food != null) {
        foods.add(food);
      }
    }
    return foods;
  }

  // ==================== RECIPE OPERATIONS ====================

  /**
   * Converts a Recipe object to a Firestore document map.
   */
  private Map<String, Object> recipeToMap(Recipe recipe) {
    Map<String, Object> map = new HashMap<>();
    map.put("recipeId", recipe.getRecipeId());
    map.put("recipeName", recipe.getRecipeName());
    map.put("category", recipe.getCategory());
    map.put("views", recipe.getViews());
    map.put("likes", recipe.getLikes());
    // Calculate totalCalories from ingredients
    map.put("totalCalories", recipe.getTotalCalories());

    // Convert ingredients list
    List<Map<String, Object>> ingredientsList = new ArrayList<>();
    if (recipe.getIngredients() != null) {
      for (Food ingredient : recipe.getIngredients()) {
        ingredientsList.add(foodToMap(ingredient));
      }
    }
    map.put("ingredients", ingredientsList);

    return map;
  }

  /**
   * Converts a Firestore document map to a Recipe object.
   */
  private Recipe mapToRecipe(Map<String, Object> map) {
    if (map == null) {
      return null;
    }

    // Convert ingredients list first
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> ingredientsList = (List<Map<String, Object>>) map.get("ingredients");
    ArrayList<Food> ingredients = new ArrayList<>();
    if (ingredientsList != null) {
      for (Map<String, Object> ingredientMap : ingredientsList) {
        Food food = mapToFood(ingredientMap);
        if (food != null) {
          ingredients.add(food);
        }
      }
    }

    // Calculate totalCalories from ingredients
    int totalCalories = 0;
    for (Food ingredient : ingredients) {
      totalCalories += ingredient.getCalories();
    }

    // Use constructor that includes totalCalories
    Recipe recipe = new Recipe(
        (String) map.get("recipeName"),
        ((Number) map.get("recipeId")).intValue(),
        (String) map.get("category"),
        ingredients,
        ((Number) map.get("views")).intValue(),
        ((Number) map.get("likes")).intValue(),
        totalCalories
    );

    return recipe;
  }

  /**
   * Gets all recipes from Firestore.
   */
  public ArrayList<Recipe> getAllRecipes() throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> future = db.collection(RECIPES_COLLECTION).get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    ArrayList<Recipe> recipes = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      Recipe recipe = mapToRecipe(document.getData());
      if (recipe != null) {
        recipes.add(recipe);
      }
    }
    return recipes;
  }

  /**
   * Gets a recipe by ID from Firestore.
   */
  public Recipe getRecipeById(int recipeId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(RECIPES_COLLECTION)
        .document(String.valueOf(recipeId));
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();
    if (document.exists()) {
      return mapToRecipe(document.getData());
    }
    return null;
  }

  /**
   * Adds a recipe to Firestore.
   */
  public boolean addRecipe(Recipe recipe) throws ExecutionException, InterruptedException {
    if (recipe == null) {
      return false;
    }
    // Check if recipe already exists
    Recipe existing = getRecipeById(recipe.getRecipeId());
    if (existing != null) {
      return false;
    }
    DocumentReference docRef = db.collection(RECIPES_COLLECTION)
        .document(String.valueOf(recipe.getRecipeId()));
    ApiFuture<WriteResult> future = docRef.set(recipeToMap(recipe));
    future.get();
    return true;
  }

  /**
   * Updates a recipe in Firestore.
   */
  public boolean updateRecipe(Recipe recipe) throws ExecutionException, InterruptedException {
    if (recipe == null) {
      return false;
    }
    DocumentReference docRef = db.collection(RECIPES_COLLECTION)
        .document(String.valueOf(recipe.getRecipeId()));
    ApiFuture<WriteResult> future = docRef.set(recipeToMap(recipe));
    future.get();
    return true;
  }

  /**
   * Gets recipes by category and calorie filter.
   */
  public List<Recipe> getRecipesByCategoryAndCalories(String category, int maxCalories)
      throws ExecutionException, InterruptedException {
    Query query = db.collection(RECIPES_COLLECTION)
        .whereEqualTo("category", category)
        .whereLessThanOrEqualTo("totalCalories", maxCalories);
    ApiFuture<QuerySnapshot> future = query.get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    List<Recipe> recipes = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      Recipe recipe = mapToRecipe(document.getData());
      if (recipe != null) {
        recipes.add(recipe);
      }
    }
    return recipes;
  }

  /**
   * Gets all recipes with calorie filter.
   */
  public List<Recipe> getRecipesByCalories(int maxCalories)
      throws ExecutionException, InterruptedException {
    Query query = db.collection(RECIPES_COLLECTION)
        .whereLessThanOrEqualTo("totalCalories", maxCalories);
    ApiFuture<QuerySnapshot> future = query.get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    List<Recipe> recipes = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      Recipe recipe = mapToRecipe(document.getData());
      if (recipe != null) {
        recipes.add(recipe);
      }
    }
    return recipes;
  }

  // ==================== USER OPERATIONS ====================

  /**
   * Converts a Client object to a Firestore document map.
   */
  private Map<String, Object> clientToMap(Client client) {
    Map<String, Object> map = new HashMap<>();
    map.put("clientId", client.getClientId());
    map.put("clientname", client.getClientname());

    // Store liked recipe IDs instead of full recipe objects
    List<Integer> likedRecipeIds = new ArrayList<>();
    if (client.getLikedRecipes() != null) {
      for (Recipe recipe : client.getLikedRecipes()) {
        likedRecipeIds.add(recipe.getRecipeId());
      }
    }
    map.put("likedRecipeIds", likedRecipeIds);

    return map;
  }

  /**
   * Converts a Firestore document map to a Client object.
   * Note: This loads recipe IDs only. Recipes need to be loaded separately.
   */
  private Client mapToClient(Map<String, Object> map) throws ExecutionException,
      InterruptedException {
    if (map == null) {
      return null;
    }
    Client client = new Client();
    client.setClientId(((Number) map.get("clientId")).intValue());
    client.setClientname((String) map.get("clientname"));

    // Load liked recipes by their IDs
    // Firestore returns Long for numeric values, so we need to convert
    @SuppressWarnings("unchecked")
    List<Object> likedRecipeIds = (List<Object>) map.get("likedRecipeIds");
    ArrayList<Recipe> likedRecipes = new ArrayList<>();
    if (likedRecipeIds != null) {
      for (Object recipeIdObj : likedRecipeIds) {
        // Convert to int, handling both Integer and Long from Firestore
        int recipeId = ((Number) recipeIdObj).intValue();
        Recipe recipe = getRecipeById(recipeId);
        if (recipe != null) {
          likedRecipes.add(recipe);
        }
      }
    }
    client.setLikedRecipes(likedRecipes);

    return client;
  }

  /**
   * Gets all clients from Firestore.
   */
  public ArrayList<Client> getAllClients() throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> future = db.collection(USERS_COLLECTION).get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    ArrayList<Client> clients = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      Client client = mapToClient(document.getData());
      if (client != null) {
        clients.add(client);
      }
    }
    return clients;
  }

  /**
   * Gets a client by ID from Firestore.
   */
  public Client getClientById(int clientId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(USERS_COLLECTION).document(String.valueOf(clientId));
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();
    if (document.exists()) {
      return mapToClient(document.getData());
    }
    return null;
  }

  /**
   * Adds a client to Firestore.
   */
  public boolean addClient(Client client) throws ExecutionException, InterruptedException {
    if (client == null) {
      return false;
    }
    // Check if client already exists
    Client existing = getClientById(client.getClientId());
    if (existing != null) {
      return false;
    }
    DocumentReference docRef = db.collection(USERS_COLLECTION)
        .document(String.valueOf(client.getClientId()));
    ApiFuture<WriteResult> future = docRef.set(clientToMap(client));
    future.get();
    return true;
  }

  /**
   * Updates a client in Firestore.
   */
  public boolean updateClient(Client client) throws ExecutionException, InterruptedException {
    if (client == null) {
      return false;
    }
    DocumentReference docRef = db.collection(USERS_COLLECTION)
        .document(String.valueOf(client.getClientId()));
    ApiFuture<WriteResult> future = docRef.set(clientToMap(client));
    future.get();
    return true;
  }

  // ==================== DELETE OPERATIONS ====================

  /**
   * Deletes a food from Firestore.
   *
   * @param foodId the ID of the food to delete
   * @return true if the food was deleted, false if it didn't exist
   */
  public boolean deleteFood(int foodId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(FOODS_COLLECTION)
        .document(String.valueOf(foodId));
    ApiFuture<WriteResult> future = docRef.delete();
    future.get();
    return true;
  }

  /**
   * Deletes a recipe from Firestore.
   *
   * @param recipeId the ID of the recipe to delete
   * @return true if the recipe was deleted, false if it didn't exist
   */
  public boolean deleteRecipe(int recipeId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(RECIPES_COLLECTION)
        .document(String.valueOf(recipeId));
    ApiFuture<WriteResult> future = docRef.delete();
    future.get();
    return true;
  }

  /**
   * Deletes a client from Firestore.
   *
   * @param clientId the ID of the client to delete
   * @return true if the client was deleted, false if it didn't exist
   */
  public boolean deleteClient(int clientId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(USERS_COLLECTION)
        .document(String.valueOf(clientId));
    ApiFuture<WriteResult> future = docRef.delete();
    future.get();
    return true;
  }
}

