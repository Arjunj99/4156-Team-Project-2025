
package dev.coms4156.project.calorieservice.models;

import java.util.ArrayList;

/**
 * Represents a user in the calorie service system.
 */
public class User implements Comparable<User> {
  private String username;
  private int userId;
  private ArrayList<Recipe> likedRecipes;

  /**
   * Complete User constructor.
   *
   * @param username username of the user.
   * @param userId unique id of the user.
   * @param likedRecipes list of recipes the user has liked.
   */
  public User(String username, int userId, ArrayList<Recipe> likedRecipes) {
    this.username = username;
    this.userId = userId;
    this.likedRecipes = likedRecipes;
  }

  /**
   * Basic User constructor without liked recipes.
   *
   * @param username username of the user.
   * @param userId unique id of the user.
   */
  public User(String username, int userId) {
    this.username = username;
    this.userId = userId;
    this.likedRecipes = new ArrayList<>();
  }

  /**
   * No args constructor.
   */
  public User() {
    this.username = "";
    this.userId = 0;
    this.likedRecipes = new ArrayList<>();
  }

  /**
   * Adds a recipe to the user's liked recipes if not already liked.
   *
   * @param recipe the Recipe to add to liked recipes.
   * @return {@code true} if the recipe was added; {@code false} if already liked.
   */
  public boolean likeRecipe(Recipe recipe) {
    if (!likedRecipes.contains(recipe)) {
      likedRecipes.add(recipe);
      recipe.incrementLikes();
      return true;
    }
    return false;
  }

  /**
   * Removes a recipe from the user's liked recipes if it exists.
   *
   * @param recipe the Recipe to remove from liked recipes.
   * @return {@code true} if the recipe was removed; {@code false} if not found.
   */
  public boolean unlikeRecipe(Recipe recipe) {
    if (likedRecipes.contains(recipe)) {
      likedRecipes.remove(recipe);
      return true;
    }
    return false;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public ArrayList<Recipe> getLikedRecipes() {
    return likedRecipes;
  }

  public void setLikedRecipes(ArrayList<Recipe> likedRecipes) {
    this.likedRecipes = likedRecipes != null ? likedRecipes : new ArrayList<>();
  }

  @Override
  public int compareTo(User other) {
    return Integer.compare(this.userId, other.userId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    User cmpUser = (User) obj;
    return cmpUser.userId == this.userId;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(userId);
  }

  @Override
  public String toString() {
    return String.format("(%d)\t%s - %d liked recipes",
        this.userId, this.username, this.likedRecipes.size());
  }
}
