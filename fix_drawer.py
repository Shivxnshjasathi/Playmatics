import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace Modifier.width(300.dp) with Modifier.width(300.dp).fillMaxHeight()
content = content.replace("modifier = Modifier.width(300.dp)", "modifier = Modifier.width(300.dp).fillMaxHeight()")

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)
