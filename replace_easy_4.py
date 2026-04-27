import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# Add missing fetchIssueTypes
text = re.sub(
    r'    public CompletableFuture<List<Priority>> fetchPriorities\(\) \{',
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
    }

    public CompletableFuture<List<Priority>> fetchPriorities() {''', text)

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
