package dev.coms4156.project.calorieservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * This class defines the Mock API Service mimicking CLIO's database. It defines
 * useful methods for accessing or modifying books.
 */
@Service
public class MockApiService {

  private ArrayList<Food> foods;
  private ArrayList<Recipe> recipes;
  private ArrayList<User> users;

  /**
   * Constructs a new {@code MockApiService} and loads data from JSON files located at
   * {@code resources/mockdata/}.
   * If files are not found, empty lists are initialized. If files are found but
   * cannot be parsed, error messages are printed and no data is loaded.
   */
  public MockApiService() {
    ObjectMapper mapper = new ObjectMapper();
    
    // Loading data basically taken from miniproject
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("mockdata/food.json")) {
      if (is == null) {
        System.err.println("Failed to find mockdata/food.json in resources.");
        foods = new ArrayList<>(0);
      } else {
        foods = mapper.readValue(is, new TypeReference<ArrayList<Food>>(){});
        System.out.println("Successfully loaded foods from mockdata/food.json.");
      }
    } catch (Exception e) {
      System.err.println("Failed to load foods: " + e.getMessage());
      foods = new ArrayList<>(0);
    }
    
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("mockdata/recipe.json")) {
      if (is == null) {
        System.err.println("Failed to find mockdata/recipe.json in resources.");
        recipes = new ArrayList<>(0);
      } else {
        recipes = mapper.readValue(is, new TypeReference<ArrayList<Recipe>>(){});
        System.out.println("Successfully loaded recipes from mockdata/recipe.json.");
      }
    } catch (Exception e) {
      System.err.println("Failed to load recipes: " + e.getMessage());
      recipes = new ArrayList<>(0);
    }
    
    try (InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("mockdata/user.json")) {
      if (is == null) {
        System.err.println("Failed to find mockdata/user.json in resources.");
        users = new ArrayList<>(0);
      } else {
        // Load users with recipe IDs first, then convert to Recipe objects
        users = loadUsersWithRecipeIds(mapper, is);
        System.out.println("Successfully loaded users from mockdata/user.json.");
      }
    } catch (Exception e) {
      System.err.println("Failed to load users: " + e.getMessage());
      users = new ArrayList<>(0);
    }
  }
  
  /**
   * Loads users from JSON, handling the conversion of recipe IDs to Recipe objects.
   *
   * @param mapper The {@code ObjectMapper} used for JSON deserialization
   * @param is The {@code InputStream} containing the user JSON data
   * @return {@code ArrayList} of {@code User} objects with liked recipes populated
   * @throws Exception if JSON parsing fails
   */
  private ArrayList<User> loadUsersWithRecipeIds(ObjectMapper mapper, InputStream is) 
      throws Exception {
    @SuppressWarnings("unchecked")
    List<Object> rawUsers = mapper.readValue(is, List.class);
    
    ArrayList<User> userList = new ArrayList<>();
    
    for (Object rawUser : rawUsers) {
      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) rawUser;
      
      String username = (String) userMap.get("username");
      Integer userId = (Integer) userMap.get("userId");
      @SuppressWarnings("unchecked")
      List<Integer> likedRecipeIds = (List<Integer>) userMap.get("likedRecipes");
      
      User user = new User(username, userId);
      
      ArrayList<Recipe> likedRecipes = new ArrayList<>();
      if (likedRecipeIds != null) {
        for (Integer recipeId : likedRecipeIds) {
          Recipe recipe = findRecipeById(recipeId);
          if (recipe != null) {
            likedRecipes.add(recipe);
          }
        }
      }
      
      user.setLikedRecipes(likedRecipes);
      userList.add(user);
    }
    
    return userList;
  }
  
  /**
   * Helper method to find a recipe by its ID.
   *
   * @param recipeId The ID of the recipe to find
   * @return The {@code Recipe} with the specified ID, or {@code null} if not found
   */
  public Recipe findRecipeById(int recipeId) {
    for (Recipe recipe : recipes) {
      if (recipe.getRecipeId() == recipeId) {
        return recipe;
      }
    }
    return null;
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
   * Helper method to find a user by their ID.
   *
   * @param userId The ID of the user to find
   * @return The {@code User} with the specified ID, or {@code null} if not found
   */
  public User findUserById(int userId) {
    for (User user : users) {
      if (user.getUserId() == userId) {
        return user;
      }
    }
    return null;
  }

  /**
   * Helper method to find a food by its ID.
   *
   * @param foodId The ID of the food to find
   * @return The {@code Food} with the specified ID, or {@code null} if not found
   */
  public Food findFoodById(int foodId) {
    for (Food food : foods) {
      if (food.getFoodId() == foodId) {
        return food;
      }
    }
    return null;
  }

  /**
   * Gets the list of all foods.
   *
   * @return {@code ArrayList} of all {@code Food} objects
   */
  public ArrayList<Food> getFoods() {
    return foods;
  }

  /**
   * Gets the list of all recipes.
   *
   * @return {@code ArrayList} of all {@code Recipe} objects
   */
  public ArrayList<Recipe> getRecipes() {
    return recipes;
  }

  /**
   * Gets the list of all users.
   *
   * @return {@code ArrayList} of all {@code User} objects
   */
  public ArrayList<User> getUsers() {
    return users;
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

    Food targetFood = findFoodById(foodId);
    
    if (targetFood == null) {
      return null;
    }
    
    final Food finalTargetFood = targetFood;
    
    List<Food> alternatives = foods.stream()
        .filter(food -> food.getCategory().equals(finalTargetFood.getCategory()))
        .filter(food -> food.getCalories() < finalTargetFood.getCalories())
        .collect(Collectors.toList());
    
    if (alternatives.size() <= 5) {
      Collections.shuffle(alternatives);
      return alternatives;
    }
    
    Collections.shuffle(alternatives);
    return alternatives.subList(0, 5);
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
    
    Food existingFood = findFoodById(food.getFoodId());
    if (existingFood != null) {
      return false;
    }
    
    foods.add(food);
    return true;
  }

  /**
   * Adds a recipe to a user's liked recipes.
   *
   * @param userId The ID of the user
   * @param recipeId The ID of the recipe to like
   * @return true if the recipe was added successfully, false if user or recipe not found
   */
  public boolean likeRecipe(int userId, int recipeId) {

    User user = findUserById(userId);
    
    if (user == null) {
      return false;
    }

    Recipe recipe = findRecipeById(recipeId);
    
    if (recipe == null) {
      return false;
    }
    
    return user.likeRecipe(recipe);
  }

  /**
   * Returns a list of recommended recipes based on user's liked recipes 
   * under calorieMax.
   *
   * @param userId The ID of the user
   * @param calorieMax Maximum calorie count for recommendations
   * @return A {@code List} of up to 10 recommended {@code Recipe} objects, 
   *         or null if user not found
   */
  public List<Recipe> recommendHealthy(int userId, int calorieMax) {

    User user = findUserById(userId);
    
    if (user == null) {
      return null; 
    }
    
    final User finalUser = user;
    
    List<String> likedCategories = finalUser.getLikedRecipes().stream()
        .map(Recipe::getCategory)
        .distinct()
        .collect(Collectors.toList());
    
    if (likedCategories.isEmpty()) {
      return new ArrayList<>();
    }
    
    List<Recipe> recommendations = recipes.stream()
        .filter(recipe -> likedCategories.contains(recipe.getCategory()))
        .filter(recipe -> recipe.getTotalCalories() <= calorieMax)
        .filter(recipe -> !finalUser.getLikedRecipes().contains(recipe)) 
        .collect(Collectors.toList());
    
    if (recommendations.size() < 10) {
      List<Recipe> additionalRecipes = recipes.stream()
          .filter(recipe -> recipe.getTotalCalories() <= calorieMax)
          .filter(recipe -> !finalUser.getLikedRecipes().contains(recipe))
          .filter(recipe -> !recommendations.contains(recipe))
          .collect(Collectors.toList());
      
      recommendations.addAll(additionalRecipes);
    }
    
    Collections.shuffle(recommendations);
    return recommendations.size() <= 10 ? recommendations : recommendations.subList(0, 10);
  }

  /**
   * Returns a list of recommended recipes based on user's liked recipes.
   *
   * @param userId The ID of the user
   * @return A {@code List} of up to 10 recommended {@code Recipe} objects, 
   *         or null if user not found or no liked recipes
   */
  public List<Recipe> recommend(int userId) {

    User user = findUserById(userId);
    
    if (user == null) {
      return null; 
    }
    
    final User finalUser = user;
    
    List<String> likedCategories = finalUser.getLikedRecipes().stream()
        .map(Recipe::getCategory)
        .distinct()
        .collect(Collectors.toList());
    
    if (likedCategories.isEmpty()) {
      return null;
    }
    
    List<Recipe> recommendations = recipes.stream()
        .filter(recipe -> likedCategories.contains(recipe.getCategory()))
        .filter(recipe -> !finalUser.getLikedRecipes().contains(recipe)) 
        .collect(Collectors.toList());
    
    if (recommendations.size() < 10) {
      List<Recipe> additionalRecipes = recipes.stream()
          .filter(recipe -> !finalUser.getLikedRecipes().contains(recipe))
          .filter(recipe -> !recommendations.contains(recipe))
          .collect(Collectors.toList());
      
      recommendations.addAll(additionalRecipes);
    }
    
    Collections.shuffle(recommendations);
    return recommendations.size() <= 10 ? recommendations : recommendations.subList(0, 10);
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
    Recipe baseRecipe = findRecipeById(recipeId);
    if (baseRecipe == null) {
      return Optional.empty();
    }

    int baseCalories = baseRecipe.getTotalCalories();
    String baseCategory = baseRecipe.getCategory();

    List<Recipe> candidates = recipes.stream()
        .filter(recipe -> recipe.getRecipeId() != recipeId)
        .filter(recipe -> sameCategory(baseCategory, recipe.getCategory()))
        .filter(recipe -> recipe.getTotalCalories() < baseCalories)
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
  }

  /**
   * Helper method to check if two categories are the same (case-insensitive).
   *
   * @param first first category {@code String}
   * @param second second category {@code String}
   * @return {@code true} if categories are the same, {@code false} otherwise
   */
  private boolean sameCategory(String first, String second) {
    if (first == null && second == null) {
      return true;
    }
    if (first == null || second == null) {
      return false;
    }
    return first.equalsIgnoreCase(second);
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
    
    Recipe existingRecipe = findRecipeById(recipe.getRecipeId());
    if (existingRecipe != null) {
      return false;
    }
    
    if (recipe.getIngredients() == null) {
      recipe.setIngredients(new ArrayList<>());
    }
    
    recipes.add(recipe);
    return true;
  }

  /**
   * Increment the recorded view count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return true when the recipe exists and the view is recorded.
   */
  public boolean incrementViews(int recipeId) {
    Recipe recipe = findRecipeById(recipeId);
    if (recipe == null) {
      return false;
    }
    recipe.incrementViews();
    return true;
  }

  /**
   * Increment the recorded like count for a recipe.
   *
   * @param recipeId identifier of the recipe.
   * @return true when the recipe exists and the like is recorded.
   */
  public boolean incrementLikes(int recipeId) {
    Recipe recipe = findRecipeById(recipeId);
    if (recipe == null) {
      return false;
    }
    recipe.incrementLikes();
    return true;
  }
}