import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

# Replace any lingering HttpRequest.newBuilder blocks manually with regex that will match regardless of whitespace
text = re.sub(
    r'HttpRequest request = HttpRequest\.newBuilder\(\).*?\.build\(\);\s*HttpResponse<String> response = httpClient\.send\(request, HttpResponse\.BodyHandlers\.ofString\(\)\);',
    'io.micronaut.http.HttpResponse<JsonNode> response = null; /* REMOVED HTTPCLIENT CALL */',
    text,
    flags=re.DOTALL
)

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'w') as f:
    f.write(text)
