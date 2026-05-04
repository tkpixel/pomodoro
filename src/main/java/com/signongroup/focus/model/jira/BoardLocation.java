package com.signongroup.focus.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BoardLocation(
    @JsonProperty("projectId") Integer projectId,
    @JsonProperty("projectKey") String projectKey,
    @JsonProperty("projectName") String projectName
) {}
