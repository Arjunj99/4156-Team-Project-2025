# 4156-Team-Project-2025 - Team Hello World
This is the GitHub repository for the **Team Project** associated with COMS 4156 Advanced Software Engineering.
Our group, **Hello World**, consists of the following members
- Arjun Somekawa (as7423)
- Jonathan Tavarez (jt3481)
- Songhee Beck (sb4446)
- Valentino Vitale (vv2343)

# List of all Functions, and Endpoints
## Food.java
### Variables
- foodName (string)
- foodId (int)
- calories (int)
- category (string)
### Functions

## Recipe.java
### Variables
- recipeName (string)
- recipeId (int)
- category (string)
- ingredients (list<Food>)
- views (int)
- likes (int)
### Functions

## User.java
### Variables
- username (string)
- userId (int)
- likedRecipes (list<Recipes>)
### Functions

## Model.java
### Variables
### Functions

## Service.java
### Variables

### Functions
#### /food/alternative
* Expected Input Parameters: foodId (int)
* Expected Output: foodAlternatives (ResponseEntity<?>)
* Returns 5 random foods of the same food category with lower calorie count.

#### /recipe/alternative
* Expected Input Parameters: recipeId (int)
* Expected Output: recipeAlternatives (ResponseEntity<?>)
* Returns 3 recipes of same recipe category with lower calorie count and 
highest views, as well as 3 random recipes of the same category with lower calorie count.

#### /recipe/totalCalorie
* Expected Input Parameters: recipeId (int)
* Expected Output: calorie (ResponseEntity<?>)
* Returns the estimated total calorie of a given recipe.

#### /recipe/calorieBreakdown
* Expected Input Parameters: recipeId (int)
* Expected Output: calorieBreakdown (ResponseEntity<?>)
* Returns a dict of each ingredient in recipe and estimated calorie value for that ingredient.

#### /user/recommend
* Expected Input Parameters: userId (int)
* Expected Output: recipes (ResponseEntity<?>)
* Returns a list of 10 recommended recipes based on user's liked recipes.

#### /user/recommendHealthy
* Expected Input Parameters: userId (int), calorieMax (int)
* Expected Output: recipes (ResponseEntity<?>)
* Returns a list of 10 recommended recipes based on user's liked recipes under the calorieMax value.

#### /user/likeRecipe
* Expected Input Parameters: userId (int), recipeId (int)
* Expected Output: status (ResponseEntity<?>)
* Adds a recipe to user's likedRecipes.

#### /recipe/addRecipe
* Expected Input Parameters: recipe (Recipe)
* Expected Output: status (ResponseEntity<?>)
* Adds a recipe to service.

#### /food/addFood
* Expected Input Parameters: food (Food)
* Expected Output: status (ResponseEntity<?>)
* Adds a food to service.

#### /recipe/viewRecipe
* Expected Input Parameters: recipeId (int)
* Expected Output: status (ResponseEntity<?>)
* Increment Recipe's Views.

#### /recipe/likeRecipe
* Expected Input Parameters: recipeId (int)
* Expected Output: status (ResponseEntity<?>)
* Increment Recipe's likes.
