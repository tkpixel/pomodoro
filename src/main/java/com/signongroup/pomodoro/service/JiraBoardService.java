package com.signongroup.pomodoro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signongroup.pomodoro.model.jira.BoardConfiguration;
import com.signongroup.pomodoro.model.jira.IssueType;
import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraTask;
import com.signongroup.pomodoro.model.jira.JiraTransition;
import com.signongroup.pomodoro.model.jira.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class JiraBoardService {

    private final JiraAuthService authService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Inject
    public JiraBoardService(JiraAuthService authService) {
        this.authService = authService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private String getAuthHeader() {
        String email = authService.getSavedEmail();
        String token = authService.getSavedToken();
        if (email == null || token == null || email.isBlank() || token.isBlank()) {
            throw new IllegalStateException("Jira credentials not configured.");
        }
        String auth = email + ":" + token;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private String getBaseUrl() {
        String url = authService.getSavedUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Jira URL not configured.");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public CompletableFuture<List<JiraBoard>> fetchBoards() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/agile/1.0/board");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode values = root.path("values");
                    List<JiraBoard> boards = new ArrayList<>();
                    for (JsonNode node : values) {
                        boards.add(objectMapper.treeToValue(node, JiraBoard.class));
                    }
                    return boards;
                } else {
                    throw new RuntimeException("Failed to fetch boards: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching boards", e);
            }
        });
    }

    public CompletableFuture<List<JiraTask>> fetchTasks(Long boardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/agile/1.0/board/" + boardId + "/issue?maxResults=50");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode issues = root.path("issues");
                    List<JiraTask> tasks = new ArrayList<>();
                    for (JsonNode node : issues) {
                        tasks.add(objectMapper.treeToValue(node, JiraTask.class));
                    }
                    return tasks;
                } else {
                    throw new RuntimeException("Failed to fetch tasks: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching tasks for board " + boardId, e);
            }
        });
    }

    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration(Long boardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/agile/1.0/board/" + boardId + "/configuration");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), BoardConfiguration.class);
                } else {
                    throw new RuntimeException("Failed to fetch board configuration: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching configuration for board " + boardId, e);
            }
        });
    }

    public CompletableFuture<List<JiraTransition>> fetchTransitions(String issueKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/issue/" + issueKey + "/transitions");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode transitionsNode = root.path("transitions");
                    List<JiraTransition> transitions = new ArrayList<>();
                    for (JsonNode node : transitionsNode) {
                        transitions.add(objectMapper.treeToValue(node, JiraTransition.class));
                    }
                    return transitions;
                } else {
                    throw new RuntimeException("Failed to fetch transitions: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching transitions for issue " + issueKey, e);
            }
        });
    }

    public CompletableFuture<Boolean> moveTask(String issueKey, String transitionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/issue/" + issueKey + "/transitions");

                String body = "{\"transition\": {\"id\": \"" + transitionId + "\"}}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() >= 200 && response.statusCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error moving task " + issueKey, e);
            }
        });
    }

    public CompletableFuture<Boolean> assignTaskToCurrentUser(String issueKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String accountId = authService.getSavedAccountId();
                if (accountId == null || accountId.isBlank()) {
                    throw new IllegalStateException("Account ID not configured. Please test connection first.");
                }

                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/issue/" + issueKey + "/assignee");

                String body = "{\"accountId\": \"" + accountId + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() >= 200 && response.statusCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error assigning task " + issueKey, e);
            }
        });
    }

    public CompletableFuture<List<IssueType>> fetchIssueTypes(String projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/issuetype");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    List<IssueType> issueTypes = new ArrayList<>();
                    for (JsonNode node : root) {
                        issueTypes.add(objectMapper.treeToValue(node, IssueType.class));
                    }
                    return issueTypes;
                } else {
                    throw new RuntimeException("Failed to fetch issue types: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching issue types for project " + projectId, e);
            }
        });
    }

    public CompletableFuture<List<Priority>> fetchPriorities() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/priority");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    List<Priority> priorities = new ArrayList<>();
                    for (JsonNode node : root) {
                        priorities.add(objectMapper.treeToValue(node, Priority.class));
                    }
                    return priorities;
                } else {
                    throw new RuntimeException("Failed to fetch priorities: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching priorities", e);
            }
        });
    }

    public CompletableFuture<Void> createIssue(com.signongroup.pomodoro.model.jira.IssueCreateRequest issueCreateRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/issue");

                String body = objectMapper.writeValueAsString(issueCreateRequest);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("Failed to create issue: HTTP " + response.statusCode() + " " + response.body());
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Error creating issue", e);
            }
        });
    }

    public CompletableFuture<Integer> fetchTicketCount(String jql) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/search/approximate-count");

                String body = "{\"jql\": \"" + jql.replace("\"", "\\\"") + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    return root.path("count").asInt(0);
                } else {
                    throw new RuntimeException("Failed to fetch ticket count: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("Error fetching ticket count for JQL: " + jql + " - " + e.getMessage());
                return 0; // Fallback to 0
            }
        });
    }

    public CompletableFuture<Void> addWorklog(String issueKey, int timeSpentSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String baseUrl = getBaseUrl();
                URI uri = URI.create(baseUrl + "/rest/api/3/issue/" + issueKey + "/worklog");

                String body = "{\"timeSpentSeconds\": " + timeSpentSeconds + "}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", getAuthHeader())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new RuntimeException("Failed to add worklog: HTTP " + response.statusCode());
                }
                return null;
            } catch (Exception e) {
                // Log and return null instead of throwing to avoid crashing the timer
                System.err.println("Error adding worklog for task " + issueKey + ": " + e.getMessage());
                return null;
            }
        });
    }
}
