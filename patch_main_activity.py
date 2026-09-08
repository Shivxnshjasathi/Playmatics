import re

with open("app/src/main/java/com/zincstate/playmatics/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("val musicEnabled = false", "val musicEnabled by settingsRepository.observeMusicEnabled()\n                .collectAsState(initial = false)")

with open("app/src/main/java/com/zincstate/playmatics/MainActivity.kt", "w") as f:
    f.write(content)
