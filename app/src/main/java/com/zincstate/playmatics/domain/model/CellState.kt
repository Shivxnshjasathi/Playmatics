package com.zincstate.playmatics.domain.model

/** Visual state of a single cell on the Sudoku board. */
enum class CellState {
    /** Pre-filled clue — immutable, displayed in bold. */
    GIVEN,
    /** No value entered yet. */
    EMPTY,
    /** User-entered value that matches the solution. */
    USER_CORRECT,
    /** User-entered value that conflicts or doesn't match the solution. */
    USER_INCORRECT
}
