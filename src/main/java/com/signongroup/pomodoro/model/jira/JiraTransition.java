package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraTransition(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("to") Status to
) {}
