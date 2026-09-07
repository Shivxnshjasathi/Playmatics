package com.zincstate.playmatics.data.remote

import com.zincstate.playmatics.data.remote.dto.ForfeitEvent
import com.zincstate.playmatics.data.remote.dto.MatchDto
import com.zincstate.playmatics.data.remote.dto.PlayerPresence
import com.zincstate.playmatics.data.remote.dto.ProgressEvent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Anonymous
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.presence.presenceChangeFlow
import io.github.jan.supabase.realtime.presence.track
import io.github.jan.supabase.realtime.realtime
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
        val session = supabase.auth.currentSessionOrNull()
        if (session != null) {
            currentUserId = session.user?.id ?: ""
            return currentUserId!!
        }
        supabase.auth.signInWith(Anonymous)
        val userId = supabase.auth.currentUserOrNull()?.id ?: ""
        currentUserId = userId
        return userId
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
        ).decodeSingle<MatchDto>()
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
            ).decodeSingleOrNull<MatchDto>()
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
            ).decodeSingleOrNull<MatchDto>()
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
        channel.subscribe()
    }

    /** Track own presence in the channel. */
    suspend fun trackPresence(userId: String, displayName: String) {
        currentChannel?.presence?.track(
            PlayerPresence(userId = userId, displayName = displayName)
        )
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
        currentChannel?.broadcast(
            event = "progress",
            data = ProgressEvent(playerId = playerId, solvedCount = solvedCount)
        )
    }

    /** Send forfeit notification. */
    suspend fun sendForfeitBroadcast(playerId: String) {
        currentChannel?.broadcast(
            event = "forfeit",
            data = ForfeitEvent(playerId = playerId)
        )
    }

    /** Observe opponent progress events. */
    fun observeProgress(): Flow<ProgressEvent>? {
        return currentChannel?.broadcastFlow<ProgressEvent>(event = "progress")
    }

    /** Observe forfeit events. */
    fun observeForfeit(): Flow<ForfeitEvent>? {
        return currentChannel?.broadcastFlow<ForfeitEvent>(event = "forfeit")
    }

    // ------------------------------------------------------------------ //
    //  Realtime — Presence                                                //
    // ------------------------------------------------------------------ //

    /** Observe presence changes. Emits whether the opponent is currently present. */
    fun observePresenceChanges(): Flow<Boolean>? {
        val userId = currentUserId ?: return null
        return currentChannel?.presence?.presenceChangeFlow()?.map { changes ->
            // Check if any non-self user is present in the current state
            val currentPresences = try {
                currentChannel?.presence?.currentPresences
                    ?.flatMap { (_, presences) ->
                        presences.mapNotNull { presence ->
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
    }

    // ------------------------------------------------------------------ //
    //  Realtime — Postgres Changes (for waiting lobby)                    //
    // ------------------------------------------------------------------ //

    /** Observe changes to a specific match row. */
    fun observeMatchChanges(matchId: String): Flow<MatchDto> {
        val channel = supabase.realtime.channel("match-changes:$matchId")
        // Subscribe to postgres changes on the matches table filtered by id
        return channel.broadcastFlow<MatchDto>(event = "match-update").mapNotNull { it }
    }
}
