import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# fetchTransitions (looks like it missed fetchTransitions)
text = re.sub(
    r'    public CompletableFuture<List<JiraTransition>> fetchTransitions\(String issueKey\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<List<JiraTransition>> fetchTransitions(String issueKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions");
                HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
                if (response.getStatus().getCode() == 200) {
                    JsonNode transitionsNode = response.body().path("transitions");
                    List<JiraTransition> transitions = new ArrayList<>();
                    for (JsonNode node : transitionsNode) {
                        transitions.add(objectMapper.treeToValue(node, JiraTransition.class));
                    }
                    return transitions;
                }
                throw new RuntimeException("Failed to fetch transitions: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                throw new RuntimeException("Error fetching transitions for issue " + issueKey, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# moveTask
text = re.sub(
    r'    public CompletableFuture<Boolean> moveTask\(String issueKey, String transitionId\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<Boolean> moveTask(String issueKey, String transitionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions");
                String body = "{\\"transition\\": {\\"id\\": \\"" + transitionId + "\\"}}";
                HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
                return response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error moving task " + issueKey, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)


with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
