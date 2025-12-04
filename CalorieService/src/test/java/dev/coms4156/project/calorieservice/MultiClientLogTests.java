package dev.coms4156.project.calorieservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.ClientEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Multi-client log tests for /client/log endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MultiClientLogTests {

  private static final Path LOG_FILE = Path.of("logs", "client-events.log");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Cleans the log file before and after each test.
   *
   * @throws IOException if file operations fail
   */
  @BeforeEach
  @AfterEach
  public void cleanLogFile() throws IOException {
    if (Files.exists(LOG_FILE)) {
      Files.delete(LOG_FILE);
    }
    Path dir = LOG_FILE.getParent();
    if (dir != null && Files.exists(dir)) {
      if (Files.list(dir).findAny().isEmpty()) {
        Files.delete(dir);
      }
    }
  }

  /**
   * Simulates two different service clients and instances
   * sending events to the service and checks that both events
   * are logged separately and can be told apart.
   */
  @Test
  public void logsEventsFromMultipleClientsAndInstances() throws Exception {
    // First client/instance
    ClientEvent event1 = new ClientEvent();
    event1.setInstanceId("instance-A");
    event1.setServiceClientId(501);
    event1.setUserId("alice");
    event1.setType("signin");
    event1.setEvent("user_signed_in");
    event1.setRecipeId(100);
    event1.setRecipeTitle("Pasta");
    event1.setTimestamp("2025-01-01T10:00:00Z");

    // Second client/instance
    ClientEvent event2 = new ClientEvent();
    event2.setInstanceId("instance-B");
    event2.setServiceClientId(502);
    event2.setUserId("bob");
    event2.setType("like");
    event2.setEvent("user_liked_recipe");
    event2.setRecipeId(200);
    event2.setRecipeTitle("Salad");
    event2.setTimestamp("2025-01-01T10:00:05Z");

    // Send first event
    mockMvc.perform(
        post("/client/log")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(event1)))
        .andExpect(status().isCreated());

    // Send second event
    mockMvc.perform(
        post("/client/log")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(event2)))
        .andExpect(status().isCreated());

    // Both events should now be in the log file as separate JSON lines
    List<String> lines = Files.readAllLines(LOG_FILE);
    assertEquals(2, lines.size(), "Expected two logged client events");

    ClientEvent logged1 = objectMapper.readValue(lines.get(0), ClientEvent.class);
    ClientEvent logged2 = objectMapper.readValue(lines.get(1), ClientEvent.class);

    // Service can tell them apart by serviceClientId, instanceId, and userId
    assertNotEquals(logged1.getServiceClientId(), logged2.getServiceClientId());
    assertNotEquals(logged1.getInstanceId(), logged2.getInstanceId());
    assertNotEquals(logged1.getUserId(), logged2.getUserId());

    // And we still preserve which recipe each one acted on
    assertEquals(100, logged1.getRecipeId());
    assertEquals(200, logged2.getRecipeId());
  }
}
