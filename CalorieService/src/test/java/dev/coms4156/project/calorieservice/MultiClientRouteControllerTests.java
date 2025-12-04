package dev.coms4156.project.calorieservice;

import dev.coms4156.project.calorieservice.controller.RouteController;
import dev.coms4156.project.calorieservice.models.Food;
import dev.coms4156.project.calorieservice.models.Recipe;
import dev.coms4156.project.calorieservice.service.MockApiService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Multi-client concurrency tests for ALL 11 endpoints.
 * Ensures clients operate independently.
 * All test use 5 simulteanous clients.
 */
public class MultiClientRouteControllerTests {

  private MockApiService mockApiService;

  @BeforeEach
  void setup() {
    mockApiService = Mockito.mock(MockApiService.class);
  }

  /** Creates a fresh MockMvc instance (isolated per client). */
  private MockMvc newMvc() {
    return MockMvcBuilders.standaloneSetup(new RouteController(mockApiService)).build();
  }

  /** Run N clients in parallel. */
  private List<MvcResult> runConcurrent(int n, Callable<MvcResult> task) throws Exception {
    ExecutorService exec = Executors.newFixedThreadPool(n);
    List<Future<MvcResult>> futures = new ArrayList<>();

    for (int i = 0; i < n; i++) {
      futures.add(exec.submit(task));
    }
    exec.shutdown();

    List<MvcResult> results = new ArrayList<>();
    for (Future<MvcResult> f : futures) {
      results.add(f.get());
    }
    return results;
  }

