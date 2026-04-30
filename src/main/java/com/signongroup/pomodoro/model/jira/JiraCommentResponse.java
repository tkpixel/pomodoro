package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Auto-generated javadoc. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraCommentResponse(
    @JsonProperty("comments") List<JiraComment> comments, @JsonProperty("total") int total) {}
