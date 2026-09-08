import re
with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.Functions" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Settings", "import androidx.compose.material.icons.filled.Settings\nimport androidx.compose.material.icons.filled.Functions")

with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)
