import re

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "r") as f:
    content = f.read()

content = content.replace("import com.zincstate.playmatics.presentation.components.MazeBoard\n", "")
content = content.replace("import com.zincstate.playmatics.presentation.components.ScrabbleBoard\n", "")

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/GameRenderer.kt", "w") as f:
    f.write(content)
