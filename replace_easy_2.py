import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# fetchIssueTypes
text = re.sub(
    r'    public CompletableFuture<List<IssueType>> fetchIssueTypes\(String projectId\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<List<IssueType>> fetchIssueTypes(String projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issuetype");
                HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
                if (response.getStatus().getCode() == 200) {
                    JsonNode root = response.body();
                    List<IssueType> issueTypes = new ArrayList<>();
                    for (JsonNode node : root) {
                        issueTypes.add(objectMapper.treeToValue(node, IssueType.class));
                    }
                    return issueTypes;
                }
                throw new RuntimeException("Failed to fetch issue types: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                throw new RuntimeException("Error fetching issue types for project " + projectId, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# fetchPriorities
text = re.sub(
    r'    public CompletableFuture<List<Priority>> fetchPriorities\(\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<List<Priority>> fetchPriorities() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/priority");
                HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
                if (response.getStatus().getCode() == 200) {
                    JsonNode root = response.body();
                    List<Priority> priorities = new ArrayList<>();
                    for (JsonNode node : root) {
                        priorities.add(objectMapper.treeToValue(node, Priority.class));
                    }
                    return priorities;
                }
                throw new RuntimeException("Failed to fetch priorities: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                throw new RuntimeException("Error fetching priorities", e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# createIssue
text = re.sub(
    r'    public CompletableFuture<Void> createIssue\(com\.signongroup\.pomodoro\.model\.jira\.IssueCreateRequest issueCreateRequest\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<Void> createIssue(com.signongroup.pomodoro.model.jira.IssueCreateRequest issueCreateRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue");
                String body = objectMapper.writeValueAsString(issueCreateRequest);
                HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
                if (response.getStatus().getCode() < 200 || response.getStatus().getCode() >= 300) {
                    throw new RuntimeException("Failed to create issue: HTTP " + response.getStatus().getCode() + " " + response.body());
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Error creating issue", e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# fetchTicketCount
text = re.sub(
    r'    public CompletableFuture<Integer> fetchTicketCount\(String jql\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<Integer> fetchTicketCount(String jql) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/search/approximate-count");
                String body = "{\\"jql\\": \\"" + jql.replace("\\"", "\\\\\\"") + "\\"\\"}";
                HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
                if (response.getStatus().getCode() == 200) {
                    return response.body().path("count").asInt(0);
                }
                throw new RuntimeException("Failed to fetch ticket count: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                System.err.println("Error fetching ticket count for JQL: " + jql + " - " + e.getMessage());
                return 0; // Fallback to 0
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# addWorklog
text = re.sub(
    r'    public CompletableFuture<Void> addWorklog\(String issueKey, int timeSpentSeconds\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<Void> addWorklog(String issueKey, int timeSpentSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/worklog");
                String body = "{\\"timeSpentSeconds\\": " + timeSpentSeconds + "}";
                HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
                if (response.getStatus().getCode() < 200 || response.getStatus().getCode() >= 300) {
                    throw new RuntimeException("Failed to add worklog: HTTP " + response.getStatus().getCode());
                }
                return null;
            } catch (Exception e) {
                // Log and return null instead of throwing to avoid crashing the timer
                System.err.println("Error adding worklog for task " + issueKey + ": " + e.getMessage());
                return null;
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
