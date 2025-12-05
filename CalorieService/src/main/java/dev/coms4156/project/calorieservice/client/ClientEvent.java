package dev.coms4156.project.calorieservice;

/**
 * Represents a log event sent from the demo client.
 */
public class ClientEvent {

  private String instanceId;
  private Integer serviceClientId;
  private String userId;
  private String type;
  private String event;
  private Integer recipeId;
  private String recipeTitle;
  private String timestamp;

  public ClientEvent() {
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Integer getServiceClientId() {
    return serviceClientId;
  }

  public void setServiceClientId(Integer serviceClientId) {
    this.serviceClientId = serviceClientId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public Integer getRecipeId() {
    return recipeId;
  }

  public void setRecipeId(Integer recipeId) {
    this.recipeId = recipeId;
  }

  public String getRecipeTitle() {
    return recipeTitle;
  }

  public void setRecipeTitle(String recipeTitle) {
    this.recipeTitle = recipeTitle;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
}
