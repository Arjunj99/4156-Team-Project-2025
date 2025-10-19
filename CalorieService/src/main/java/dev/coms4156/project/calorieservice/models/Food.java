
package dev.coms4156.project.calorieservice.model;

public class Food implements Comparable<Food> {
  private String foodName;
  private int foodId;
  private int calories;
  private String category;

  /**
   * Complete Food constructor.
   *
   * @param foodName name of the food.
   * @param foodId unique id of the food.
   * @param calories calorie count of the food.
   * @param category category of the food.
   */
  public Food(String foodName, int foodId, int calories, String category) {
    this.foodName = foodName;
    this.foodId = foodId;
    this.calories = calories;
    this.category = category;
  }

  /**
   * No args constructor.
   */
  public Food() {
    this.foodName = "";
    this.foodId = 0;
    this.calories = 0;
    this.category = "";
  }

  public String getFoodName() {
    return foodName;
  }

  public void setFoodName(String foodName) {
    this.foodName = foodName;
  }

  public int getFoodId() {
    return foodId;
  }

  public void setFoodId(int foodId) {
    this.foodId = foodId;
  }

  public int getCalories() {
    return calories;
  }

  public void setCalories(int calories) {
    this.calories = calories;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public int compareTo(Food other) {
    return Integer.compare(this.foodId, other.foodId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Food cmpFood = (Food) obj;
    return cmpFood.foodId == this.foodId;
  }

  @Override
  public String toString() {
    return String.format("(%d)\t%s - %d cal", this.foodId, this.foodName, this.calories);
  }
}
