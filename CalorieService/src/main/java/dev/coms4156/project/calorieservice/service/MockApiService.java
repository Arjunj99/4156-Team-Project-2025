package dev.coms4156.project.calorieservice.service;

import dev.coms4156.project.calorieservice.models.Client;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * This class defines the API Service that uses Firestore for data persistence.
 * It provides methods for accessing or modifying foods, recipes, and clients.
 */
@Service
public class MockApiService {

  private final FirestoreService firestoreService;
  private boolean testMode = false;

  /**
   * Constructs a new {@code MockApiService} with FirestoreService dependency injection.
   *
   * @param firestoreService The FirestoreService to use for database operations
   */
  public MockApiService(FirestoreService firestoreService) {
    this.firestoreService = firestoreService;
  }

  /**
   * Removes test data from Firestore.
   * This method is intended for test cleanup purposes.
   *
   * @param testId the ID threshold for test data (removes items with ID >= testId)
   */
  public void cleanupTestData(int testId) {
    if (testMode) {
      return;
    }
    try {
      // Note: Firestore deletion would need to be implemented in FirestoreService
      // For now, this is a placeholder that maintains the interface
      System.out.println("Test data cleanup requested for IDs >= " + testId);
    } catch (Exception e) {
      System.err.println("Failed to cleanup test data: " + e.getMessage());
    }
  }

