import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "r") as f:
    content = f.read()

# Replace all occurrences of onDigit(char.code - 'A'.code) with onDigit(char.code - 'A'.code + 1)
content = content.replace("onLetter = { char -> onDigit(char.code - 'A'.code) }", "onLetter = { char -> onDigit(char.code - 'A'.code + 1) }")

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "w") as f:
    f.write(content)
