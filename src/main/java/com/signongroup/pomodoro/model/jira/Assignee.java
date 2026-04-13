package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Assignee(
    @JsonProperty("accountId") String accountId,
    @JsonProperty("displayName") String displayName,
    @JsonProperty("avatarUrls") Map<String, String> avatarUrls
) {}