  public void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }

  /**
   * Helper method to find a recipe by its ID.
   *
   * @param recipeId The ID of the recipe to find
   * @return The {@code Recipe} with the specified ID, or {@code null} if not found
   */
  public Recipe findRecipeById(int recipeId) {
    try {
      return firestoreService.getRecipeById(recipeId);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error finding recipe: " + e.getMessage());
      return null;
    }
  }

  /**
   * Retrieve a recipe by its identifier.
   *
   * @param recipeId identifier of the recipe to fetch.
   * @return {@code Optional} containing the {@code Recipe} if present,
   *         or empty if not found
   */
  public Optional<Recipe> getRecipeById(int recipeId) {
    return Optional.ofNullable(findRecipeById(recipeId));
  }

  /**
   * Helper method to find a client by their ID.
   *
   * @param clientId The ID of the client to find
   * @return The {@code Client} with the specified ID, or {@code null} if not found
   */
  public Client findClientById(int clientId) {
    try {
      return firestoreService.getClientById(clientId);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error finding client: " + e.getMessage());
      return null;
    }
  }

  /**
   * Helper method to find a food by its ID.
   *
   * @param foodId The ID of the food to find
   * @return The {@code Food} with the specified ID, or {@code null} if not found
   */
  public Food findFoodById(int foodId) {
    try {
      return firestoreService.getFoodById(foodId);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error finding food: " + e.getMessage());
      return null;
    }
  }

  /**
   * Adds a new client to the service.
   *
   * @param client the client to add
   * @return true if the client was added successfully, false if client is null or already exists
   */
  public boolean addClient(Client client) {
    if (client == null) {
      return false;
    }
    try {
      return firestoreService.addClient(client);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error adding client: " + e.getMessage());
      return false;
    }
  }

  /**
   * Gets the list of all foods.
   *
   * @return {@code ArrayList} of all {@code Food} objects
   */
  public ArrayList<Food> getFoods() {
    try {
      return firestoreService.getAllFoods();
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error getting foods: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Gets the list of all recipes.
   *
   * @return {@code ArrayList} of all {@code Recipe} objects
   */
  public ArrayList<Recipe> getRecipes() {
    try {
      return firestoreService.getAllRecipes();
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error getting recipes: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Gets the list of all clients.
   *
   * @return {@code ArrayList} of all {@code Client} objects
   */
  public ArrayList<Client> getClients() {
    try {
      return firestoreService.getAllClients();
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error getting clients: " + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Returns up to 5 random foods of the same category with lower calorie
   * count than the specified food.
   *
   * @param foodId The ID of the food to find alternatives for
   * @return A {@code List} of up to 5 random {@code Food} objects from the
   *         same category with lower calories, or null if food not found
   */
  public List<Food> getFoodAlternatives(int foodId) {
    try {
      Food targetFood = findFoodById(foodId);

      if (targetFood == null) {
        return null;
      }

      // Get foods from Firestore with category and calorie filters
      List<Food> alternatives = firestoreService.getFoodsByCategoryAndCalories(
          targetFood.getCategory(),
          targetFood.getCalories()
      );

      if (alternatives.isEmpty()) {
        return new ArrayList<>();
      }

      if (alternatives.size() <= 5) {
        Collections.shuffle(alternatives);
        return alternatives;
      }

      Collections.shuffle(alternatives);
      return alternatives.subList(0, 5);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error getting food alternatives: " + e.getMessage());
      return null;
    }
  }

  /**
   * Adds a new food to the service.
   *
   * @param food The {@code Food} object to add
   * @return true if the food was added successfully, false if food is null or already exists
   */
  public boolean addFood(Food food) {
    if (food == null) {
      return false;
    }
    try {
      return firestoreService.addFood(food);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error adding food: " + e.getMessage());
      return false;
    }
  }

  /**
   * Adds a recipe to a client's liked recipes.
   *
   * @param clientId The ID of the client
   * @param recipeId The ID of the recipe to like
   * @return true if the recipe was added successfully, false if client or recipe not found
   */
  public boolean likeRecipe(int clientId, int recipeId) {
    try {
      Client client = findClientById(clientId);

      if (client == null) {
        return false;
      }

      Recipe recipe = findRecipeById(recipeId);

      if (recipe == null) {
        return false;
      }

      boolean result = client.likeRecipe(recipe);
      if (result) {

        firestoreService.updateRecipe(recipe);
        // Update client with new liked recipe
        firestoreService.updateClient(client);
      }
      return result;
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error liking recipe: " + e.getMessage());
      return false;
    }
  }

  /**
   * Returns a list of recommended recipes based on client's liked recipes
   * under calorieMax.
   *
   * @param clientId The ID of the client
   * @param calorieMax Maximum calorie count for recommendations
   * @return A {@code List} of up to 10 recommended {@code Recipe} objects,
   *         or null if client not found
   */
  public List<Recipe> recommendHealthy(int clientId, int calorieMax) {
    try {
      Client client = findClientById(clientId);

      if (client == null) {
        return null;
      }

      final Client finalClient = client;

      List<String> likedCategories = finalClient.getLikedRecipes().stream()
          .map(Recipe::getCategory)
          .distinct()
          .collect(Collectors.toList());

      if (likedCategories.isEmpty()) {
        // If no liked categories, return any recipes under calorieMax
        List<Recipe> allRecipes = firestoreService.getRecipesByCalories(calorieMax);
        Collections.shuffle(allRecipes);
        return allRecipes.size() <= 10 ? allRecipes : allRecipes.subList(0, 10);
      }

      // Get recipes by category and calories
      List<Recipe> categoryRecipes = new ArrayList<>();
      for (String category : likedCategories) {
        List<Recipe> recipes = firestoreService.getRecipesByCategoryAndCalories(
            category, calorieMax);
        categoryRecipes.addAll(recipes);
      }

      // Filter out already liked recipes
      List<Recipe> recommendations = categoryRecipes.stream()
          .filter(recipe -> !finalClient.getLikedRecipes().contains(recipe))
          .collect(Collectors.toList());

      if (recommendations.size() < 10) {
        // Fill with other recipes under calorieMax
        List<Recipe> additionalRecipes = firestoreService.getRecipesByCalories(calorieMax);
        List<Recipe> filteredAdditional = additionalRecipes.stream()
            .filter(recipe -> !finalClient.getLikedRecipes().contains(recipe))
            .filter(recipe -> !recommendations.contains(recipe))
            .collect(Collectors.toList());

        recommendations.addAll(filteredAdditional);
      }

      Collections.shuffle(recommendations);
      return recommendations.size() <= 10 ? recommendations : recommendations.subList(0, 10);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error getting healthy recommendations: " + e.getMessage());
      return null;
    }
  }

  /**
   * Returns a list of recommended recipes based on client's liked recipes.
   *
   * @param clientId The ID of the client
   * @return A {@code List} of up to 10 recommended {@code Recipe} objects,
   *         or null if client not found or no liked recipes
   */
  public List<Recipe> recommend(int clientId) {
    try {
      Client client = findClientById(clientId);

      if (client == null) {
        return null;
      }

      final Client finalClient = client;

      List<String> likedCategories = finalClient.getLikedRecipes().stream()
          .map(Recipe::getCategory)
          .distinct()
          .collect(Collectors.toList());

      if (likedCategories.isEmpty()) {
        return null;
      }

      // Use Firestore queries to get recipes by category instead of fetching all
      // Use a very high calorie limit to effectively get all recipes in each category
      List<Recipe> categoryRecipes = new ArrayList<>();
      for (String category : likedCategories) {
        List<Recipe> recipes = firestoreService.getRecipesByCategoryAndCalories(
            category, Integer.MAX_VALUE);
        categoryRecipes.addAll(recipes);
      }

      // Filter out already liked recipes
      List<Recipe> recommendations = categoryRecipes.stream()
          .filter(recipe -> !finalClient.getLikedRecipes().contains(recipe))
          .collect(Collectors.toList());

      if (recommendations.size() < 10) {
        // Get additional recipes from all categories, excluding already liked ones
        List<Recipe> additionalRecipes = firestoreService.getRecipesByCalories(Integer.MAX_VALUE);
        List<Recipe> filteredAdditional = additionalRecipes.stream()
            .filter(recipe -> !finalClient.getLikedRecipes().contains(recipe))
            .filter(recipe -> !recommendations.contains(recipe))
            .collect(Collectors.toList());

        recommendations.addAll(filteredAdditional);
      }

      Collections.shuffle(recommendations);
      return recommendations.size() <= 10 ? recommendations : recommendations.subList(0, 10);
    } catch (Exception e) {
      System.err.println("Error getting recommendations: " + e.getMessage());
      return null;
    }
  }

  /**
   * Find alternate recipes in the same category with lower total calories.
   *
   * @param recipeId identifier of the recipe to compare against.
   * @return {@code Optional} containing a {@code Map} with two lists: topAlternatives
   *         (up to 3 top-viewed recipes) and randomAlternatives (up to 3 random recipes),
   *         or empty if recipe not found
   */
  public Optional<Map<String, List<Recipe>>> getRecipeAlternatives(int recipeId) {
    try {
      Recipe baseRecipe = findRecipeById(recipeId);
      if (baseRecipe == null) {
        return Optional.empty();
      }

      int baseCalories = baseRecipe.getTotalCalories();
      String baseCategory = baseRecipe.getCategory();

      // Use Firestore query to get recipes in same category with lower calories
      // Use baseCalories - 1 to get only recipes with calories strictly less than base
      List<Recipe> candidates = firestoreService.getRecipesByCategoryAndCalories(
          baseCategory, baseCalories - 1);

      // Filter out the base recipe itself
      candidates = candidates.stream()
          .filter(recipe -> recipe.getRecipeId() != recipeId)
          .collect(Collectors.toList());

      List<Recipe> topAlternatives = candidates.stream()
          .sorted((first, second) -> Integer.compare(second.getViews(), first.getViews()))
          .limit(3)
          .collect(Collectors.toList());

      List<Recipe> randomPool = new ArrayList<>(candidates);
      randomPool.removeAll(topAlternatives);
      Collections.shuffle(randomPool);

      List<Recipe> randomAlternatives = randomPool.stream()
          .limit(3)
          .collect(Collectors.toList());

      Map<String, List<Recipe>> response = new HashMap<>();
      response.put("topAlternatives", topAlternatives);
      response.put("randomAlternatives", randomAlternatives);
      return Optional.of(response);
    } catch (Exception e) {
      System.err.println("Error getting recipe alternatives: " + e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Calculate the total calorie count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return {@code Optional} containing the total calorie count as an {@code Integer},
   *         or empty if recipe not found
   */
  public Optional<Integer> getTotalCalories(int recipeId) {
    Recipe recipe = findRecipeById(recipeId);
    if (recipe == null) {
      return Optional.empty();
    }
    return Optional.of(recipe.getTotalCalories());
  }

  /**
   * Produce a calorie breakdown for each ingredient within a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return {@code Optional} containing an ordered {@code Map} of ingredient
   *         names to calorie counts, or empty if recipe not found
   */
  public Optional<Map<String, Integer>> getCalorieBreakdown(int recipeId) {
    Recipe recipe = findRecipeById(recipeId);
    if (recipe == null) {
      return Optional.empty();
    }
    Map<String, Integer> breakdown = new LinkedHashMap<>();
    for (Food ingredient : recipe.getIngredients()) {
      breakdown.put(ingredient.getFoodName(), ingredient.getCalories());
    }
    return Optional.of(breakdown);
  }

  /**
   * Store a new recipe if the identifier has not been used.
   *
   * @param recipe {@code Recipe} object to persist.
   * @return true when the recipe is added; false if recipe is null or the ID already exists
   */
  public boolean addRecipe(Recipe recipe) {
    if (recipe == null) {
      return false;
    }

    if (recipe.getIngredients() == null) {
      recipe.setIngredients(new ArrayList<>());
    }

    try {
      return firestoreService.addRecipe(recipe);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error adding recipe: " + e.getMessage());
      return false;
    }
  }

  /**
   * Increment the recorded view count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return true when the recipe exists and the view is recorded.
   */
  public boolean incrementViews(int recipeId) {
    try {
      Recipe recipe = findRecipeById(recipeId);
      if (recipe == null) {
        return false;
      }
      recipe.incrementViews();
      return firestoreService.updateRecipe(recipe);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error incrementing views: " + e.getMessage());
      return false;
    }
  }

  /**
   * Increment the recorded like count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return true when the recipe exists and the like is recorded.
   */
  public boolean incrementLikes(int recipeId) {
    try {
      Recipe recipe = findRecipeById(recipeId);
      if (recipe == null) {
        return false;
      }
      recipe.incrementLikes();
      return firestoreService.updateRecipe(recipe);
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Error incrementing likes: " + e.getMessage());
      return false;
    }
  }
}
