# playmatics. 🎮

A real-time multiplayer logic and math puzzle hub for Android. This repository contains the Android client, but the architecture is designed to be platform-agnostic for future Web and iOS ports.

## 🏗 System Architecture

Playmatics uses a classic **Unidirectional Data Flow (UDF)** architecture on the client-side (MVI pattern), paired with a **Backend-as-a-Service (Supabase)** for real-time synchronization.

### 1. Client-Side (Android)
- **UI Layer**: Jetpack Compose (Material 3). Uses custom `spring` physics for real-time progress bar snapping and highly optimized recomposition for the Sudoku grid.
- **Presentation Layer**: `ViewModel` handling state mutations via Kotlin `StateFlow`.
- **Domain Layer**: `MatchRepository` and `PuzzleRepository` interfaces abstracting data sources. Deterministic `SudokuGenerator` for puzzle logic.
- **Data Layer**: 
  - `Room` (SQLite) for offline persistence (player stats, singleplayer history).
  - `supabase-kt` client for remote database and WebSocket connections.
- **DI**: Hilt for constructor injection.

---

## ⚡ Multiplayer Synchronization Logic

The multiplayer system is designed to be highly scalable and latency-tolerant, relying on **Deterministic Generation** and **Realtime Broadcasts**.

### Deterministic Puzzle Generation
Instead of serializing and transferring a full 81-character Sudoku grid over the network, Playmatics relies on shared seeds.
1. The Host generates a random `seed` (Int64) and selects a `Difficulty`.
2. This `seed` and `difficulty` are saved to the Postgres database.
3. The Guest fetches the `seed` and `difficulty`.
4. **Both clients independently run the `SudokuGenerator`** using `kotlin.random.Random(seed)`. Because the random number generator is seeded, both clients mathematically guarantee the exact same puzzle layout and solution.

### Matchmaking Flow (Postgres)
1. **Creation**: Host creates a row in the `matches` table with a 4-letter `room_code` and `status = 'waiting'`.
2. **Joining**: Guest queries the `matches` table by `room_code`. If found, the Guest updates the row: `guest_id = UUID`, `status = 'in_progress'`.
3. **Polling/CDC**: The Host client polls the Postgres table for changes to `status`. When it transitions to `in_progress`, both clients transition to the Match Screen.

### In-Game Realtime (WebSockets)
Once the match starts, database writes are too slow for real-time progress bars. Playmatics switches to **Supabase Realtime Channels** (`match:<match_id>`).
- **Progress Broadcasts**: Every time a player solves a cell, their client sends an ephemeral `BroadcastEvent` (JSON payload) over the WebSocket containing their new `solved_count`.
- **Forfeit Broadcasts**: If a player manually leaves the room, a `forfeit` broadcast is sent, immediately ending the game for the opponent.
- **Network Resilience**: Auto-forfeit on presence drop is disabled. Clients maintain local state and sync gracefully if the WebSocket momentarily drops.

---

## 🗄 Database Schema (Supabase Postgres)

The backend relies on a single heavily-optimized table with Row Level Security (RLS).

```sql
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_code VARCHAR(10) NOT NULL UNIQUE,
    seed BIGINT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'waiting', -- 'waiting', 'in_progress', 'completed'
    host_id UUID NOT NULL REFERENCES auth.users(id),
    guest_id UUID REFERENCES auth.users(id),
    winner_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index for fast room code lookups
CREATE INDEX idx_matches_room_code ON matches(room_code);
```

### Row Level Security (RLS) Policies
- **Insert**: Only authenticated users can insert (they become `host_id`).
- **Select**: Anyone can read matches (needed for `room_code` querying).
- **Update**: Only `host_id` or `guest_id` can mutate the match state (to prevent unauthorized interference).

---

## 🚀 Porting to Web (Next.js / React)
If you are porting this application to a web framework, follow this mapping:

1. **State Management**: Map Kotlin `StateFlow` to React `useState` / `useReducer` or Zustand.
2. **Database SDK**: Swap `supabase-kt` for `@supabase/supabase-js`.
3. **Realtime Channels**: Use `supabase.channel('match:ID').on('broadcast', ...)` for progress updates.
4. **Sudoku Engine**: Port the Kotlin backtracking algorithm to TypeScript, ensuring you use a seeded RNG library (like `seedrandom`) to maintain the deterministic generation parity with the Android app!
