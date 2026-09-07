package com.zincstate.playmatics.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for complex types stored as strings.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    /** Map<Int, Set<Int>> ↔ JSON string for pencil notes. */
    @TypeConverter
    fun fromPencilNotes(notes: String): Map<Int, Set<Int>> {
        if (notes.isBlank()) return emptyMap()
        return try {
            json.decodeFromString<Map<Int, Set<Int>>>(notes)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun toPencilNotes(notes: Map<Int, Set<Int>>): String {
        return json.encodeToString(notes)
    }
}
