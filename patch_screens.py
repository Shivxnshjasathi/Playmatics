import re

def update_file(filename):
    with open(filename, "r") as f:
        content = f.read()

    # Add GameType import if missing
    if "import com.zincstate.playmatics.domain.engine.GameType" not in content:
        content = content.replace("import com.zincstate.playmatics.domain.engine.Difficulty", "import com.zincstate.playmatics.domain.engine.Difficulty\nimport com.zincstate.playmatics.domain.engine.GameType")

    # Add givenCells if missing
    if "givenCells = state.givenCells," not in content:
        content = content.replace("board = state.board,", "board = state.board,\n                    givenCells = state.givenCells,")
    
    with open(filename, "w") as f:
        f.write(content)

update_file("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchScreen.kt")
update_file("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt")
