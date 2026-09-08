package com.zincstate.playmatics.domain.engine

/**
 * Common contract for every puzzle-variant engine.
 *
 * Every implementation must be:
 * • **Pure Kotlin** — no Android dependencies.
 * • **Thread-safe** — no shared mutable state.
 * • **Deterministic** — same `seed` + `difficulty` ⇒ same puzzle on every device.
 */
interface PuzzleEngine {

    /** The game type this engine handles. */
    val gameType: GameType

    /**
     * Generate a puzzle deterministically from [seed] + [difficulty].
     */
    fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle

    /**
     * Returns `true` if placing [value] at ([row], [col]) doesn't violate
     * any constraint of this variant on the current [board].
     */
    fun isValidPlacement(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata? = null
    ): Boolean

    /**
     * Returns the set of cell coordinates that conflict with placing
     * [value] at ([row], [col]) on the current [board].
     */
    fun conflictingCells(
        board: Array<IntArray>,
        row: Int,
        col: Int,
        value: Int,
        metadata: VariantMetadata? = null
    ): Set<Pair<Int, Int>>

    /**
     * Counts cells in [board] that are filled AND match the [solution].
     */
    fun countCorrectCells(
        board: Array<IntArray>,
        solution: Array<IntArray>
    ): Int
}
