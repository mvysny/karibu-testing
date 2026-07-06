# Emulating Vaadin heartbeats / idle-UI reaping

Status: **parked** (idea only, not scheduled). Split out of the F5 reload / unload-beacon work
(see the `2026-07-06` entry in [../DECISIONS.md](../DECISIONS.md)).

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

## Why the timing can't be emulated (and why it's parked)

Faithful emulation of the *mechanism* needs a **time axis** Karibu doesn't have:

- Flow compares wall-clock timestamps; a UI is only reaped after ~3 missed heartbeat intervals.
- Karibu is synchronous and clock-less. To emulate this we'd need either a controllable/virtual clock
  or an explicit "advance N heartbeats" API, plus a way to stamp/track each UI's last-heartbeat time.

But we don't need the mechanism — we need the *outcome* for the one scenario Karibu can actually
produce. See below.

## Chosen shape when unparked: flag the abandoned UI, reap on demand

Key observation: in Karibu's world there is exactly **one** way a live, non-current UI can be left
sitting in `session.uIs` — `UnloadBeaconTiming.NEVER` on an F5 (the "tab closed but the unload beacon
was lost" case). Karibu can't express the other real-world reap causes (frozen tab, laptop asleep,
background-tab timer throttling) at all. So the set of "UIs Flow's heartbeat cleanup would eventually
reap" collapses, in Karibu, to "UIs Karibu left behind because the beacon was `NEVER`."

That makes a targeted design both honest and functionally complete-within-Karibu:

1. **Flag on creation.** In the `NEVER` branch of `MockVaadin.reloadCurrentUI()`, mark the lingering
   old UI via `ComponentUtil.setData(oldUI, <marker>, true)` before leaving it live. The marker
   travels with the UI, GC's away with it, and needs no separate bookkeeping. (Distinct from Flow's
   own `ReplacedViaPreserveOnRefresh` element-node sentinel, which belongs to the preserve path.)
2. **Reap on demand.** A new public `MockVaadin.reapInactiveUIs()` iterates a snapshot of
   `session.uIs` and, for each flagged, non-current UI, calls the existing private `discardOldUI()` —
   reusing its close + detach-listener firing + `session.removeUI()` + current-UI juggling. The
   detach-listener firing is the whole observable point (e.g. a tab-scope library releasing per-UI
   state when the abandoned tab is finally cleaned up).

   The name mirrors Flow's actual `VaadinService.closeInactiveUIs()` / `isUIActive()`, so a Vaadin dev
   grepping Flow source for this cleanup lands on it; it names the *production behavior emulated*, not
   the mock mechanics (consistent with `MockPage.reload()` etc.). Both the trigger (lost unload beacon)
   and the emulated mechanism (heartbeat / idle-UI reap) belong in the kdoc, not the identifier — e.g.
   *"Emulates the outcome of Flow's heartbeat / idle-UI reap (`closeInactiveUIs`) for UIs abandoned by
   a lost unload beacon (`UnloadBeaconTiming.NEVER`)."* Deliberately **not** named
   `reapUIsThatMissedHeartbeats()` (the body never counts a heartbeat — that would re-introduce the
   `expireInactiveUIs` overclaim) nor `reapUnloadedUIs()` (backwards: these UIs are the ones that
   *failed* to unload).

### Why a flag, not "close all non-current UIs"

A generic "close every non-current, non-closing UI" reaper (the rejected `expireInactiveUIs()` idea)
defines eligibility by an *incidental* property (not-current) rather than a *meaningful* one (this UI
was abandoned by a lost beacon). Today the two sets coincide, but the flag still wins: it is an honest
predicate, it is future-proof (a legitimate multi-UI test wouldn't get wrongly swept), and it is
self-documenting when someone inspects `session.uIs`.

### Honest limitations to state in the kdoc

- **No timing.** `reapInactiveUIs()` reaps immediately when called, not after ~3 heartbeat intervals.
  It models *that* the reap eventually happened, never *when*. A test therefore cannot assert "not
  reaped before N intervals, reaped after" — but that assertion tests Flow's timeout machinery, not
  app code, so it's out of scope by design.
- **Lost-beacon abandonment only.** It reaps the `NEVER`-abandoned subset of Flow's reap causes, which
  is the only subset Karibu can produce — so it's complete within Karibu's model, not a general
  heartbeat emulation.
- **Never reaps the current UI.** In a test the current UI is the one under test; real Flow would reap
  even an active tab's UI if the whole browser died, but that's meaningless here.

### Rejected: virtual clock

A settable/advanceable mock time so `isUIActive` behaves realistically (touching heartbeat timestamps,
`DeploymentConfiguration.getHeartbeatInterval`, possibly a fake `HeartbeatHandler`). Much more
machinery, and it fights Karibu's synchronous/clock-less core. Only worth it if someone specifically
wants to test heartbeat-*timeout* behavior itself — which is Flow's behavior, not the app's, so
near-zero value for app authors. Not planned.

### Composes with the shipped beacon feature

`NEVER` *sets* the flag; `reapInactiveUIs()` *consumes* it — two halves of one lifecycle, no new
global state, no clock. Build it when a real test needs it, not speculatively.

## Not needed for the beacon feature

The beacon feature ships with `NEVER` meaning "old UI lingers, not reaped." This idea is only about
adding the *follow-up* reap; the beacon work does not depend on it.
