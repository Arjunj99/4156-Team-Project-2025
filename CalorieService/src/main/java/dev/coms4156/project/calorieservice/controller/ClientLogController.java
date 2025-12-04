package dev.coms4156.project.calorieservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.coms4156.project.calorieservice.ClientEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RestController
public class ClientLogController {

  private static final Path LOG_FILE = Path.of("logs", "client-events.log");
  private final ObjectMapper objectMapper;

  public ClientLogController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

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
