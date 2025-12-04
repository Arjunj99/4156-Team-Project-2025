
package dev.coms4156.project.calorieservice.models;

import java.util.ArrayList;

/**
 * Represents a client in the calorie service system.
 */
public class Client implements Comparable<Client> {
  private String clientname;
  private int clientId;
  private ArrayList<Recipe> likedRecipes;

  /**
   * Complete Client constructor.
   *
   * @param clientname clientname of the client.
   * @param clientId unique id of the client.
   * @param likedRecipes list of recipes the client has liked.
   */
  public Client(String clientname, int clientId, ArrayList<Recipe> likedRecipes) {
    this.clientname = clientname;
    this.clientId = clientId;
    this.likedRecipes = likedRecipes != null ? likedRecipes : new ArrayList<>();
  }

  /**
   * Basic Client constructor without liked recipes.
   *
   * @param clientname clientname of the client.
   * @param clientId unique id of the client.
   */
  public Client(String clientname, int clientId) {
    this.clientname = clientname;
    this.clientId = clientId;
    this.likedRecipes = new ArrayList<>();
  }

  /**
   * No args constructor.
   */
  public Client() {
    this.clientname = "";
    this.clientId = 0;
    this.likedRecipes = new ArrayList<>();
  }

  /**
   * Adds a recipe to the client's liked recipes if not already liked.
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
   * Removes a recipe from the client's liked recipes if it exists.
   *
   * @param recipe the Recipe to remove from liked recipes.
   * @return {@code true} if the recipe was removed; {@code false} if not found.
   */
  public boolean unlikeRecipe(Recipe recipe) {
    if (recipe == null) {
      return false;
    }
    if (likedRecipes.contains(recipe)) {
      likedRecipes.remove(recipe);
      return true;
    }
    return false;
  }

  public String getClientname() {
    return clientname;
  }

  public void setClientname(String clientname) {
    this.clientname = clientname;
  }

  public int getClientId() {
    return clientId;
  }

  public void setClientId(int clientId) {
    this.clientId = clientId;
  }

  public ArrayList<Recipe> getLikedRecipes() {
    return likedRecipes;
  }

  public void setLikedRecipes(ArrayList<Recipe> likedRecipes) {
    this.likedRecipes = likedRecipes != null ? likedRecipes : new ArrayList<>();
  }

  @Override
  public int compareTo(Client other) {
    return Integer.compare(this.clientId, other.clientId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Client cmpClient = (Client) obj;
    return cmpClient.clientId == this.clientId;
  }

  @Override
  public String toString() {
    return String.format("(%d)\t%s - %d liked recipes",
        this.clientId, this.clientname, this.likedRecipes.size());
  }
}
