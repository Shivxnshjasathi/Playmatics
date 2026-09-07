import re

with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "r") as f:
    content = f.read()

# Fix imports
content = content.replace("import io.github.jan.supabase.realtime.broadcastFlow", "import io.github.jan.supabase.realtime.broadcastFlow\nimport io.github.jan.supabase.realtime.broadcast.BroadcastPayload\nimport io.github.jan.supabase.realtime.presenceDataFlow")

# Fix track presence
old_track = """        currentChannel?.presence?.track(
            PlayerPresence(userId = userId, displayName = displayName)
        )"""
new_track = """        val presenceJson = kotlinx.serialization.json.Json.encodeToJsonElement(PlayerPresence.serializer(), PlayerPresence(userId = userId, displayName = displayName)).jsonObject
        currentChannel?.track(presenceJson)"""
content = content.replace(old_track, new_track)

# Fix sendProgress
old_sendProgress = """        currentChannel?.broadcast(
            event = "progress",
            payload = ProgressEvent(playerId = playerId, solvedCount = solvedCount)
        )"""
new_sendProgress = """        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(ProgressEvent.serializer(), ProgressEvent(playerId = playerId, solvedCount = solvedCount))
        currentChannel?.broadcast(
            event = "progress",
            payload = BroadcastPayload.Json(payloadJson)
        )"""
content = content.replace(old_sendProgress, new_sendProgress)

# Fix sendForfeit
old_sendForfeit = """        currentChannel?.broadcast(
            event = "forfeit",
            payload = ForfeitEvent(playerId = playerId)
        )"""
new_sendForfeit = """        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(ForfeitEvent.serializer(), ForfeitEvent(playerId = playerId))
        currentChannel?.broadcast(
            event = "forfeit",
            payload = BroadcastPayload.Json(payloadJson)
        )"""
content = content.replace(old_sendForfeit, new_sendForfeit)

# Fix observeProgress
old_observeProgress = """    fun observeProgress(): Flow<ProgressEvent>? {
        return currentChannel?.broadcastFlow<ProgressEvent>(event = "progress")
    }"""
new_observeProgress = """    fun observeProgress(): Flow<ProgressEvent>? {
        return currentChannel?.broadcastFlow("progress")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(ProgressEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }"""
content = content.replace(old_observeProgress, new_observeProgress)

# Fix observeForfeit
old_observeForfeit = """    fun observeForfeit(): Flow<ForfeitEvent>? {
        return currentChannel?.broadcastFlow<ForfeitEvent>(event = "forfeit")
    }"""
new_observeForfeit = """    fun observeForfeit(): Flow<ForfeitEvent>? {
        return currentChannel?.broadcastFlow("forfeit")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(ForfeitEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }"""
content = content.replace(old_observeForfeit, new_observeForfeit)

# Fix observePresenceChanges
old_observePresence = """    fun observePresenceChanges(): Flow<Boolean>? {
        val userId = currentUserId ?: return null
        return currentChannel?.presence?.currentPresences
                    ?.flatMap { entry ->
                        entry.value.mapNotNull { presence ->
                            try {
                                kotlinx.serialization.json.Json.decodeFromJsonElement(
                                    PlayerPresence.serializer(),
                                    presence.state
                                )
                            } catch (_: Exception) { null }
                        }
                    } ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            currentPresences.any { it.userId != userId }
        }
    }"""
new_observePresence = """    fun observePresenceChanges(): Flow<Boolean>? {
        val userId = currentUserId ?: return null
        return currentChannel?.presenceDataFlow<PlayerPresence>()?.map { presences ->
            presences.any { it.userId != userId }
        }
    }"""

# Since observePresenceChanges is multi-line and hard to match exactly, use regex
import re
content = re.sub(r'    fun observePresenceChanges\(\): Flow<Boolean>\? \{.*?\n    \}', new_observePresence, content, flags=re.DOTALL)


with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "w") as f:
    f.write(content)
