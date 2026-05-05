package com.signongroup.focus.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BoardColumn(
    @JsonProperty("name") String name,
    @JsonProperty("statuses") List<StatusMapping> statuses
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatusMapping(
        @JsonProperty("id") String id
    ) {}
}
