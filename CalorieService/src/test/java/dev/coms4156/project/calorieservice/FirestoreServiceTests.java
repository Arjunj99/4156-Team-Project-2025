package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Unit tests for FirestoreService with mocked Firestore dependencies.
 */
@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FirestoreServiceTests {

  private FirestoreService firestoreService;
  private Firestore mockFirestore;
  private CollectionReference mockFoodCollection;
  private CollectionReference mockRecipeCollection;
  private CollectionReference mockUserCollection;
  private DocumentReference mockDocumentRef;
  private Query mockQuery;


  @BeforeAll
  public void setUpAll() {

  }

  /**
   * Sets up mocks for Firestore dependencies before each test.
   */
  @BeforeEach
  public void setUp() throws Exception {
    firestoreService = new FirestoreService();
    mockFirestore = mock(Firestore.class);
    mockFoodCollection = mock(CollectionReference.class);
    mockRecipeCollection = mock(CollectionReference.class);
    mockUserCollection = mock(CollectionReference.class);
    mockDocumentRef = mock(DocumentReference.class);
    mockQuery = mock(Query.class);
    
    Field dbField = FirestoreService.class.getDeclaredField("db");
    dbField.setAccessible(true);
    dbField.set(firestoreService, mockFirestore);
    
    when(mockFirestore.collection("food")).thenReturn(mockFoodCollection);
    when(mockFirestore.collection("recipes")).thenReturn(mockRecipeCollection);
    when(mockFirestore.collection("users")).thenReturn(mockUserCollection);
    when(mockFoodCollection.document(anyString())).thenReturn(mockDocumentRef);
    when(mockRecipeCollection.document(anyString())).thenReturn(mockDocumentRef);
    when(mockUserCollection.document(anyString())).thenReturn(mockDocumentRef);
  }

 
  @AfterAll
  public void tearDownAll() {
  }

  // ==================== FOOD OPERATIONS ====================

  @Test
  public void getAllFoodsReturnsAllFoodsTest() 
      throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
    Map<String, Object> data = createFoodMap(1, "Apple", 95, "Fruit");
    when(doc.getData()).thenReturn(data);
    docs.add(doc);
    
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(docs);
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockFoodCollection.get()).thenReturn(future);
    
    ArrayList<Food> result = firestoreService.getAllFoods();
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  public void getAllFoodsReturnsEmptyListTest() 
      throws ExecutionException, InterruptedException {
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(new ArrayList<>());
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockFoodCollection.get()).thenReturn(future);
    
    ArrayList<Food> result = firestoreService.getAllFoods();
    assertTrue(result.isEmpty());
  }

  @Test
  public void getAllFoodsThrowsExceptionTest() 
      throws ExecutionException, InterruptedException {
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenThrow(new ExecutionException("Connection failed", null));
    when(mockFoodCollection.get()).thenReturn(future);
    
    assertThrows(ExecutionException.class, () -> firestoreService.getAllFoods());
  }

  @Test
  public void getFoodByIdReturnsExistingFoodTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createFoodMap(1, "Apple", 95, "Fruit"));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    Food result = firestoreService.getFoodById(1);
    assertNotNull(result);
    assertEquals(1, result.getFoodId());
  }

  @Test
  public void getFoodByIdReturnsNullForNonExistentTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    assertNull(firestoreService.getFoodById(999));
  }

  @Test
  public void getFoodByIdBoundaryZeroTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createFoodMap(0, "Test", 100, "Test"));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    Food result = firestoreService.getFoodById(0);
    assertNotNull(result);
    assertEquals(0, result.getFoodId());
  }

  @Test
  public void addFoodSuccessTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
    when(getFuture.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(getFuture);
    
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> setFuture = mock(ApiFuture.class);
    when(setFuture.get()).thenReturn(writeResult);
    when(mockDocumentRef.set(any(Map.class))).thenReturn(setFuture);
    
    assertTrue(firestoreService.addFood(new Food("Apple", 1, 95, "Fruit")));
    verify(mockDocumentRef).set(any(Map.class));
  }

  @Test
  public void addFoodReturnsFalseForNullTest() 
      throws ExecutionException, InterruptedException {
    assertFalse(firestoreService.addFood(null));
    verify(mockDocumentRef, never()).set(any(Map.class));
  }

  @Test
  public void addFoodReturnsFalseForDuplicateTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createFoodMap(1, "Apple", 95, "Fruit"));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    assertFalse(firestoreService.addFood(new Food("Apple", 1, 95, "Fruit")));
    verify(mockDocumentRef, never()).set(any(Map.class));
  }

  @Test
  public void getFoodsByCategoryAndCaloriesReturnsMatchingFoodsTest() 
      throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
    when(doc.getData()).thenReturn(createFoodMap(1, "Apple", 95, "Fruit"));
    docs.add(doc);
    
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(docs);
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockQuery.get()).thenReturn(future);
    when(mockFoodCollection.whereEqualTo("category", "Fruit")).thenReturn(mockQuery);
    when(mockQuery.whereLessThan("calories", 100)).thenReturn(mockQuery);
    
    List<Food> result = firestoreService.getFoodsByCategoryAndCalories("Fruit", 100);
    assertEquals(1, result.size());
  }

  @Test
  public void getFoodsByCategoryAndCaloriesReturnsEmptyListTest() 
      throws ExecutionException, InterruptedException {
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(new ArrayList<>());
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockQuery.get()).thenReturn(future);
    when(mockFoodCollection.whereEqualTo("category", "Fruit")).thenReturn(mockQuery);
    when(mockQuery.whereLessThan("calories", 10)).thenReturn(mockQuery);
    
    assertTrue(firestoreService.getFoodsByCategoryAndCalories("Fruit", 10).isEmpty());
  }

  @Test
  public void deleteFoodSuccessTest() 
      throws ExecutionException, InterruptedException {
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(writeResult);
    when(mockDocumentRef.delete()).thenReturn(future);
    
    assertTrue(firestoreService.deleteFood(1));
    verify(mockDocumentRef).delete();
  }

  // ==================== RECIPE OPERATIONS ====================

  @Test
  public void getAllRecipesReturnsAllRecipesTest() 
      throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
    when(doc.getData()).thenReturn(createRecipeMap(1001, "Test Recipe", "Dessert", 200));
    docs.add(doc);
    
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(docs);
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockRecipeCollection.get()).thenReturn(future);
    
    ArrayList<Recipe> result = firestoreService.getAllRecipes();
    assertEquals(1, result.size());
  }

  @Test
  public void getAllRecipesReturnsEmptyListTest() 
      throws ExecutionException, InterruptedException {
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(new ArrayList<>());
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockRecipeCollection.get()).thenReturn(future);
    
    assertTrue(firestoreService.getAllRecipes().isEmpty());
  }

  @Test
  public void getRecipeByIdReturnsExistingRecipeTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createRecipeMap(1001, "Test Recipe", "Dessert", 200));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    Recipe result = firestoreService.getRecipeById(1001);
    assertNotNull(result);
    assertEquals(1001, result.getRecipeId());
  }

  @Test
  public void getRecipeByIdReturnsNullForNonExistentTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    assertNull(firestoreService.getRecipeById(9999));
  }

  @Test
  public void getRecipeByIdWithEmptyIngredientsTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createRecipeMap(1002, "Empty Recipe", "Dessert", 0));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    Recipe result = firestoreService.getRecipeById(1002);
    assertTrue(result.getIngredients().isEmpty());
  }

  @Test
  public void addRecipeSuccessTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
    when(getFuture.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(getFuture);
    
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> setFuture = mock(ApiFuture.class);
    when(setFuture.get()).thenReturn(writeResult);
    when(mockDocumentRef.set(any(Map.class))).thenReturn(setFuture);
    
    Recipe recipe = new Recipe("Test Recipe", 1001, "Dessert", 
        new ArrayList<>(), 0, 0, 0);
    assertTrue(firestoreService.addRecipe(recipe));
  }

  @Test
  public void addRecipeReturnsFalseForNullTest() 
      throws ExecutionException, InterruptedException {
    assertFalse(firestoreService.addRecipe(null));
  }

  @Test
  public void addRecipeReturnsFalseForDuplicateTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createRecipeMap(1001, "Existing", "Dessert", 200));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    assertFalse(firestoreService.addRecipe(
        new Recipe("Test", 1001, "Dessert", new ArrayList<>(), 0, 0, 0)));
  }

  @Test
  public void updateRecipeSuccessTest() 
      throws ExecutionException, InterruptedException {
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(writeResult);
    when(mockDocumentRef.set(any(Map.class))).thenReturn(future);
    
    Recipe recipe = new Recipe("Updated", 1001, "Dessert", new ArrayList<>(), 10, 5, 200);
    assertTrue(firestoreService.updateRecipe(recipe));
  }

  @Test
  public void updateRecipeReturnsFalseForNullTest() 
      throws ExecutionException, InterruptedException {
    assertFalse(firestoreService.updateRecipe(null));
  }

  @Test
  public void getRecipesByCategoryAndCaloriesReturnsMatchingRecipesTest() 
      throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
    when(doc.getData()).thenReturn(createRecipeMap(1001, "Test", "Dessert", 400));
    docs.add(doc);
    
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(docs);
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockQuery.get()).thenReturn(future);
    when(mockRecipeCollection.whereEqualTo("category", "Dessert")).thenReturn(mockQuery);
    when(mockQuery.whereLessThanOrEqualTo("totalCalories", 500)).thenReturn(mockQuery);
    
    List<Recipe> result = firestoreService.getRecipesByCategoryAndCalories("Dessert", 500);
    assertEquals(1, result.size());
  }

  @Test
  public void getRecipesByCaloriesReturnsMatchingRecipesTest() 
      throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
    when(doc.getData()).thenReturn(createRecipeMap(1001, "Test", "Dessert", 400));
    docs.add(doc);
    
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(docs);
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockQuery.get()).thenReturn(future);
    when(mockRecipeCollection.whereLessThanOrEqualTo("totalCalories", 500))
        .thenReturn(mockQuery);
    
    List<Recipe> result = firestoreService.getRecipesByCalories(500);
    assertEquals(1, result.size());
  }

  @Test
  public void deleteRecipeSuccessTest() 
      throws ExecutionException, InterruptedException {
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(writeResult);
    when(mockDocumentRef.delete()).thenReturn(future);
    
    assertTrue(firestoreService.deleteRecipe(1001));
  }

  // ==================== USER OPERATIONS ====================

  @Test
  public void getAllUsersReturnsAllUsersTest() 
      throws ExecutionException, InterruptedException {
    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
    when(doc.getData()).thenReturn(createUserMap(501, "Test User"));
    docs.add(doc);
    
    QuerySnapshot snapshot = mock(QuerySnapshot.class);
    when(snapshot.getDocuments()).thenReturn(docs);
    ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(snapshot);
    when(mockUserCollection.get()).thenReturn(future);
    
    // Mock getRecipeById for loading liked recipes
    DocumentReference mockRecipeDocRef = mock(DocumentReference.class);
    when(mockRecipeCollection.document(anyString())).thenReturn(mockRecipeDocRef);
    DocumentSnapshot mockRecipeDoc = mock(DocumentSnapshot.class);
    when(mockRecipeDoc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> mockRecipeFuture = mock(ApiFuture.class);
    when(mockRecipeFuture.get()).thenReturn(mockRecipeDoc);
    when(mockRecipeDocRef.get()).thenReturn(mockRecipeFuture);
    
    ArrayList<User> result = firestoreService.getAllUsers();
    assertEquals(1, result.size());
  }

  @Test
  public void getUserByIdReturnsExistingUserTest() 
      throws ExecutionException, InterruptedException {
    List<Integer> likedRecipeIds = new ArrayList<>();
    likedRecipeIds.add(1001);
    Map<String, Object> userData = createUserMap(501, "Test User");
    userData.put("likedRecipeIds", likedRecipeIds);
    
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(userData);
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    // Mock getRecipeById for loading liked recipes
    DocumentReference mockRecipeDocRef = mock(DocumentReference.class);
    when(mockRecipeCollection.document("1001")).thenReturn(mockRecipeDocRef);
    DocumentSnapshot mockRecipeDoc = mock(DocumentSnapshot.class);
    when(mockRecipeDoc.exists()).thenReturn(true);
    when(mockRecipeDoc.getData()).thenReturn(createRecipeMap(1001, "Liked", "Dessert", 200));
    ApiFuture<DocumentSnapshot> mockRecipeFuture = mock(ApiFuture.class);
    when(mockRecipeFuture.get()).thenReturn(mockRecipeDoc);
    when(mockRecipeDocRef.get()).thenReturn(mockRecipeFuture);
    
    User result = firestoreService.getUserById(501);
    assertNotNull(result);
    assertEquals(1, result.getLikedRecipes().size());
  }

  @Test
  public void getUserByIdReturnsNullForNonExistentTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    assertNull(firestoreService.getUserById(9999));
  }

  @Test
  public void getUserByIdHandlesMissingRecipesTest() 
      throws ExecutionException, InterruptedException {
    List<Integer> likedRecipeIds = new ArrayList<>();
    likedRecipeIds.add(9999); // Non-existent recipe
    Map<String, Object> userData = createUserMap(503, "User");
    userData.put("likedRecipeIds", likedRecipeIds);
    
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(userData);
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    // Mock getRecipeById to return null for missing recipe
    DocumentReference mockRecipeDocRef = mock(DocumentReference.class);
    when(mockRecipeCollection.document("9999")).thenReturn(mockRecipeDocRef);
    DocumentSnapshot mockRecipeDoc = mock(DocumentSnapshot.class);
    when(mockRecipeDoc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> mockRecipeFuture = mock(ApiFuture.class);
    when(mockRecipeFuture.get()).thenReturn(mockRecipeDoc);
    when(mockRecipeDocRef.get()).thenReturn(mockRecipeFuture);
    
    User result = firestoreService.getUserById(503);
    assertTrue(result.getLikedRecipes().isEmpty());
  }

  @Test
  public void addUserSuccessTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(false);
    ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
    when(getFuture.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(getFuture);
    
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> setFuture = mock(ApiFuture.class);
    when(setFuture.get()).thenReturn(writeResult);
    when(mockDocumentRef.set(any(Map.class))).thenReturn(setFuture);
    
    assertTrue(firestoreService.addUser(new User("Test User", 501)));
  }

  @Test
  public void addUserReturnsFalseForNullTest() 
      throws ExecutionException, InterruptedException {
    assertFalse(firestoreService.addUser(null));
  }

  @Test
  public void addUserReturnsFalseForDuplicateTest() 
      throws ExecutionException, InterruptedException {
    DocumentSnapshot doc = mock(DocumentSnapshot.class);
    when(doc.exists()).thenReturn(true);
    when(doc.getData()).thenReturn(createUserMap(501, "Existing"));
    ApiFuture<DocumentSnapshot> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(doc);
    when(mockDocumentRef.get()).thenReturn(future);
    
    assertFalse(firestoreService.addUser(new User("Test", 501)));
  }

  @Test
  public void updateUserSuccessTest() 
      throws ExecutionException, InterruptedException {
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(writeResult);
    when(mockDocumentRef.set(any(Map.class))).thenReturn(future);
    
    assertTrue(firestoreService.updateUser(new User("Updated", 501)));
  }

  @Test
  public void updateUserReturnsFalseForNullTest() 
      throws ExecutionException, InterruptedException {
    assertFalse(firestoreService.updateUser(null));
  }

  @Test
  public void deleteUserSuccessTest() 
      throws ExecutionException, InterruptedException {
    WriteResult writeResult = mock(WriteResult.class);
    ApiFuture<WriteResult> future = mock(ApiFuture.class);
    when(future.get()).thenReturn(writeResult);
    when(mockDocumentRef.delete()).thenReturn(future);
    
    assertTrue(firestoreService.deleteUser(501));
  }


  private Map<String, Object> createFoodMap(int id, String name, int calories, String category) {
    Map<String, Object> map = new HashMap<>();
    map.put("foodId", id);
    map.put("foodName", name);
    map.put("calories", calories);
    map.put("category", category);
    return map;
  }

  private Map<String, Object> createRecipeMap(int id, String name, String category, 
      int calories) {
    Map<String, Object> map = new HashMap<>();
    map.put("recipeId", id);
    map.put("recipeName", name);
    map.put("category", category);
    map.put("views", 10);
    map.put("likes", 5);
    map.put("totalCalories", calories);
    map.put("ingredients", new ArrayList<>());
    return map;
  }

  private Map<String, Object> createUserMap(int id, String username) {
    Map<String, Object> map = new HashMap<>();
    map.put("userId", id);
    map.put("username", username);
    map.put("likedRecipeIds", new ArrayList<>());
    return map;
  }
}

