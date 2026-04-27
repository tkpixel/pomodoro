import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# Replace imports
text = re.sub(
    r'import java\.net\.http\.HttpClient;\nimport java\.net\.http\.HttpRequest;\nimport java\.net\.http\.HttpResponse;\nimport java\.nio\.charset\.StandardCharsets;\nimport java\.time\.Duration;\nimport java\.util\.ArrayList;\nimport java\.util\.Base64;\nimport java\.util\.List;\nimport java\.util\.concurrent\.CompletableFuture;',
    '''import io.micronaut.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;''',
    text
)

# Class sig and fields
text = re.sub(
    r'public class JiraBoardService \{\n\n    private final JiraAuthService authService;\n    private final HttpClient httpClient;\n    private final ObjectMapper objectMapper;\n\n    @Inject\n    public JiraBoardService\(JiraAuthService authService\) \{\n        this\.authService = authService;\n        this\.httpClient = HttpClient\.newBuilder\(\)\n                \.connectTimeout\(Duration\.ofSeconds\(10\)\)\n                \.build\(\);\n        this\.objectMapper = new ObjectMapper\(\);\n    \}',
    '''public class JiraBoardService {

    private final JiraAuthService authService;
    private final JiraApiClient jiraApiClient;
    private final ObjectMapper objectMapper;
    private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Inject
    public JiraBoardService(JiraAuthService authService, JiraApiClient jiraApiClient, ObjectMapper objectMapper) {
        this.authService = authService;
        this.jiraApiClient = jiraApiClient;
        this.objectMapper = objectMapper;
    }''',
    text
)

text = re.sub(
    r'    private String getAuthHeader\(\) \{[\s\S]*?\}\n\n',
    '',
    text
)

# fetchBoards
text = re.sub(
    r'''    public CompletableFuture<List<JiraBoard>> fetchBoards\(\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchTasks
text = re.sub(
    r'''    public CompletableFuture<List<JiraTask>> fetchTasks\(String boardId\) \{[\s\S]*?\}\n        \}\);''',
    '''    public CompletableFuture<List<JiraTask>> fetchTasks(String boardId) {
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchBoardConfiguration
text = re.sub(
    r'''    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration\(String boardId\) \{[\s\S]*?\}\n        \}\);''',
    '''    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration(String boardId) {
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchTransitions
text = re.sub(
    r'''    public CompletableFuture<List<JiraTransition>> fetchTransitions\(String issueKey\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# moveTask
text = re.sub(
    r'''    public CompletableFuture<Boolean> moveTask\(String issueKey, String transitionId\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# assignTaskToCurrentUser
text = re.sub(
    r'''    public CompletableFuture<Boolean> assignTaskToCurrentUser\(String issueKey\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchIssueTypes
text = re.sub(
    r'''    public CompletableFuture<List<IssueType>> fetchIssueTypes\(String projectId\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchPriorities
text = re.sub(
    r'''    public CompletableFuture<List<Priority>> fetchPriorities\(\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# createIssue
text = re.sub(
    r'''    public CompletableFuture<Void> createIssue\(com\.signongroup\.pomodoro\.model\.jira\.IssueCreateRequest issueCreateRequest\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchTicketCount
text = re.sub(
    r'''    public CompletableFuture<Integer> fetchTicketCount\(String jql\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

# addWorklog
text = re.sub(
    r'''    public CompletableFuture<Void> addWorklog\(String issueKey, int timeSpentSeconds\) \{[\s\S]*?\}\n        \}\);''',
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
        }, VIRTUAL_EXECUTOR);''',
    text
)

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
