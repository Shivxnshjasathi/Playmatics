# Fix MatchRepositoryImpl.kt
with open("app/src/main/java/com/zincstate/playmatics/data/repository/MatchRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace("supabaseManager", "matchManager")
if "import kotlinx.coroutines.flow.mapNotNull" not in content:
    content = content.replace("import kotlinx.coroutines.flow.map", "import kotlinx.coroutines.flow.map\nimport kotlinx.coroutines.flow.mapNotNull")

with open("app/src/main/java/com/zincstate/playmatics/data/repository/MatchRepositoryImpl.kt", "w") as f:
    f.write(content)

# Fix SinglePlayerScreen.kt
with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.runtime.mutableStateOf" not in content:
    content = content.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.remember\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.setValue")

with open("app/src/main/java/com/zincstate/playmatics/presentation/singleplayer/SinglePlayerScreen.kt", "w") as f:
    f.write(content)
