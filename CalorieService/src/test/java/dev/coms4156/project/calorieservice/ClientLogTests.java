package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.ClientEvent;
import dev.coms4156.project.calorieservice.controller.ClientLogController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This class contains the unit tests for the ClientLogController class.
 */
public class ClientLogTests {

  private static final Path LOG_FILE = Path.of("logs", "client-events.log");

  private static ObjectMapper objectMapper;
  private static ClientLogController controller;

  /**
   * This method sets up our testing variables and cleans up any existing log file.
   */
  @BeforeAll
  public static void setUpClientLogTests() throws IOException {
    objectMapper = new ObjectMapper();
    controller = new ClientLogController(objectMapper);

    // Ensure we start with a clean state.
    deleteLogFileIfExists();
  }

  /**
   * This method cleans up files created during testing.
   */
  @AfterAll
  public static void tearDownClientLogTests() throws IOException {
    // Clean up the log file after all tests are done.
    deleteLogFileIfExists();
  }

  private static void deleteLogFileIfExists() throws IOException {
    if (Files.exists(LOG_FILE)) {
      Files.delete(LOG_FILE);
    }
  }

  /**
   * Tests that a client event is logged successfully and
   * that the log file is created with a single JSON line.
   */
  @Test
  public void testLogClientEventCreatesFileAndWritesLine() throws IOException {
    deleteLogFileIfExists();

    ClientEvent event = new ClientEvent();

    ResponseEntity<String> response = controller.logClientEvent(event);

    // Check HTTP response
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("logged", response.getBody());

    // Check that file was created
    assertTrue(Files.exists(LOG_FILE));

    // Check that the file has exactly one line containing the JSON representation
    List<String> lines = Files.readAllLines(LOG_FILE);
    assertEquals(1, lines.size());

    String expectedJson = objectMapper.writeValueAsString(event);
    assertEquals(expectedJson, lines.get(0));
  }

  /**
   * Tests that multiple client events are appended as separate lines in the log file.
   */
  @Test
  public void testLogClientEventAppendsMultipleLines() throws IOException {
    // Ensure clean state for this specific test.
    deleteLogFileIfExists();

    ClientEvent event1 = new ClientEvent();
    ClientEvent event2 = new ClientEvent();

    controller.logClientEvent(event1);
    controller.logClientEvent(event2);

    // Verify file exists and contains two lines
    assertTrue(Files.exists(LOG_FILE));

    List<String> lines = Files.readAllLines(LOG_FILE);
    assertEquals(2, lines.size());

    String expectedJson1 = objectMapper.writeValueAsString(event1);
    String expectedJson2 = objectMapper.writeValueAsString(event2);

    assertEquals(expectedJson1, lines.get(0));
    assertEquals(expectedJson2, lines.get(1));
  }
}
