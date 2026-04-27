import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# moveTask
text = re.sub(
    r'''    public CompletableFuture<Boolean> moveTask\(String issueKey, String transitionId\) \{\n        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/transitions"\);\n\n                String body = "\{\\"transition\\": \{\\"id\\": \\"" \+ transitionId \+ "\\""\}\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.POST\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                return response\.statusCode\(\) >= 200 && response\.statusCode\(\) < 300;\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error moving task " \+ issueKey, e\);\n            \}\n        \}\);\n    \}''',
    '''    public CompletableFuture<Boolean> moveTask(String issueKey, String transitionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = URI.create(getBaseUrl() + "/rest/api/3/issue/" + issueKey + "/transitions");
                String body = "{\\"transition\\": {\\"id\\": \\"" + transitionId + "\\"}}";
                io.micronaut.http.HttpResponse<JsonNode> response = jiraApiClient.post(uri, body).join();
                return response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300;
            } catch (Exception e) {
                throw new RuntimeException("Error moving task " + issueKey, e);
            }
        }, VIRTUAL_EXECUTOR);
    }''',
    text
)

# assignTaskToCurrentUser
text = re.sub(
    r'''    public CompletableFuture<Boolean> assignTaskToCurrentUser\(String issueKey\) \{\n        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String accountId = authService\.getSavedAccountId\(\);\n                if \(accountId == null \|\| accountId\.isBlank\(\)\) \{\n                    throw new IllegalStateException\("Account ID not configured\. Please test connection first\."\);\n                \}\n\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/assignee"\);\n\n                String body = "\{\\"accountId\\": \\"" \+ accountId \+ "\\""\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.PUT\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                return response\.statusCode\(\) >= 200 && response\.statusCode\(\) < 300;\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error assigning task " \+ issueKey, e\);\n            \}\n        \}\);\n    \}''',
    '''    public CompletableFuture<Boolean> assignTaskToCurrentUser(String issueKey) {
        return CompletableFuture.supplyAsync(() -> {
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
        }, VIRTUAL_EXECUTOR);
    }''',
    text
)

# fetchTicketCount
text = re.sub(
    r'''    public CompletableFuture<Integer> fetchTicketCount\(String jql\) \{\n        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/search/approximate-count"\);\n\n                String body = "\{\\"jql\\": \\"" \+ jql\.replace\("\\"", "\\\\\\""\) \+ "\\"\}\}";\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.header\("Content-Type", "application/json"\)\n                        \.POST\(HttpRequest\.BodyPublishers\.ofString\(body\)\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    return root\.path\("count"\)\.asInt\(0\);\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch ticket count: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                System\.err\.println\("Error fetching ticket count for JQL: " \+ jql \+ " - " \+ e\.getMessage\(\)\);\n                return 0; // Fallback to 0\n            \}\n        \}\);\n    \}''',
    '''    public CompletableFuture<Integer> fetchTicketCount(String jql) {
        return CompletableFuture.supplyAsync(() -> {
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
        }, VIRTUAL_EXECUTOR);
    }''',
    text
)


with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
