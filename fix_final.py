import re

# Fix VariantMetadata.kt
with open("app/src/main/java/com/zincstate/playmatics/domain/engine/VariantMetadata.kt", "a") as f:
    f.write("\n}\n")

# Fix HomeScreen.kt
with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "r") as f:
    content = f.read()
content = content.replace("Icons.Filled.Grid3x3", "Icons.Filled.Star")
with open("app/src/main/java/com/zincstate/playmatics/presentation/home/HomeScreen.kt", "w") as f:
    f.write(content)

