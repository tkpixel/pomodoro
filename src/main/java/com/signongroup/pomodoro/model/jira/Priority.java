package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Priority(
    @JsonProperty("name") String name,
    @JsonProperty("iconUrl") String iconUrl
) {}
