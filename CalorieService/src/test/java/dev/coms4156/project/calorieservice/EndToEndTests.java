package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.models.User;
import dev.coms4156.project.calorieservice.service.FirestoreService;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * End-to-end tests that exercise the service via real HTTP requests against an embedded
 * server, with persistence backed by the Firestore Emulator (via the Google Cloud Firestore
 * SDK). Tests seed and clean data using FirestoreService.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
public class EndToEndTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private FirestoreService firestoreService;

  private boolean emulatorReady;

  private final Set<Integer> seededFoods = new HashSet<>();
  private final Set<Integer> seededRecipes = new HashSet<>();
  private final Set<Integer> seededUsers = new HashSet<>();

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  @BeforeEach
  void setup() {
    emulatorReady = System.getenv("FIRESTORE_EMULATOR_HOST") != null;
    assumeTrue(emulatorReady, "Firestore emulator not configured; skipping E2E HTTP tests");
  }

  @AfterEach
  void cleanup() throws Exception {
    if (!emulatorReady) {
      return;
    }
    for (Integer id : seededFoods) {
      safe(() -> firestoreService.deleteFood(id));
    }
    for (Integer id : seededRecipes) {
      safe(() -> firestoreService.deleteRecipe(id));
    }
    for (Integer id : seededUsers) {
      safe(() -> firestoreService.deleteUser(id));
    }
    seededFoods.clear();
    seededRecipes.clear();
    seededUsers.clear();
  }

  @Test
  @DisplayName("Index endpoints return welcome text")
  void indexEndpoints() {
    ResponseEntity<String> r1 = rest.getForEntity(url("/"), String.class);
    ResponseEntity<String> r2 = rest.getForEntity(url("/index"), String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, r1.getStatusCode().value());
    org.junit.jupiter.api.Assertions.assertTrue(r1.getBody().contains("Welcome to the home page"));
    org.junit.jupiter.api.Assertions.assertEquals(200, r2.getStatusCode().value());
    org.junit.jupiter.api.Assertions.assertTrue(r2.getBody().contains("Welcome to the home page"));
  }

  @Test
  @DisplayName("Add food then query alternatives; excludes target; duplicate returns 400")
  void addFoodAndAlternatives() throws Exception {
    String target = foodJson(7500, "Snack", 300);
    String alt1 = foodJson(7501, "Snack", 150);
    String alt2 = foodJson(7502, "Snack", 100);

    postJson(url("/food/addFood"), target, 200);
    postJson(url("/food/addFood"), alt1, 200);
    postJson(url("/food/addFood"), alt2, 200);
    recordFood(7500);
    recordFood(7501);
    recordFood(7502);

    URI uri = UriComponentsBuilder.fromUriString(url("/food/alternative"))
        .queryParam("foodId", 7500).build().toUri();
    ResponseEntity<String> res = rest.getForEntity(uri, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, res.getStatusCode().value());

    List<Map<String, Object>> foods = new ObjectMapper().readValue(
        res.getBody(), new TypeReference<List<Map<String, Object>>>() {});
    org.junit.jupiter.api.Assertions.assertEquals(2, foods.size());
    List<Integer> ids = foods.stream()
        .map(m -> ((Number) m.get("foodId")).intValue()).collect(Collectors.toList());
    org.junit.jupiter.api.Assertions.assertFalse(ids.contains(7500));

    ResponseEntity<String> dup = postJson(url("/food/addFood"), target, null);
    org.junit.jupiter.api.Assertions.assertEquals(400, dup.getStatusCode().value());
  }

  @Test
  @DisplayName("Food alternatives: 404 for missing, 200 message for no matches")
  void foodAlternativesNegatives() throws Exception {
    URI nf = UriComponentsBuilder.fromUriString(url("/food/alternative"))
        .queryParam("foodId", 999999).build().toUri();
    ResponseEntity<String> r404 = rest.getForEntity(nf, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, r404.getStatusCode().value());

    firestoreService.addFood(new Food("Only", 7600, 300, "OnlyC"));
    recordFood(7600);
    URI nomatch = UriComponentsBuilder.fromUriString(url("/food/alternative"))
        .queryParam("foodId", 7600).build().toUri();
    ResponseEntity<String> r200 = rest.getForEntity(nomatch, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, r200.getStatusCode().value());
    org.junit.jupiter.api.Assertions.assertTrue(
        r200.getBody().contains("No lower calorie alternatives found for food ID 7600."));
  }

  @Test
  @DisplayName("Add recipe JSON; totals and breakdown compute; duplicate and malformed handled")
  void addRecipeTotalsBreakdownAndErrors() throws Exception {
    int rid = 7700;
    String payload = recipeJson(rid, "Lunch",
        new int[] {rid + 1, 120}, new int[] {rid + 2, 80});
    postJson(url("/recipe/addRecipe"), payload, 201);
    recordRecipe(rid);

    URI total = UriComponentsBuilder.fromUriString(url("/recipe/totalCalorie"))
        .queryParam("recipeId", rid).build().toUri();
    ResponseEntity<String> tot = rest.getForEntity(total, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, tot.getStatusCode().value());
    Map<String, Object> totMap = new ObjectMapper().readValue(
        tot.getBody(), new TypeReference<Map<String, Object>>() {});
    org.junit.jupiter.api.Assertions.assertEquals(
        200, ((Number) totMap.get("totalCalories")).intValue());

    URI bd = UriComponentsBuilder.fromUriString(url("/recipe/calorieBreakdown"))
        .queryParam("recipeId", rid).build().toUri();
    ResponseEntity<String> br = rest.getForEntity(bd, String.class);
    Map<String, Object> brMap = new ObjectMapper().readValue(
        br.getBody(), new TypeReference<Map<String, Object>>() {});
    org.junit.jupiter.api.Assertions.assertEquals(2, brMap.size());
    org.junit.jupiter.api.Assertions.assertEquals(120, ((Number) brMap.get("I120")).intValue());
    org.junit.jupiter.api.Assertions.assertEquals(80, ((Number) brMap.get("I80")).intValue());

    ResponseEntity<String> dup = postJson(url("/recipe/addRecipe"), payload, null);
    org.junit.jupiter.api.Assertions.assertEquals(409, dup.getStatusCode().value());

    ResponseEntity<String> bad = postJson(url("/recipe/addRecipe"), "{\"recipeId\":}", null);
    org.junit.jupiter.api.Assertions.assertEquals(400, bad.getStatusCode().value());
  }

  @Test
  @DisplayName("Add food malformed JSON returns 400")
  void addFoodMalformedJsonReturns400() {
    ResponseEntity<String> bad = postJson(url("/food/addFood"), "{\\\"foodId\\\":}", null);
    org.junit.jupiter.api.Assertions.assertEquals(400, bad.getStatusCode().value());
  }

  @Test
  @DisplayName("Recipe alternatives: strict < filter, base excluded, 404 when missing")
  void recipeAlternativesFlows() throws Exception {
    int base = 7800;
    firestoreService.addRecipe(buildRecipe(base, "D", new int[] {200, 200}, 10));
    firestoreService.addRecipe(buildRecipe(base + 1, "D", new int[] {150, 250}, 50));
    firestoreService.addRecipe(buildRecipe(base + 2, "D", new int[] {120, 100}, 5));
    recordRecipe(base);
    recordRecipe(base + 1);
    recordRecipe(base + 2);

    URI alt = UriComponentsBuilder.fromUriString(url("/recipe/alternative"))
        .queryParam("recipeId", base).build().toUri();
    ResponseEntity<String> res = rest.getForEntity(alt, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, res.getStatusCode().value());
    Map<String, Object> map = new ObjectMapper().readValue(
        res.getBody(), new TypeReference<Map<String, Object>>() {});
    List<Map<String, Object>> top = castList(map.get("topAlternatives"));
    List<Map<String, Object>> rnd = castList(map.get("randomAlternatives"));
    List<Integer> topIds = ids(top);
    List<Integer> rndIds = ids(rnd);
    org.junit.jupiter.api.Assertions.assertFalse(topIds.contains(base));
    org.junit.jupiter.api.Assertions.assertFalse(rndIds.contains(base));
    org.junit.jupiter.api.Assertions.assertFalse(topIds.contains(base + 1));
    org.junit.jupiter.api.Assertions.assertFalse(rndIds.contains(base + 1));

    URI missing = UriComponentsBuilder.fromUriString(url("/recipe/alternative"))
        .queryParam("recipeId", 999999).build().toUri();
    ResponseEntity<String> nf = rest.getForEntity(missing, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, nf.getStatusCode().value());
  }

  @Test
  @DisplayName("Recipe alternatives: no alternatives yields empty arrays")
  void recipeAlternativesNoAlternativesEmpty() throws Exception {
    int base = 9020;
    firestoreService.addRecipe(buildRecipe(base, "SoloCat", new int[] {500}, 0));
    recordRecipe(base);
    URI alt = UriComponentsBuilder.fromUriString(url("/recipe/alternative"))
        .queryParam("recipeId", base).build().toUri();
    ResponseEntity<String> res = rest.getForEntity(alt, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, res.getStatusCode().value());

    Map<String, Object> map = new ObjectMapper().readValue(
        res.getBody(), new TypeReference<Map<String, Object>>() {});
    List<Map<String, Object>> top = castList(map.get("topAlternatives"));
    List<Map<String, Object>> rnd = castList(map.get("randomAlternatives"));
    org.junit.jupiter.api.Assertions.assertTrue(top.isEmpty());
    org.junit.jupiter.api.Assertions.assertTrue(rnd.isEmpty());
  }

  @Test
  @DisplayName("View and like recipe mutate state (200), 404 when missing")
  void viewAndLikeMutations() throws Exception {
    int rid = 7900;
    firestoreService.addRecipe(buildRecipe(rid, "D", new int[] {100, 120}, 0));
    recordRecipe(rid);
    URI view = UriComponentsBuilder.fromUriString(url("/recipe/viewRecipe"))
        .queryParam("recipeId", rid).build().toUri();
    ResponseEntity<String> r1 = rest.postForEntity(view, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, r1.getStatusCode().value());

    URI like = UriComponentsBuilder.fromUriString(url("/recipe/likeRecipe"))
        .queryParam("recipeId", rid).build().toUri();
    ResponseEntity<String> r2 = rest.postForEntity(like, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, r2.getStatusCode().value());

    URI nfV = UriComponentsBuilder.fromUriString(url("/recipe/viewRecipe"))
        .queryParam("recipeId", 999999).build().toUri();
    ResponseEntity<String> r404v = rest.postForEntity(nfV, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, r404v.getStatusCode().value());

    URI nfL = UriComponentsBuilder.fromUriString(url("/recipe/likeRecipe"))
        .queryParam("recipeId", 999999).build().toUri();
    ResponseEntity<String> r404l = rest.postForEntity(nfL, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, r404l.getStatusCode().value());
  }

  @Test
  @DisplayName("User like + recommend flows; no-likes and missing user errors")
  void userLikeAndRecommendFlows() throws Exception {
    int uid = 8000;
    firestoreService.addUser(new User("u", uid));
    recordUser(uid);
    firestoreService.addRecipe(buildRecipe(8001, "C", new int[] {10}, 0));
    firestoreService.addRecipe(buildRecipe(8002, "C", new int[] {230}, 0));
    firestoreService.addRecipe(buildRecipe(8003, "C", new int[] {231}, 0));
    recordRecipe(8001);
    recordRecipe(8002);
    recordRecipe(8003);

    URI like = UriComponentsBuilder.fromUriString(url("/user/likeRecipe"))
        .queryParam("userId", uid).queryParam("recipeId", 8001).build().toUri();
    ResponseEntity<String> ok = rest.postForEntity(like, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, ok.getStatusCode().value());

    URI rec = UriComponentsBuilder.fromUriString(url("/user/recommend"))
        .queryParam("userId", uid).build().toUri();
    ResponseEntity<String> rrec = rest.getForEntity(rec, String.class);
    List<Integer> ids = ids(new ObjectMapper().readValue(
        rrec.getBody(), new TypeReference<List<Map<String, Object>>>() {}));
    org.junit.jupiter.api.Assertions.assertFalse(ids.contains(8001));

    URI healthy = UriComponentsBuilder.fromUriString(url("/user/recommendHealthy"))
        .queryParam("userId", uid).queryParam("calorieMax", 230).build().toUri();
    ResponseEntity<String> rhe = rest.getForEntity(healthy, String.class);
    List<Integer> idsH = ids(new ObjectMapper().readValue(
        rhe.getBody(), new TypeReference<List<Map<String, Object>>>() {}));
    org.junit.jupiter.api.Assertions.assertTrue(idsH.contains(8002));
    org.junit.jupiter.api.Assertions.assertFalse(idsH.contains(8003));
    org.junit.jupiter.api.Assertions.assertFalse(idsH.contains(8001));

    int uid2 = 8005;
    firestoreService.addUser(new User("nolikes", uid2));
    recordUser(uid2);
    URI rec404 = UriComponentsBuilder.fromUriString(url("/user/recommend"))
        .queryParam("userId", uid2).build().toUri();
    ResponseEntity<String> notFound = rest.getForEntity(rec404, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, notFound.getStatusCode().value());

    int uid3 = 8010;
    firestoreService.addUser(new User("missinguser", uid3));
    firestoreService.addRecipe(buildRecipe(8011, "Any", new int[] {90}, 0));
    recordUser(uid3);
    recordRecipe(8011);
    URI he = UriComponentsBuilder.fromUriString(url("/user/recommendHealthy"))
        .queryParam("userId", uid3).queryParam("calorieMax", 100).build().toUri();
    ResponseEntity<String> okHe = rest.getForEntity(he, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, okHe.getStatusCode().value());

    URI missUser = UriComponentsBuilder.fromUriString(url("/user/recommend"))
        .queryParam("userId", 999999).build().toUri();
    ResponseEntity<String> rmu = rest.getForEntity(missUser, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, rmu.getStatusCode().value());
  }

  @Test
  @DisplayName("User likeRecipe duplicate returns 400 (already liked)")
  void userLikeRecipeDuplicateReturns400() throws Exception {
    int uid = 9100;
    int rid = 9101;
    firestoreService.addUser(new User("dup", uid));
    firestoreService.addRecipe(buildRecipe(rid, "C", new int[] {10}, 0));
    recordUser(uid);
    recordRecipe(rid);

    URI like = UriComponentsBuilder.fromUriString(url("/user/likeRecipe"))
        .queryParam("userId", uid).queryParam("recipeId", rid).build().toUri();
    ResponseEntity<String> ok = rest.postForEntity(like, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(200, ok.getStatusCode().value());

    ResponseEntity<String> bad = rest.postForEntity(like, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(400, bad.getStatusCode().value());
  }

  @Test
  @DisplayName("User likeRecipe returns 400 for missing user/recipe ids")
  void userLikeRecipeMissingReturns400() {
    URI like = UriComponentsBuilder.fromUriString(url("/user/likeRecipe"))
        .queryParam("userId", 999999).queryParam("recipeId", 999999).build().toUri();
    ResponseEntity<String> bad = rest.postForEntity(like, null, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(400, bad.getStatusCode().value());
  }

  @Test
  @DisplayName("Totals and breakdown 404 when recipe missing")
  void totalsBreakdownMissing() {
    URI t = UriComponentsBuilder.fromUriString(url("/recipe/totalCalorie"))
        .queryParam("recipeId", 999999).build().toUri();
    ResponseEntity<String> r1 = rest.getForEntity(t, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, r1.getStatusCode().value());

    URI b = UriComponentsBuilder.fromUriString(url("/recipe/calorieBreakdown"))
        .queryParam("recipeId", 999999).build().toUri();
    ResponseEntity<String> r2 = rest.getForEntity(b, String.class);
    org.junit.jupiter.api.Assertions.assertEquals(404, r2.getStatusCode().value());
  }

  private ResponseEntity<String> postJson(String url, String json, Integer expectStatus) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(json, headers);
    ResponseEntity<String> resp = rest.exchange(url, HttpMethod.POST, entity, String.class);
    if (expectStatus != null) {
      org.junit.jupiter.api.Assertions.assertEquals(
          expectStatus.intValue(), resp.getStatusCode().value());
    }
    return resp;
  }

  private String foodJson(int id, String category, int calories) {
    return String.format("""
        {
          "foodName": "F%d",
          "foodId": %d,
          "calories": %d,
          "category": "%s"
        }
        """, id, id, calories, category);
  }

  private String recipeJson(int rid, String category, int[]... idCalPairs) {
    StringBuilder ings = new StringBuilder();
    for (int i = 0; i < idCalPairs.length; i++) {
      int[] p = idCalPairs[i];
      if (i > 0) {
        ings.append(",\n");
      }
      ings.append(String.format(
          "            { \"foodName\": \"I%d\", \"foodId\": %d, \"calories\": %d, "
              + "\"category\": \"T\" }",
          p[1], p[0], p[1]));
    }
    return String.format("""
        {
          "recipeName": "R%d",
          "recipeId": %d,
          "category": "%s",
          "ingredients": [
%s
          ],
          "views": 0,
          "likes": 0
        }
        """, rid, rid, category, ings.toString());
  }

  private static List<Map<String, Object>> castList(Object o) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> list = (List<Map<String, Object>>) o;
    return list;
  }

  private static List<Integer> ids(List<Map<String, Object>> list) {
    return list.stream()
        .map(m -> ((Number) m.get("recipeId")).intValue())
        .collect(Collectors.toList());
  }

  private static void safe(ThrowingRunnable r) {
    try {
      r.run();
    } catch (Exception ignored) {
      ignored.toString();
    }
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }

  private void recordFood(int id) {
    seededFoods.add(id);
  }

  private void recordRecipe(int id) {
    seededRecipes.add(id);
  }

  private void recordUser(int id) {
    seededUsers.add(id);
  }

  private Recipe buildRecipe(int id, String category, int[] ingredientCalories, int views)
      throws Exception {
    ArrayList<Food> ings = new ArrayList<>();
    int idx = 1;
    for (int cal : ingredientCalories) {
      ings.add(new Food("I" + cal, id * 1000 + (idx++), cal, "T"));
    }
    return new Recipe("R" + id, id, category, ings, views, 0,
        java.util.Arrays.stream(ingredientCalories).sum());
  }
}
