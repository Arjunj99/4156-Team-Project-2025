package dev.coms4156.project.calorieservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.client.ClientEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for logging client events to a local log file.
 */
@RestController
public class ClientLogController {

  private static final Path LOG_FILE = Path.of("logs", "client-events.log");
  private final ObjectMapper objectMapper;

  /**
   * Constructs a {@code ClientLogController} with the provided {@link ObjectMapper}.
   *
   * @param objectMapper the object mapper used to serialize client events
   */
  public ClientLogController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Logs a client event to the local log file.
   *
   * @param event the client event to log
   * @return a response indicating whether the event was logged successfully
   */
  @PostMapping("/client/log")
  public ResponseEntity<String> logClientEvent(@RequestBody ClientEvent event) {
    try {
      ensureLogFile();
      appendEvent(event);
      return ResponseEntity.status(HttpStatus.CREATED).body("logged");
    } catch (IOException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Failed to write client log");
    }
  }

  private void ensureLogFile() throws IOException {
    Path dir = LOG_FILE.getParent();
    if (dir != null && !Files.exists(dir)) {
      Files.createDirectories(dir);
    }
    if (!Files.exists(LOG_FILE)) {
      Files.createFile(LOG_FILE);
    }
  }

  private synchronized void appendEvent(ClientEvent event) throws IOException {
    String json = objectMapper.writeValueAsString(event);
    String line = json + System.lineSeparator();
    Files.writeString(LOG_FILE, line, StandardOpenOption.APPEND);
  }
}
