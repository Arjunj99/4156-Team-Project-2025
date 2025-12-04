package dev.coms4156.project.calorieservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * This class contains the unit tests for the ClientEvent class.
 */
public class ClientEventTests {

  public static ClientEvent clientEvent;

  /**
   * This method sets up our testing variables.
   */
  @BeforeAll
  public static void setUpClientEventForTesting() {
    clientEvent = new ClientEvent();
    clientEvent.setInstanceId("instance-1");
    clientEvent.setServiceClientId(123);
    clientEvent.setUserId("user-abc");
    clientEvent.setType("CLICK");
    clientEvent.setEvent("User clicked on recipe");
    clientEvent.setRecipeId(42);
    clientEvent.setRecipeTitle("Test Recipe");
    clientEvent.setTimestamp("2025-12-03T10:15:30Z");
  }

  /**
   * This method cleans up our testing variables.
   */
  @AfterAll
  public static void tearDownClientEventForTesting() {
    clientEvent = null;
  }

  /**
   * Tests that the getters return the values set in the setup method.
   */
  @Test
  public void testGettersReturnValuesSetInSetup() {
    assertNotNull(clientEvent);

    assertEquals("instance-1", clientEvent.getInstanceId());
    assertEquals(123, clientEvent.getServiceClientId());
    assertEquals("user-abc", clientEvent.getUserId());
    assertEquals("CLICK", clientEvent.getType());
    assertEquals("User clicked on recipe", clientEvent.getEvent());
    assertEquals(42, clientEvent.getRecipeId());
    assertEquals("Test Recipe", clientEvent.getRecipeTitle());
    assertEquals("2025-12-03T10:15:30Z", clientEvent.getTimestamp());
  }

  /**
   * Tests that the default constructor initializes all fields to null.
   */
  @Test
  public void testDefaultConstructorInitializesFieldsToNull() {
    ClientEvent emptyEvent = new ClientEvent();

    assertNull(emptyEvent.getInstanceId());
    assertNull(emptyEvent.getServiceClientId());
    assertNull(emptyEvent.getUserId());
    assertNull(emptyEvent.getType());
    assertNull(emptyEvent.getEvent());
    assertNull(emptyEvent.getRecipeId());
    assertNull(emptyEvent.getRecipeTitle());
    assertNull(emptyEvent.getTimestamp());
  }

  /**
   * Tests that setters correctly update the values of fields.
   */
  @Test
  public void testSettersUpdateFields() {
    ClientEvent event = new ClientEvent();

    event.setInstanceId("instance-2");
    event.setServiceClientId(456);
    event.setUserId("user-xyz");
    event.setType("VIEW");
    event.setEvent("User viewed recipe");
    event.setRecipeId(99);
    event.setRecipeTitle("Another Recipe");
    event.setTimestamp("2025-12-03T11:00:00Z");

    assertEquals("instance-2", event.getInstanceId());
    assertEquals(456, event.getServiceClientId());
    assertEquals("user-xyz", event.getUserId());
    assertEquals("VIEW", event.getType());
    assertEquals("User viewed recipe", event.getEvent());
    assertEquals(99, event.getRecipeId());
    assertEquals("Another Recipe", event.getRecipeTitle());
    assertEquals("2025-12-03T11:00:00Z", event.getTimestamp());
  }
}
