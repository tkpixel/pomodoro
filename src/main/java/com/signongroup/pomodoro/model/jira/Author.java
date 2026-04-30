package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/** Auto-generated javadoc. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Author(
    @JsonProperty("accountId") String accountId,
    @JsonProperty("displayName") String displayName,
    @JsonProperty("avatarUrls") Map<String, String> avatarUrls) {}
