# Emulating Vaadin heartbeats / idle-UI reaping

Status: **parked** (idea only, not scheduled). Split out of
[beacon-reload-timing.md](beacon-reload-timing.md).

## The idea

Let a test drive Flow's periodic idle-UI cleanup so a lingering UI (e.g. the one left alive by
`UnloadBeaconTiming.NEVER` after an F5 where the unload beacon was lost) can eventually be closed and
removed — the way production would reap it.

## What real Flow does

`VaadinService.cleanupSession()` (invoked from `requestEnd`) runs:

- `closeInactiveUIs(session)` — for each UI where `!isUIActive(ui) && !ui.isClosing()`, calls
  `ui.close()` and purges its preserved-chain cache.
- `removeClosedUIs(session)` — for each `ui.isClosing()`, calls `session.removeUI(ui)` (detach + drop).

`isUIActive(ui)` is **time-based**: a UI is inactive once it has missed roughly three heartbeats —
`getHeartbeatTimeout() = deploymentConfiguration.getHeartbeatInterval() * 3.1`, compared against the
UI's last-heartbeat timestamp.

## Why it's hard (and why it's parked)

Faithful emulation needs a **time axis** Karibu doesn't have:

- Flow compares wall-clock timestamps; a UI is only reaped after ~3 missed heartbeat intervals.
- Karibu is synchronous and clock-less. To emulate this we'd need either a controllable/virtual clock
  or an explicit "advance N heartbeats" API, plus a way to stamp/track each UI's last-heartbeat time.

Options if we pick this up later:

1. **Explicit, timeless reaper** — `MockVaadin.expireInactiveUIs()` that just closes+removes all
   non-current, non-closing UIs immediately (ignores timestamps). Simple; models "the reap eventually
   happened" without modeling *when*.
2. **Virtual clock** — a settable/advanceable mock time so `isUIActive` behaves realistically and a
   test can assert "not reaped before 3 intervals, reaped after." More faithful, much more machinery
   (touches heartbeat timestamps, `DeploymentConfiguration.getHeartbeatInterval`, possibly a fake
   `HeartbeatHandler`).

Leaning toward option 1 if/when a real test needs it — option 2 only if someone specifically wants to
test heartbeat-timeout behavior itself.

## Not needed for the beacon feature

The beacon feature ships with `NEVER` meaning "old UI lingers, not reaped." This idea is only about
adding the *follow-up* reap; the beacon work does not depend on it.
