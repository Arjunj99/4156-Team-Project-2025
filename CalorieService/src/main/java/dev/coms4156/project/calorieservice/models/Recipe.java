
package dev.coms4156.project.calorieservice.models;

import java.util.ArrayList;

/**
 * Represents a recipe in the calorie service system.
 */
public class Recipe implements Comparable<Recipe> {
  private String recipeName;
  private int recipeId;
  private String category;
  private ArrayList<Food> ingredients;
  private int views;
  private int likes;
  private int totalCalories;

  /**
   * Complete Recipe constructor.
   *
   * @param recipeName name of the recipe.
   * @param recipeId unique id of the recipe.
   * @param category category of the recipe.
   * @param ingredients list of Food ingredients.
   * @param views number of views.
   * @param likes number of likes.
   * @param totalCalories total calories of the recipe.
   */
  public Recipe(String recipeName, int recipeId, String category,
                ArrayList<Food> ingredients, int views, int likes, int totalCalories) {
    if (recipeId < 0) {
      throw new IllegalArgumentException("Recipe ID cannot be negative");
    }
    if (views < 0) {
      throw new IllegalArgumentException("Views cannot be negative");
    }
    if (likes < 0) {
      throw new IllegalArgumentException("Likes cannot be negative");
    }
    this.recipeName = recipeName;
    this.recipeId = recipeId;
    this.category = category;
    this.ingredients = ingredients;
    this.views = views;
    this.likes = likes;
    this.totalCalories = totalCalories;
  }

  /**
   * No args constructor.
   */
  public Recipe() {
    this.recipeName = "";
    this.recipeId = 0;
    this.category = "";
    this.ingredients = new ArrayList<>();
    this.views = 0;
    this.likes = 0;
    this.totalCalories = 0;
  }

  /**
   * Calculates the total calories of the recipe by summing ingredient calories.
   *
   * @return total calorie count of the recipe.
   */
  public int getTotalCalories() {
    int total = 0;
    for (Food ingredient : ingredients) {
      total += ingredient.getCalories();
    }
    return total;
  }

  /**
   * Increments the view count of the recipe by 1.
   */
  public void incrementViews() {
    this.views++;
  }

  /**
   * Increments the like count of the recipe by 1.
   */
  public void incrementLikes() {
    this.likes++;
  }

  public String getRecipeName() {
    return recipeName;
  }

  public void setRecipeName(String recipeName) {
    this.recipeName = recipeName;
  }

  public int getRecipeId() {
    return recipeId;
  }

  /**
   * Sets the recipe ID.
   *
   * @param recipeId the recipe ID to set
   * @throws IllegalArgumentException if recipeId is negative
   */
  public void setRecipeId(int recipeId) {
    if (recipeId < 0) {
      throw new IllegalArgumentException("Recipe ID cannot be negative");
    }
    this.recipeId = recipeId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public ArrayList<Food> getIngredients() {
    return ingredients;
  }

  public void setIngredients(ArrayList<Food> ingredients) {
    this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
  }

  public int getViews() {
    return views;
  }

  /**
   * Sets the views count.
   *
   * @param views the views count to set
   * @throws IllegalArgumentException if views is negative
   */
  public void setViews(int views) {
    if (views < 0) {
      throw new IllegalArgumentException("Views cannot be negative");
    }
    this.views = views;
  }

  public int getLikes() {
    return likes;
  }

  /**
   * Sets the likes count.
   *
   * @param likes the likes count to set
   * @throws IllegalArgumentException if likes is negative
   */
  public void setLikes(int likes) {
    if (likes < 0) {
      throw new IllegalArgumentException("Likes cannot be negative");
    }
    this.likes = likes;
  }

  @Override
  public int compareTo(Recipe other) {
    return Integer.compare(this.recipeId, other.recipeId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Recipe cmpRecipe = (Recipe) obj;
    return cmpRecipe.recipeId == this.recipeId;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(recipeId);
  }

  @Override
  public String toString() {
    return String.format("(%d)\t%s - %d views, %d likes, %d calories",
        this.recipeId, this.recipeName, this.views, this.likes, this.totalCalories);
  }
}
