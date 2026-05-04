package com.signongroup.focus.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Status(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("statusCategory") StatusCategory statusCategory
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusCategory(
        @JsonProperty("key") String key,
        @JsonProperty("name") String name
    ) {}
}
