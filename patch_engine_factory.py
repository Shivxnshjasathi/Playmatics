import re

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/EngineFactory.kt", "r") as f:
    content = f.read()

# Replace MazeEngine and ScrabbleEngine imports with KenKenEngine
content = content.replace("import com.zincstate.playmatics.domain.engine.variants.MazeEngine\nimport com.zincstate.playmatics.domain.engine.variants.ScrabbleEngine\n", "import com.zincstate.playmatics.domain.engine.variants.KenKenEngine\n")
content = content.replace("GameType.MAZE -> MazeEngine\n            GameType.SCRABBLE -> ScrabbleEngine", "GameType.KENKEN -> KenKenEngine")

with open("app/src/main/java/com/zincstate/playmatics/domain/engine/EngineFactory.kt", "w") as f:
    f.write(content)
