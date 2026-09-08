with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "r") as f:
    content = f.read()

# Change it back to `private var currentUserId` but add a custom getter or just use the existing `getCurrentUserId()` in `MatchRepositoryImpl.kt`!
# Wait, let's just rename the property to `_currentUserId` and keep `getCurrentUserId()` or vice versa.
content = content.replace("var currentUserId", "private var currentUserId")

with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/zincstate/playmatics/data/repository/MatchRepositoryImpl.kt", "r") as f:
    content = f.read()

content = content.replace("val userId = matchManager.currentUserId", "val userId = matchManager.getCurrentUserId()")

with open("app/src/main/java/com/zincstate/playmatics/data/repository/MatchRepositoryImpl.kt", "w") as f:
    f.write(content)
