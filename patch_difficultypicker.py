import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/DifficultyPicker.kt", "r") as f:
    content = f.read()

# Replace spacing
content = content.replace("Arrangement.spacedBy(12.dp)", "Arrangement.spacedBy(4.dp)")

# Add softWrap and maxLines
content = content.replace("label = { Text(difficulty.name) }", "label = { Text(difficulty.name, maxLines = 1, softWrap = false) }")

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/DifficultyPicker.kt", "w") as f:
    f.write(content)
