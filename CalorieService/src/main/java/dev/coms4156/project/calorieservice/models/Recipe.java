
package dev.coms4156.project.calorieservice.model;

import java.util.ArrayList;

public class Recipe implements Comparable<Recipe> {
  private String recipeName;
  private int recipeId;
  private String category;
  private ArrayList<Food> ingredients;
  private int views;
  private int likes;

  /**
   * Complete Recipe constructor.
   *
   * @param recipeName name of the recipe.
   * @param recipeId unique id of the recipe.
   * @param category category of the recipe.
   * @param ingredients list of Food ingredients.
   * @param views number of views.
   * @param likes number of likes.
   */
  public Recipe(String recipeName, int recipeId, String category,
                ArrayList<Food> ingredients, int views, int likes) {
    this.recipeName = recipeName;
    this.recipeId = recipeId;
    this.category = category;
    this.ingredients = ingredients;
    this.views = views;
    this.likes = likes;
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

  public void setRecipeId(int recipeId) {
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

  public void setViews(int views) {
    this.views = views;
  }

  public int getLikes() {
    return likes;
  }

  public void setLikes(int likes) {
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
  public String toString() {
    return String.format("(%d)\t%s - %d views, %d likes",
        this.recipeId, this.recipeName, this.views, this.likes);
  }
}
