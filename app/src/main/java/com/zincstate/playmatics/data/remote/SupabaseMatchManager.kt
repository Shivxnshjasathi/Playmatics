package com.zincstate.playmatics.data.remote

import com.zincstate.playmatics.data.remote.dto.ForfeitEvent
import com.zincstate.playmatics.data.remote.dto.MatchDto
import com.zincstate.playmatics.data.remote.dto.PlayerPresence
import com.zincstate.playmatics.data.remote.dto.ProgressEvent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.broadcast.BroadcastPayload
import io.github.jan.supabase.realtime.presenceDataFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for all Supabase interactions:
 * - Anonymous auth
 * - PostgREST CRUD and RPCs
 * - Realtime channels (Broadcast + Presence)
 */
@Singleton

@Serializable
data class DrawStrokeEvent(val playerId: String, val stroke: List<Float>)

@Serializable
data class GuessEvent(val playerId: String, val guess: String)

@Serializable
data class ClearEvent(val playerId: String)

class SupabaseMatchManager @Inject constructor(
    private val supabase: SupabaseClient
) {
    private var currentChannel: RealtimeChannel? = null
    private var currentUserId: String? = null

    // ------------------------------------------------------------------ //
    //  Auth                                                               //
    // ------------------------------------------------------------------ //

    /** Sign in anonymously if no session exists. Returns stable user ID. */
    suspend fun ensureAuthenticated(): String {
        currentUserId?.let { return it }
        val session = try { supabase.auth.currentSessionOrNull() } catch (e: Exception) { null }
        if (session != null) {
            currentUserId = session.user?.id ?: ""
            return currentUserId!!
        }
        return try {
            supabase.auth.signInAnonymously()
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            currentUserId = userId
            userId
        } catch (e: Exception) {
            ""
        }
    }

    fun getCurrentUserId(): String = currentUserId ?: ""

    // ------------------------------------------------------------------ //
    //  PostgREST — Room lifecycle                                         //
    // ------------------------------------------------------------------ //

    /** Insert a new match row. RLS enforces host_id = auth.uid(). */
    suspend fun createRoom(matchDto: MatchDto): MatchDto {
        return supabase.from("matches")
            .insert(matchDto) { select() }
            .decodeSingle<MatchDto>()
    }

    /** Select a match by room_code (visible if status=waiting per RLS). */
    suspend fun findRoomByCode(roomCode: String): MatchDto? {
        return try {
            supabase.from("matches")
                .select {
                    filter {
                        eq("room_code", roomCode)
                        eq("status", "waiting")
                    }
                }
                .decodeSingleOrNull<MatchDto>()
        } catch (_: Exception) {
            null
        }
    }

    /** Delete a waiting room (host cancel). RLS enforces host_id = auth.uid() and status=waiting. */
    suspend fun deleteRoom(matchId: String) {
        supabase.from("matches").delete {
            filter { eq("id", matchId) }
        }
    }

    // ------------------------------------------------------------------ //
    //  PostgREST — RPCs                                                   //
    // ------------------------------------------------------------------ //

    @Serializable
    private data class JoinMatchParams(
        @SerialName("p_room_code") val roomCode: String,
        @SerialName("p_guest_id") val guestId: String
    )

    /** Guest joins a room. Throws on ROOM_UNAVAILABLE. */
    suspend fun joinMatch(roomCode: String, guestId: String): MatchDto {
        return supabase.postgrest.rpc(
            "join_match",
            JoinMatchParams(roomCode, guestId)
        ).decodeAs<MatchDto>()
    }

    @Serializable
    private data class CompleteMatchParams(
        @SerialName("p_match_id") val matchId: String,
        @SerialName("p_winner_id") val winnerId: String
    )

    /** First to 81 calls this. Returns null if someone already won. */
    suspend fun completeMatch(matchId: String, winnerId: String): MatchDto? {
        return try {
            supabase.postgrest.rpc(
                "complete_match",
                CompleteMatchParams(matchId, winnerId)
            ).decodeAs<MatchDto>()
        } catch (_: Exception) {
            null
        }
    }

    @Serializable
    private data class ForfeitMatchParams(
        @SerialName("p_match_id") val matchId: String,
        @SerialName("p_forfeiting_player") val forfeitingPlayer: String
    )

    /** Forfeit after grace period. */
    suspend fun forfeitMatch(matchId: String, forfeitingPlayerId: String): MatchDto? {
        return try {
            supabase.postgrest.rpc(
                "forfeit_match",
                ForfeitMatchParams(matchId, forfeitingPlayerId)
            ).decodeAs<MatchDto>()
        } catch (_: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------ //
    //  Realtime — Channel lifecycle                                       //
    // ------------------------------------------------------------------ //

    /** Join the Realtime channel for a match. */
    suspend fun joinMatchChannel(matchId: String) {
        leaveMatchChannel() // clean up any existing channel
        val channel = supabase.realtime.channel("match:$matchId")
        currentChannel = channel
        channel.subscribe(blockUntilSubscribed = true)
    }

    /** Track own presence in the channel. */
    suspend fun trackPresence(userId: String, displayName: String) {
        val presenceJson = kotlinx.serialization.json.Json.encodeToJsonElement(PlayerPresence.serializer(), PlayerPresence(userId = userId, displayName = displayName)).jsonObject
        currentChannel?.track(presenceJson)
    }

    /** Leave and clean up the Realtime channel. */
    suspend fun leaveMatchChannel() {
        currentChannel?.let { channel ->
            try {
                supabase.realtime.removeChannel(channel)
            } catch (_: Exception) { /* already removed */ }
        }
        currentChannel = null
    }

    // ------------------------------------------------------------------ //
    //  Realtime — Broadcast                                               //
    // ------------------------------------------------------------------ //

    /** Send own progress to the opponent. */
    suspend fun sendProgress(playerId: String, solvedCount: Int) {
        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(ProgressEvent.serializer(), ProgressEvent(playerId = playerId, solvedCount = solvedCount))
        currentChannel?.broadcast(
            event = "progress",
            payload = BroadcastPayload.Json(payloadJson)
        )
    }

    /** Send forfeit notification. */
    suspend fun sendForfeitBroadcast(playerId: String) {
        val payloadJson = kotlinx.serialization.json.Json.encodeToJsonElement(ForfeitEvent.serializer(), ForfeitEvent(playerId = playerId))
        currentChannel?.broadcast(
            event = "forfeit",
            payload = BroadcastPayload.Json(payloadJson)
        )
    }

    /** Observe opponent progress events. */
    fun observeProgress(): Flow<ProgressEvent>? {
        return currentChannel?.broadcastFlow("progress")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(ProgressEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }

    /** Observe forfeit events. */
    fun observeForfeit(): Flow<ForfeitEvent>? {
        return currentChannel?.broadcastFlow("forfeit")?.mapNotNull {
            val jsonPayload = it.payload as? BroadcastPayload.Json ?: return@mapNotNull null
            try { kotlinx.serialization.json.Json.decodeFromJsonElement(ForfeitEvent.serializer(), jsonPayload.value) } catch (e: Exception) { null }
        }
    }


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

    // ------------------------------------------------------------------ //
    //  Realtime — Presence                                                //
    // ------------------------------------------------------------------ //

    /** Observe presence changes. Emits whether the opponent is currently present. */
    fun observePresenceChanges(): Flow<Boolean>? {
        val userId = currentUserId ?: return null
        val channel = currentChannel ?: return null
        return kotlinx.coroutines.flow.flow {
            channel.presenceDataFlow<PlayerPresence>().collect { presences ->
                emit(presences.any { it.userId != userId })
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Realtime — Postgres Changes (for waiting lobby)                    //
    // ------------------------------------------------------------------ //

    /** Observe changes to a specific match row (via polling for robust sync). */
    fun observeMatchChanges(matchId: String): Flow<MatchDto> {
        return kotlinx.coroutines.flow.flow {
            while (true) {
                try {
                    val match = supabase.from("matches")
                        .select { filter { eq("id", matchId) } }
                        .decodeSingleOrNull<MatchDto>()
                    
                    if (match != null) {
                        emit(match)
                    }
                } catch (e: Exception) {
                    // best effort
                }
                kotlinx.coroutines.delay(2000)
            }
        }
    }
}
