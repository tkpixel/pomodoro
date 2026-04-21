package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraBoard(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("type") String type,
    @JsonProperty("location") BoardLocation location
) {
    @Override
    public String toString() {
        return name;
    }
}
