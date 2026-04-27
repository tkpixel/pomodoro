import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# fetchBoardConfiguration fallback
text = re.sub(
    r'''    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration\(String boardId\) \{\n        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/agile/1\.0/board/" \+ boardId \+ "/configuration"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    return objectMapper\.readValue\(response\.body\(\), BoardConfiguration\.class\);\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch board configuration: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching configuration for board " \+ boardId, e\);\n            \}\n        \}\);\n    \}''',
    '''    public CompletableFuture<BoardConfiguration> fetchBoardConfiguration(String boardId) {
        return CompletableFuture.supplyAsync(() -> {
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
        }, VIRTUAL_EXECUTOR);
    }''',
    text
)

# fetchTransitions
text = re.sub(
    r'''    public CompletableFuture<List<JiraTransition>> fetchTransitions\(String issueKey\) \{\n        return CompletableFuture\.supplyAsync\(\(\) -> \{\n            try \{\n                String baseUrl = getBaseUrl\(\);\n                URI uri = URI\.create\(baseUrl \+ "/rest/api/3/issue/" \+ issueKey \+ "/transitions"\);\n\n                HttpRequest request = HttpRequest\.newBuilder\(\)\n                        \.uri\(uri\)\n                        \.header\("Authorization", getAuthHeader\(\)\)\n                        \.header\("Accept", "application/json"\)\n                        \.GET\(\)\n                        \.build\(\);\n\n                HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);\n\n                if \(response\.statusCode\(\) == 200\) \{\n                    JsonNode root = objectMapper\.readTree\(response\.body\(\)\);\n                    JsonNode transitionsNode = root\.path\("transitions"\);\n                    List<JiraTransition> transitions = new ArrayList<>\(\);\n                    for \(JsonNode node : transitionsNode\) \{\n                        transitions\.add\(objectMapper\.treeToValue\(node, JiraTransition\.class\)\);\n                    \}\n                    return transitions;\n                \} else \{\n                    throw new RuntimeException\("Failed to fetch transitions: HTTP " \+ response\.statusCode\(\)\);\n                \}\n            \} catch \(Exception e\) \{\n                throw new RuntimeException\("Error fetching transitions for issue " \+ issueKey, e\);\n            \}\n        \}\);\n    \}''',
    '''    public CompletableFuture<List<JiraTransition>> fetchTransitions(String issueKey) {
        return CompletableFuture.supplyAsync(() -> {
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
        }, VIRTUAL_EXECUTOR);
    }''',
    text
)


with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
