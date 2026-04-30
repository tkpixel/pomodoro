package com.signongroup.pomodoro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signongroup.pomodoro.model.jira.AdfDoc;
import com.signongroup.pomodoro.model.jira.BoardConfiguration;
import com.signongroup.pomodoro.model.jira.IssueType;
import com.signongroup.pomodoro.model.jira.JiraBoard;
import com.signongroup.pomodoro.model.jira.JiraChangelogItem;
import com.signongroup.pomodoro.model.jira.JiraChangelogResponse;
import com.signongroup.pomodoro.model.jira.JiraComment;
import com.signongroup.pomodoro.model.jira.JiraCommentResponse;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Singleton
public class JiraBoardService {

  private final JiraAuthService authService;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private static final Executor VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

  @Inject
  public JiraBoardService(JiraAuthService authService, ObjectMapper objectMapper) {
    this.authService = authService;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  private String buildAuthHeader() {
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

  private JsonNode get(URI uri) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", buildAuthHeader())
            .header("Accept", "application/json")
            .GET()
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
    }
    return objectMapper.readTree(response.body());
  }

  private JsonNode post(URI uri, String body) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", buildAuthHeader())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.body() == null || response.body().isBlank()) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(response.body());
  }

  private int postStatus(URI uri, String body) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", buildAuthHeader())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
  }

  private int putStatus(URI uri, String body) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", buildAuthHeader())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
  }

  public CompletableFuture<List<JiraBoard>> fetchBoards() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root = get(URI.create(getBaseUrl() + "/rest/agile/1.0/board"));
            List<JiraBoard> boards = new ArrayList<>();
            for (JsonNode node : root.path("values")) {
              boards.add(objectMapper.treeToValue(node, JiraBoard.class));
            }
            return boards;
          } catch (Exception e) {
            throw new RuntimeException("Error fetching boards", e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<List<JiraTask>> fetchTasks(Long boardId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root =
                get(
                    URI.create(
                        getBaseUrl()
                            + "/rest/agile/1.0/board/"
                            + boardId
                            + "/issue?maxResults=50"));
            List<JiraTask> tasks = new ArrayList<>();
            for (JsonNode node : root.path("issues")) {
              tasks.add(objectMapper.treeToValue(node, JiraTask.class));
            }
            return tasks;
          } catch (Exception e) {
            throw new RuntimeException("Error fetching tasks for board " + boardId, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<BoardConfiguration> fetchBoardConfiguration(Long boardId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root =
                get(
                    URI.create(
                        getBaseUrl() + "/rest/agile/1.0/board/" + boardId + "/configuration"));
            return objectMapper.treeToValue(root, BoardConfiguration.class);
          } catch (Exception e) {
            throw new RuntimeException("Error fetching configuration for board " + boardId, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<List<JiraTransition>> fetchTransitions(String issueKey) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root =
                get(URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions"));
            List<JiraTransition> transitions = new ArrayList<>();
            for (JsonNode node : root.path("transitions")) {
              transitions.add(objectMapper.treeToValue(node, JiraTransition.class));
            }
            return transitions;
          } catch (Exception e) {
            throw new RuntimeException("Error fetching transitions for issue " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<Boolean> moveTask(String issueKey, String transitionId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions");
            String body = "{\"transition\": {\"id\": \"" + transitionId + "\"}}";
            int status = postStatus(uri, body);
            return status >= 200 && status < 300;
          } catch (Exception e) {
            throw new RuntimeException("Error moving task " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<Boolean> assignTaskToCurrentUser(String issueKey) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            String accountId = authService.getSavedAccountId();
            if (accountId == null || accountId.isBlank()) {
              throw new IllegalStateException(
                  "Account ID not configured. Please test connection first.");
            }
            URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/assignee");
            String body = "{\"accountId\": \"" + accountId + "\"}";
            int status = putStatus(uri, body);
            return status >= 200 && status < 300;
          } catch (Exception e) {
            throw new RuntimeException("Error assigning task " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<List<IssueType>> fetchIssueTypes(String projectId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root = get(URI.create(getBaseUrl() + "/rest/api/3/issuetype"));
            List<IssueType> issueTypes = new ArrayList<>();
            for (JsonNode node : root) {
              issueTypes.add(objectMapper.treeToValue(node, IssueType.class));
            }
            return issueTypes;
          } catch (Exception e) {
            throw new RuntimeException("Error fetching issue types for project " + projectId, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<List<Priority>> fetchPriorities() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root = get(URI.create(getBaseUrl() + "/rest/api/3/priority"));
            List<Priority> priorities = new ArrayList<>();
            for (JsonNode node : root) {
              priorities.add(objectMapper.treeToValue(node, Priority.class));
            }
            return priorities;
          } catch (Exception e) {
            throw new RuntimeException("Error fetching priorities", e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<Void> createIssue(
      com.signongroup.pomodoro.model.jira.IssueCreateRequest issueCreateRequest) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue");
            String body = objectMapper.writeValueAsString(issueCreateRequest);
            int status = postStatus(uri, body);
            if (status < 200 || status >= 300) {
              throw new RuntimeException("Failed to create issue: HTTP " + status);
            }
            return null;
          } catch (Exception e) {
            throw new RuntimeException("Error creating issue", e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<Integer> fetchTicketCount(String jql) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            URI uri = URI.create(getBaseUrl() + "/rest/api/3/search/approximate-count");
            String body = "{\"jql\": \"" + jql.replace("\"", "\\\"") + "\"}";
            JsonNode root = post(uri, body);
            return root.path("count").asInt(0);
          } catch (Exception e) {
            System.err.println(
                "Error fetching ticket count for JQL: " + jql + " - " + e.getMessage());
            return 0;
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<Void> addWorklog(String issueKey, int timeSpentSeconds) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/worklog");
            String body = "{\"timeSpentSeconds\": " + timeSpentSeconds + "}";
            int status = postStatus(uri, body);
            if (status < 200 || status >= 300) {
              throw new RuntimeException("Failed to add worklog: HTTP " + status);
            }
            return null;
          } catch (Exception e) {
            System.err.println("Error adding worklog for task " + issueKey + ": " + e.getMessage());
            return null;
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<JiraTask> fetchIssueDetails(String issueKey) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root =
                get(
                    URI.create(
                        getBaseUrl() + "/rest/api/3/issue/" + issueKey + "?expand=renderedFields"));
            return objectMapper.treeToValue(root, JiraTask.class);
          } catch (Exception e) {
            throw new RuntimeException("Error fetching details for issue " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<List<JiraComment>> fetchIssueComments(String issueKey) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root =
                get(
                    URI.create(
                        getBaseUrl()
                            + "/rest/api/3/issue/"
                            + issueKey
                            + "/comment?expand=renderedBody"));
            JiraCommentResponse response =
                objectMapper.treeToValue(root, JiraCommentResponse.class);
            return response.comments() != null ? response.comments() : new ArrayList<>();
          } catch (Exception e) {
            throw new RuntimeException("Error fetching comments for issue " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<List<JiraChangelogItem>> fetchIssueChangelog(String issueKey) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            JsonNode root =
                get(URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/changelog"));
            JiraChangelogResponse response =
                objectMapper.treeToValue(root, JiraChangelogResponse.class);
            return response.values() != null ? response.values() : new ArrayList<>();
          } catch (Exception e) {
            throw new RuntimeException("Error fetching changelog for issue " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }

  public CompletableFuture<Void> addComment(String issueKey, String commentText) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/comment");
            AdfDoc bodyDoc = AdfDoc.ofText(commentText);
            String body = objectMapper.writeValueAsString(Map.of("body", bodyDoc));
            int status = postStatus(uri, body);
            if (status < 200 || status >= 300) {
              throw new RuntimeException("Failed to add comment: HTTP " + status);
            }
            return null;
          } catch (Exception e) {
            throw new RuntimeException("Error adding comment to issue " + issueKey, e);
          }
        },
        VIRTUAL_EXECUTOR);
  }
}
