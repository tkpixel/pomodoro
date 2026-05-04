package com.signongroup.focus.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IssueFields(
    @JsonProperty("project") Project project,
    @JsonProperty("summary") String summary,
    @JsonProperty("issuetype") IssueType issuetype,
    @JsonProperty("priority") Priority priority,
    @JsonProperty("assignee") Assignee assignee,
    @JsonProperty("description") AdfDoc description,
    @JsonProperty("timetracking") JiraTask.Timetracking timetracking
) {}