  /** GET /food/alternative Multi Client Testing. */
  @Test
  void multiClient_foodAlternative() throws Exception {
    Mockito.when(mockApiService.getFoodAlternatives(Mockito.anyInt()))
        .thenAnswer(inv -> {
          int id = inv.getArgument(0);
          return List.of(new Food("food" + id, id, 10, "cat"));
        });

    List<MvcResult> results = runConcurrent(5, () -> {
      int fid = (int) (Math.random() * 10000);
      return newMvc().perform(
          MockMvcRequestBuilders.get("/food/alternative")
            .param("foodId", String.valueOf(fid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** POST /food/addFood Multi Client Testing. */
  @Test
  void multiClient_addFood() throws Exception {
    Mockito.when(mockApiService.addFood(Mockito.any(Food.class))).thenReturn(true);

    List<MvcResult> results = runConcurrent(5, () -> {
      int fid = (int) (Math.random() * 10000);

      String json = """
                    {"foodName":"apple%1$d","foodId":%1$d,"calories":50,"category":"fruit"}
                    """.formatted(fid);

      return newMvc().perform(
          MockMvcRequestBuilders.post("/food/addFood")
            .contentType("application/json")
            .content(json))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
          MockMvcResultMatchers.content()
            .string(org.hamcrest.Matchers.containsString("Food added successfully")))
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** GET /client/recommendHealthy Multi Client Testing. */
  @Test
  void multiClient_recommendHealthy() throws Exception {
    Mockito.when(mockApiService.recommendHealthy(Mockito.anyInt(), Mockito.anyInt()))
        .thenAnswer(inv -> {
          int cid = inv.getArgument(0);
          return List.of(
            new Recipe("r" + cid, cid + 100, "cat", new ArrayList<>(), 0, 0, 100));
        });

    List<MvcResult> results = runConcurrent(5, () -> {
      int cid = (int) (Math.random() * 10000);

      return newMvc().perform(
          MockMvcRequestBuilders.get("/client/recommendHealthy")
            .param("clientId", String.valueOf(cid))
            .param("calorieMax", "500"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** GET /client/recommend Multi Client Testing. */
  @Test
  void multiClient_recommend() throws Exception {
    Mockito.when(mockApiService.recommend(Mockito.anyInt()))
        .thenAnswer(inv -> {
          int cid = inv.getArgument(0);
          return List.of(
            new Recipe("rec" + cid, cid + 200, "cat", new ArrayList<>(), 0, 0, 100));
        });

    List<MvcResult> results = runConcurrent(5, () -> {
      int cid = (int) (Math.random() * 10000);

      return newMvc().perform(
          MockMvcRequestBuilders.get("/client/recommend")
            .param("clientId", String.valueOf(cid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** POST /client/likeRecipe Multi Client Testing. */
  @Test
  void multiClient_likeRecipe_client() throws Exception {
    Mockito.when(mockApiService.likeRecipe(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(true);

    List<MvcResult> results = runConcurrent(5, () -> {
      int cid = (int) (Math.random() * 10000);
      int rid = cid + 500;

      return newMvc().perform(
          MockMvcRequestBuilders.post("/client/likeRecipe")
            .param("clientId", String.valueOf(cid))
            .param("recipeId", String.valueOf(rid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** GET /recipe/alternative Multi Client Testing. */
  @Test
  void multiClient_recipeAlternative() throws Exception {
    Mockito.when(mockApiService.getRecipeAlternatives(Mockito.anyInt()))
        .thenAnswer(inv -> {
          int rid = inv.getArgument(0);

          Map<String, List<Recipe>> m = new HashMap<>();
          m.put(
              "topAlternatives",
                List.of(
                  new Recipe(
                    "top" + rid, rid + 1, "cat", new ArrayList<>(), 10, 1, 200)));
          m.put(
              "randomAlternatives",
                List.of(
                  new Recipe(
                    "rand" + rid, rid + 2, "cat", new ArrayList<>(), 5, 0, 150)));
          return Optional.of(m);
        });

    List<MvcResult> results = runConcurrent(5, () -> {
      int rid = (int) (Math.random() * 10000);
      return newMvc().perform(
          MockMvcRequestBuilders.get("/recipe/alternative")
            .param("recipeId", String.valueOf(rid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** GET /recipe/totalCalorie Multi Client Testing. */
  @Test
  void multiClient_totalCalories() throws Exception {
    Mockito.when(mockApiService.getTotalCalories(Mockito.anyInt()))
        .thenReturn(Optional.of(123));

    List<MvcResult> results = runConcurrent(5, () -> {
      int rid = (int) (Math.random() * 10000);
      return newMvc().perform(
          MockMvcRequestBuilders.get("/recipe/totalCalorie")
            .param("recipeId", String.valueOf(rid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** GET /recipe/calorieBreakdown Multi Client Testing. */
  @Test
  void multiClient_calorieBreakdown() throws Exception {
    Mockito.when(mockApiService.getCalorieBreakdown(Mockito.anyInt()))
        .thenReturn(Optional.of(Map.of("egg", 70)));

    List<MvcResult> results = runConcurrent(5, () -> {
      int rid = (int) (Math.random() * 10000);
      return newMvc().perform(
          MockMvcRequestBuilders.get("/recipe/calorieBreakdown")
            .param("recipeId", String.valueOf(rid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** POST /recipe/addRecipe Multi Client Testing. */
  @Test
  void multiClient_addRecipe() throws Exception {
    Mockito.when(mockApiService.addRecipe(Mockito.any(Recipe.class)))
        .thenReturn(true);

    List<MvcResult> results = runConcurrent(5, () -> {
      int rid = (int) (Math.random() * 10000);

      String json = """
                    {"recipeName":"r%1$d","recipeId":%1$d,"category":"cat","ingredients":[]}
                    """.formatted(rid);

      return newMvc().perform(
          MockMvcRequestBuilders.post("/recipe/addRecipe")
            .contentType("application/json")
            .content(json))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** POST /recipe/viewRecipe Multi Client Testing. */
  @Test
  void multiClient_viewRecipe() throws Exception {
    Mockito.when(mockApiService.incrementViews(Mockito.anyInt()))
        .thenReturn(true);

    List<MvcResult> results = runConcurrent(5, () -> {
      int rid = (int) (Math.random() * 10000);
      return newMvc().perform(
          MockMvcRequestBuilders.post("/recipe/viewRecipe")
            .param("recipeId", String.valueOf(rid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }

  /** POST /recipe/likeRecipe Multi Client Testing. */
  @Test
  void multiClient_likeRecipe_recipe() throws Exception {
    Mockito.when(mockApiService.incrementLikes(Mockito.anyInt()))
        .thenReturn(true);

    List<MvcResult> results = runConcurrent(5, () -> {
      int rid = (int) (Math.random() * 10000);
      return newMvc().perform(
          MockMvcRequestBuilders.post("/recipe/likeRecipe")
            .param("recipeId", String.valueOf(rid)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    });

    Assertions.assertEquals(5, results.size());
  }
}
