import re

with open('src/main/java/com/signongroup/pomodoro/service/JiraBoardService.java', 'r') as f:
    text = f.read()

pattern = r'    public CompletableFuture<[^>]+> fetchTasks\([^)]*\)\s*\{\s*return CompletableFuture\.supplyAsync\(\(\)\s*->\s*\{.*?\}(?:\s*,\s*VIRTUAL_EXECUTOR)?\s*\);\s*\}'
matches = re.findall(pattern, text, flags=re.DOTALL)
print(f"fetchTasks matches: {len(matches)}")
