package com.signongroup.pomodoro.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraTask(
    @JsonProperty("id") String id,
    @JsonProperty("key") String key,
    @JsonProperty("fields") Fields fields
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fields(
        @JsonProperty("summary") String summary,
        @JsonProperty("assignee") Assignee assignee,
        @JsonProperty("priority") Priority priority,
        @JsonProperty("status") Status status,
        @JsonProperty("timetracking") Timetracking timetracking,
        // Epic label can be hidden in custom fields. Some standard mappings:
        @JsonProperty("customfield_10014") String epicKey, // Common for Epic Link
        @JsonProperty("customfield_10016") Double storyPoints, // Common for Story Points
        // Also map labels in case Epic isn't available
        @JsonProperty("labels") java.util.List<String> labels,
        @JsonProperty("issuetype") IssueType issuetype
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Timetracking(
        @JsonProperty("originalEstimate") String originalEstimate,
        @JsonProperty("remainingEstimate") String remainingEstimate,
        @JsonProperty("timeSpent") String timeSpent,
        @JsonProperty("originalEstimateSeconds") Long originalEstimateSeconds,
        @JsonProperty("remainingEstimateSeconds") Long remainingEstimateSeconds,
        @JsonProperty("timeSpentSeconds") Long timeSpentSeconds
    ) {}
}
