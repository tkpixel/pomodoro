import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# fetchBoards
text = re.sub(
    r'    public CompletableFuture<List<JiraBoard>> fetchBoards\(\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<List<JiraBoard>> fetchBoards() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/agile/1.0/board");
                HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
                if (response.getStatus().getCode() == 200) {
                    JsonNode values = response.body().path("values");
                    List<JiraBoard> boards = new ArrayList<>();
                    for (JsonNode node : values) {
                        boards.add(objectMapper.treeToValue(node, JiraBoard.class));
                    }
                    return boards;
                }
                throw new RuntimeException("Failed to fetch boards: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                throw new RuntimeException("Error fetching boards", e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# fetchTasks
text = re.sub(
    r'    public CompletableFuture<List<JiraTask>> fetchTasks\(Long boardId\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<List<JiraTask>> fetchTasks(Long boardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/agile/1.0/board/" + boardId + "/issue?maxResults=50");
                HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
                if (response.getStatus().getCode() == 200) {
                    JsonNode issuesNode = response.body().path("issues");
                    List<JiraTask> tasks = new ArrayList<>();
                    for (JsonNode node : issuesNode) {
                        tasks.add(objectMapper.treeToValue(node, JiraTask.class));
                    }
                    return tasks;
                }
                throw new RuntimeException("Failed to fetch tasks: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                throw new RuntimeException("Error fetching tasks for board " + boardId, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# fetchBoardConfiguration
text = re.sub(
    r'    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration\(Long boardId\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration(Long boardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/agile/1.0/board/" + boardId + "/configuration");
                HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
                if (response.getStatus().getCode() == 200) {
                    return objectMapper.treeToValue(response.body(), BoardConfiguration.class);
                }
                throw new RuntimeException("Failed to fetch board config: HTTP " + response.getStatus().getCode());
            } catch (Exception e) {
                throw new RuntimeException("Error fetching configuration for board " + boardId, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

# assignTaskToCurrentUser
text = re.sub(
    r'    public CompletableFuture<Boolean> assignTaskToCurrentUser\(String issueKey\) \{[\s\S]*?\}\s*\}\);\s*\}',
    '''    public CompletableFuture<Boolean> assignTaskToCurrentUser(String issueKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String accountId = authService.getSavedAccountId();
                if (accountId == null || accountId.isBlank()) {
                    throw new IllegalStateException("Account ID not configured. Please test connection first.");
                }
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/assignee");
                String body = "{\\"accountId\\": \\"" + accountId + "\\"}";
                HttpResponse<JsonNode> response = jiraApiClient.put(uri, body).join();
                return response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error assigning task " + issueKey, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''', text)

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
