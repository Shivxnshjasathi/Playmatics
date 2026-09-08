# Fix SupabaseMatchManager.kt to make currentUserId public
with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "r") as f:
    content = f.read()
content = content.replace("private var currentUserId", "var currentUserId")
with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "w") as f:
    f.write(content)

# Fix MultiplayerMatchScreen.kt GameType import
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchScreen.kt", "r") as f:
    content = f.read()
if "import com.zincstate.playmatics.domain.engine.GameType" not in content:
    content = content.replace("import com.zincstate.playmatics.domain.engine.Difficulty", "import com.zincstate.playmatics.domain.engine.Difficulty\nimport com.zincstate.playmatics.domain.engine.GameType")
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchScreen.kt", "w") as f:
    f.write(content)

# Fix SinglePlayerScreen.kt GameType import
with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "r") as f:
    content = f.read()
if "import com.zincstate.playmatics.domain.engine.GameType" not in content:
    content = content.replace("import com.zincstate.playmatics.domain.engine.Difficulty", "import com.zincstate.playmatics.domain.engine.Difficulty\nimport com.zincstate.playmatics.domain.engine.GameType")
with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "w") as f:
    f.write(content)

# Fix MultiplayerMatchViewModel.kt missing kotlinx import and hostId reference
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "r") as f:
    content = f.read()
if "import kotlinx.coroutines.flow.firstOrNull" not in content:
    content = content.replace("import kotlinx.coroutines.flow.update", "import kotlinx.coroutines.flow.update\nimport kotlinx.coroutines.flow.firstOrNull")
# Find the exact line in MultiplayerMatchViewModel.kt and fix it
# val matchStream = matchRepository.observeMatch(matchId).kotlinx.coroutines.flow.firstOrNull() -> val matchStream = matchRepository.observeMatch(matchId).firstOrNull()
content = content.replace(".kotlinx.coroutines.flow.firstOrNull()", ".firstOrNull()")
content = content.replace("isHost = matchStream?.hostId == userId", "isHost = matchStream?.hostId == userId")
with open("app/src/main/java/com/zincstate/playmatics/presentation/multiplayer/match/MultiplayerMatchViewModel.kt", "w") as f:
    f.write(content)

