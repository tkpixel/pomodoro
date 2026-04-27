import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# fetchBoards
text = re.sub(
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/agile/1\.0/board"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    JsonNode values = root\.path\("values"\);\n                    List<JiraBoard> boards = new ArrayList<>\(\);\n                    for \(JsonNode node : values\) \{\n                        boards\.add\(objectMapper\.treeToValue\(node, JiraBoard\.class\)\);\n                    \}\n                    return boards;\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch boards: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching boards", e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/agile/1.0/board");
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/agile/1\.0/board/" \+ boardId \+ "/issue\?maxResults=50"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    JsonNode issuesNode = root\.path\("issues"\);\n                    List<JiraTask> tasks = new ArrayList<>\(\);\n                    for \(JsonNode node : issuesNode\) \{\n                        tasks\.add\(objectMapper\.treeToValue\(node, JiraTask\.class\)\);\n                    \}\n                    return tasks;\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch tasks: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching tasks for board " \+ boardId, e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/agile/1.0/board/" + boardId + "/issue?maxResults=50");
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/agile/1\.0/board/" \+ boardId \+ "/configuration"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    return objectMapper\.treeToValue\(root, BoardConfiguration\.class\);\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch board config: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching configuration for board " \+ boardId, e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/agile/1.0/board/" + boardId + "/configuration");
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/transitions"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    JsonNode transitionsNode = root\.path\("transitions"\);\n                    List<JiraTransition> transitions = new ArrayList<>\(\);\n                    for \(JsonNode node : transitionsNode\) \{\n                        transitions\.add\(objectMapper\.treeToValue\(node, JiraTransition\.class\)\);\n                    \}\n                    return transitions;\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch transitions: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching transitions for issue " \+ issueKey, e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions");
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/transitions"\);\n\n                String body = "\{\\"transition\\": \{\\"id\\": \\"" \+ transitionId \+ "\\""\}\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.POST\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                return response\.statusCode\(\) >= 200 && response\.statusCode\(\) < 300;\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error moving task " \+ issueKey, e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions");
                String body = "{\\"transition\\": {\\"id\\": \\"" + transitionId + "\\"}}";
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
                return response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error moving task " + issueKey, e);
            }
        }, VIRTUAL_EXECUTOR);''',
    text
)

# assignTaskToCurrentUser
text = re.sub(
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String accountId = authService\.getSavedAccountId\(\);\n                if \(accountId == null \|\| accountId\.isBlank\(\)\) \{\n                    throw new IllegalStateException\("Account ID not configured\. Please test connection first\."\);\n                \}\n\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/assignee"\);\n\n                String body = "\{\\"accountId\\": \\"" \+ accountId \+ "\\""\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.PUT\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                return response\.statusCode\(\) >= 200 && response\.statusCode\(\) < 300;\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error assigning task " \+ issueKey, e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                String accountId = authService.getSavedAccountId();
                if (accountId == null || accountId.isBlank()) {
                    throw new IllegalStateException("Account ID not configured. Please test connection first.");
                }
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/assignee");
                String body = "{\\"accountId\\": \\"" + accountId + "\\"}";
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.put(uri, body).join();
                return response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error assigning task " + issueKey, e);
            }
        }, VIRTUAL_EXECUTOR);''',
    text
)

# fetchIssueTypes
text = re.sub(
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issuetype"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    List<IssueType> issueTypes = new ArrayList<>\(\);\n                    for \(JsonNode node : root\) \{\n                        issueTypes\.add\(objectMapper\.treeToValue\(node, IssueType\.class\)\);\n                    \}\n                    return issueTypes;\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch issue types: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching issue types for project " \+ projectId, e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issuetype");
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/priority"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    List<Priority> priorities = new ArrayList<>\(\);\n                    for \(JsonNode node : root\) \{\n                        priorities\.add\(objectMapper\.treeToValue\(node, Priority\.class\)\);\n                    \}\n                    return priorities;\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch priorities: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching priorities", e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/priority");
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.get(uri).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue"\);\n\n                String body = objectMapper\.writeValueAsString\(issueCreateRequest\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.POST\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) < 200 \|\| response\.statusCode\(\) >= 300\) \{\n                    throw new RuntimeException\("Failed to create issue: HTTP " \+ response\.statusCode\(\) \+ " " \+ response\.body\(\)\);\n                \}\n                return null;\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error creating issue", e\);\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue");
                String body = objectMapper.writeValueAsString(issueCreateRequest);
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/search/approximate-count"\);\n\n                String body = "\{\\"jql\\": \\"" \+ jql\.replace\("\\"", "\\\\\\""\) \+ "\\"\}\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.POST\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    return root\.path\("count"\)\.asInt\(0\);\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch ticket count: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                System\.err\.println\("Error fetching ticket count for JQL: " \+ jql \+ " - " \+ e\.getMessage\(\)\);\n                return 0; // Fallback to 0\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/search/approximate-count");
                String body = "{\\"jql\\": \\"" + jql.replace("\\"", "\\\\\\"") + "\\"\\"}";
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
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
    r'''        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/worklog"\);\n\n                String body = "\{\\"timeSpentSeconds\\": " \+ timeSpentSeconds \+ "\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.POST\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) < 200 \|\| response\.statusCode\(\) >= 300\) \{\n                    throw new RuntimeException\("Failed to add worklog: HTTP " \+ response\.statusCode\(\)\);\n                \}\n                return null;\n            \} catch \(Exception e\) \{\n                // Log and return null instead of throwing to avoid crashing the timer\n                System\.err\.println\("Error adding worklog for task " \+ issueKey \+ ": " \+ e\.getMessage\(\)\);\n                return null;\n            \}\n        \}\);''',
    '''        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/worklog");
                String body = "{\\"timeSpentSeconds\\": " + timeSpentSeconds + "}";
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
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
