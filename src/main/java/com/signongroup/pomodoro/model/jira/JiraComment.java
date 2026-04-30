package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Auto-generated javadoc. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraComment(
    @JsonProperty("id") String id,
    @JsonProperty("author") Author author,
    @JsonProperty("body")
        Object body, // Object since it could be raw string in some v2 api or AdfDoc in v3
    @JsonProperty("renderedBody") String renderedBody,
    @JsonProperty("created") String created) {}
