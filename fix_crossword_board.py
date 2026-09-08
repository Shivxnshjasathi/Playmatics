import re
with open("app/src/main/java/com/zincstate/playmatics/presentation/components/CrosswordBoard.kt", "r") as f:
    content = f.read()

content = content.replace("{ (num, clue) ->", "{ entry ->\n                    val num = entry.key\n                    val clue = entry.value")

with open("app/src/main/java/com/zincstate/playmatics/presentation/components/CrosswordBoard.kt", "w") as f:
    f.write(content)
