package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Auto-generated javadoc. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraChangelogItem(
    @JsonProperty("id") String id,
    @JsonProperty("author") Author author,
    @JsonProperty("created") String created,
    @JsonProperty("items") List<HistoryItem> items) {
  /** Auto-generated javadoc. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record HistoryItem(
      @JsonProperty("field") String field,
      @JsonProperty("fieldtype") String fieldtype,
      @JsonProperty("fieldId") String fieldId,
      @JsonProperty("from") String from,
      @JsonProperty("fromString") String fromString,
      @JsonProperty("to") String to,
      @JsonProperty("toString") String toStringValue) {}
}
