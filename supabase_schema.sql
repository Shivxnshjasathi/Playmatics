-- ============================================================
-- Playmatics — Competitive Sudoku: Supabase SQL Schema
-- Run this entire file in Supabase SQL Editor (Dashboard → SQL)
-- ============================================================

-- 1. Extensions
create extension if not exists "pgcrypto";

-- 2. Matches table
create table if not exists public.matches (
  id            uuid        primary key default gen_random_uuid(),
  room_code     text        not null unique,
  seed          bigint      not null,
  difficulty    text        not null check (difficulty in ('easy', 'normal', 'hard')),
  game_type     text        not null default 'sudoku',
  status        text        not null default 'waiting' check (status in ('waiting', 'in_progress', 'completed')),
  host_id       uuid        not null,
  guest_id      uuid,
  winner_id     uuid,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  started_at    timestamptz,
  completed_at  timestamptz
);

-- 3. Indexes
create index if not exists idx_matches_room_code on public.matches (room_code);
create index if not exists idx_matches_status_created on public.matches (status, created_at);

-- 4. Row Level Security
alter table public.matches enable row level security;

-- Anyone authenticated can see waiting rooms or their own matches
drop policy if exists "select_matches" on public.matches;
create policy "select_matches"
on public.matches for select
to authenticated
using (status = 'waiting' or auth.uid() = host_id or auth.uid() = guest_id);

-- Only the host can insert (RLS enforces host_id = auth.uid())
drop policy if exists "insert_matches" on public.matches;
create policy "insert_matches"
on public.matches for insert
to authenticated
with check (auth.uid() = host_id);

-- Host can delete their own waiting room (cancel before guest joins)
drop policy if exists "delete_own_waiting_match" on public.matches;
create policy "delete_own_waiting_match"
on public.matches for delete
to authenticated
using (auth.uid() = host_id and status = 'waiting');

-- NOTE: There is intentionally NO general UPDATE policy.
-- All state transitions go through security definer RPCs below.

-- ============================================================
-- 5. RPC Functions (security definer — bypass RLS)
-- ============================================================

-- 5a. Guest claims an open room
create or replace function public.join_match(p_room_code text, p_guest_id uuid)
returns public.matches
language plpgsql security definer as $$
declare v_match public.matches;
begin
  update public.matches
  set guest_id   = p_guest_id,
      status     = 'in_progress',
      started_at = now(),
      updated_at = now()
  where room_code = p_room_code
    and status    = 'waiting'
    and guest_id  is null
    and host_id  <> p_guest_id
  returning * into v_match;

  if v_match.id is null then
    raise exception 'ROOM_UNAVAILABLE';
  end if;
  return v_match;
end; $$;

-- 5b. First to 81 correct cells wins. Second caller gets null (someone already won).
create or replace function public.complete_match(p_match_id uuid, p_winner_id uuid)
returns public.matches
language plpgsql security definer as $$
declare v_match public.matches;
begin
  update public.matches
  set status       = 'completed',
      winner_id    = p_winner_id,
      completed_at = now(),
      updated_at   = now()
  where id     = p_match_id
    and status = 'in_progress'
  returning * into v_match;
  return v_match; -- null means someone already won
end; $$;

-- 5c. Forfeit after disconnect grace period expires
create or replace function public.forfeit_match(p_match_id uuid, p_forfeiting_player uuid)
returns public.matches
language plpgsql security definer as $$
declare v_match public.matches;
begin
  update public.matches
  set status       = 'completed',
      completed_at = now(),
      updated_at   = now(),
      winner_id    = case
                       when host_id = p_forfeiting_player then guest_id
                       else host_id
                     end
  where id     = p_match_id
    and status = 'in_progress'
    and p_forfeiting_player in (host_id, guest_id)
  returning * into v_match;
  return v_match;
end; $$;

-- 5d. Auto-delete stale/abandoned rooms
create or replace function public.cleanup_stale_matches()
returns void
language sql security definer as $$
  delete from public.matches
  where (status = 'waiting'    and created_at   < now() - interval '10 minutes')
     or (status = 'in_progress' and updated_at  < now() - interval '45 minutes')
     or (status = 'completed'   and completed_at < now() - interval '48 hours');
$$;

-- ============================================================
-- 6. Cron schedule (requires pg_cron extension)
-- Enable pg_cron in Dashboard → Database → Extensions first.
-- If pg_cron isn't available on your plan, call cleanup_stale_matches()
-- from a scheduled Edge Function instead.
-- ============================================================
-- select cron.schedule(
--   'cleanup-stale-matches',
--   '*/10 * * * *',
--   $$ select public.cleanup_stale_matches(); $$
-- );

-- ============================================================
-- 7. Migrations (Run these if updating an existing schema)
-- ============================================================
-- ALTER TABLE public.matches ADD COLUMN IF NOT EXISTS game_type text not null default 'sudoku';
