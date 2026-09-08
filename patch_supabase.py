import re

with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "r") as f:
    content = f.read()

# Add Serializable classes at the top of the file before SupabaseMatchManager class
serializable_classes = """
@Serializable
data class DrawStrokeEvent(val playerId: String, val stroke: List<Float>)

@Serializable
data class GuessEvent(val playerId: String, val guess: String)

@Serializable
data class ClearEvent(val playerId: String)
"""

if "data class DrawStrokeEvent" not in content:
    content = content.replace("class SupabaseMatchManager", serializable_classes + "\nclass SupabaseMatchManager")

# Add send and observe methods
new_methods = """
    // ------------------------------------------------------------------ //
    //  Realtime — Drawing                                                 //
    // ------------------------------------------------------------------ //

    suspend fun sendDrawStroke(playerId: String, stroke: List<Float>) {
        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(DrawStrokeEvent.serializer(), DrawStrokeEvent(playerId, stroke))
        currentChannel?.broadcast(event = "draw_stroke", payload = BroadcastPayload.Json(payloadJson))
    }

    suspend fun sendGuess(playerId: String, guess: String) {
        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(GuessEvent.serializer(), GuessEvent(playerId, guess))
        currentChannel?.broadcast(event = "draw_guess", payload = BroadcastPayload.Json(payloadJson))
    }

    suspend fun sendClearBoard(playerId: String) {
        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(ClearEvent.serializer(), ClearEvent(playerId))
        currentChannel?.broadcast(event = "draw_clear", payload = BroadcastPayload.Json(payloadJson))
    }

    fun observeDrawStrokes(): Flow<DrawStrokeEvent>? {
        return currentChannel?.broadcastFlow("draw_stroke")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(DrawStrokeEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }

    fun observeGuesses(): Flow<GuessEvent>? {
        return currentChannel?.broadcastFlow("draw_guess")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(GuessEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }

    fun observeClearBoard(): Flow<ClearEvent>? {
        return currentChannel?.broadcastFlow("draw_clear")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(ClearEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }
"""

if "fun observeDrawStrokes" not in content:
    # Insert right before Presence section
    content = content.replace("    // ------------------------------------------------------------------ //\n    //  Realtime — Presence", new_methods + "\n    // ------------------------------------------------------------------ //\n    //  Realtime — Presence")

with open("app/src/main/java/com/zincstate/playmatics/data/remote/SupabaseMatchManager.kt", "w") as f:
    f.write(content)
