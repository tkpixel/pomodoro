package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BoardConfiguration(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("columnConfig") ColumnConfig columnConfig) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ColumnConfig(@JsonProperty("columns") List<BoardColumn> columns) {}
}
