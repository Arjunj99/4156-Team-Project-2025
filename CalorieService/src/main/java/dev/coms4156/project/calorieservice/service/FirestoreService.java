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
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
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
 * Handles all CRUD operations for Food, Recipe, and User entities.
 */
@Service
public class FirestoreService {

  private static final String FOODS_COLLECTION = "food";
  private static final String RECIPES_COLLECTION = "recipes";
  private static final String USERS_COLLECTION = "users";

  private Firestore db;

  /**
   * Initializes the Firestore connection after bean construction.
   */
  // @PostConstruct
  // public void initialize() {
  //   try {
  //     // Read credentials from resources
  //     InputStream credentialsStream = FirestoreService.class.getClassLoader()
  //         .getResourceAsStream(CREDENTIALS_PATH);

  //     if (credentialsStream == null) {
  //       throw new RuntimeException("❌ Could not find credentials file: " + CREDENTIALS_PATH);
  //     }

  //     System.out.println("📁 Found credentials file in resources: " + CREDENTIALS_PATH);

  //     // Load credentials
  //     ServiceAccountCredentials credentials =
  //         ServiceAccountCredentials.fromStream(credentialsStream);

  //     // Force-token refresh to ensure JSON is valid and service-account is used
  //     credentials = (ServiceAccountCredentials) credentials.createScoped(
  //         java.util.Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")
  //     );
  //     credentials.refreshAccessToken();

  //     System.out.println("🔍 Loaded credentials class = " + credentials.getClass().getName());
  //     System.out.println("🔑 Using service account: " + credentials.getClientEmail());

  //     // Build Firestore client
  //     FirestoreOptions firestoreOptions =
  //         FirestoreOptions.newBuilder()
  //             .setProjectId(PROJECT_ID)
  //             .setCredentials(credentials)
  //             .build();

  //     db = firestoreOptions.getService();
  //     System.out.println("✅ Connected to Firestore project: " + PROJECT_ID);

  //   } catch (Exception e) {
  //     throw new RuntimeException("Failed to initialize Firestore: " + e.getMessage(), e);
  //   }
  // }
  @PostConstruct
  public void initialize() {
    try {
      Firestore db = FirestoreOptions.getDefaultInstance().getService();
      this.db = db;

      System.out.println("✅ Firestore initialized using Cloud Run ADC");

    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize Firestore: " + e.getMessage(), e);
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
        System.out.println("🔌 Firestore connection closed");
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
   * Converts a User object to a Firestore document map.
   */
  private Map<String, Object> userToMap(User user) {
    Map<String, Object> map = new HashMap<>();
    map.put("userId", user.getUserId());
    map.put("username", user.getUsername());

    // Store liked recipe IDs instead of full recipe objects
    List<Integer> likedRecipeIds = new ArrayList<>();
    if (user.getLikedRecipes() != null) {
      for (Recipe recipe : user.getLikedRecipes()) {
        likedRecipeIds.add(recipe.getRecipeId());
      }
    }
    map.put("likedRecipeIds", likedRecipeIds);

    return map;
  }

  /**
   * Converts a Firestore document map to a User object.
   * Note: This loads recipe IDs only. Recipes need to be loaded separately.
   */
  private User mapToUser(Map<String, Object> map) throws ExecutionException, InterruptedException {
    if (map == null) {
      return null;
    }
    User user = new User();
    user.setUserId(((Number) map.get("userId")).intValue());
    user.setUsername((String) map.get("username"));

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
    user.setLikedRecipes(likedRecipes);

    return user;
  }

  /**
   * Gets all users from Firestore.
   */
  public ArrayList<User> getAllUsers() throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> future = db.collection(USERS_COLLECTION).get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    ArrayList<User> users = new ArrayList<>();
    for (QueryDocumentSnapshot document : documents) {
      User user = mapToUser(document.getData());
      if (user != null) {
        users.add(user);
      }
    }
    return users;
  }

  /**
   * Gets a user by ID from Firestore.
   */
  public User getUserById(int userId) throws ExecutionException, InterruptedException {
    DocumentReference docRef = db.collection(USERS_COLLECTION).document(String.valueOf(userId));
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();
    if (document.exists()) {
      return mapToUser(document.getData());
    }
    return null;
  }

  /**
   * Adds a user to Firestore.
   */
  public boolean addUser(User user) throws ExecutionException, InterruptedException {
    if (user == null) {
      return false;
    }
    // Check if user already exists
    User existing = getUserById(user.getUserId());
    if (existing != null) {
      return false;
    }
    DocumentReference docRef = db.collection(USERS_COLLECTION)
        .document(String.valueOf(user.getUserId()));
    ApiFuture<WriteResult> future = docRef.set(userToMap(user));
    future.get();
    return true;
  }

  /**
   * Updates a user in Firestore.
   */
  public boolean updateUser(User user) throws ExecutionException, InterruptedException {
    if (user == null) {
      return false;
    }
    DocumentReference docRef = db.collection(USERS_COLLECTION)
        .document(String.valueOf(user.getUserId()));
    ApiFuture<WriteResult> future = docRef.set(userToMap(user));
    future.get();
    return true;
  }
}

