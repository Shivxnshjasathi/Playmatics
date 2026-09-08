import re

with open("app/src/main/java/com/zincstate/playmatics/data/repository/SettingsRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace("return settingsDao.getSettings().map { it?.musicEnabled ?: true }", "return settingsDao.getSettings().map { it?.musicEnabled ?: false }")

with open("app/src/main/java/com/zincstate/playmatics/data/repository/SettingsRepositoryImpl.kt", "w") as f:
    f.write(content)
