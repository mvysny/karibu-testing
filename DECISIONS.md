# Technical decisions

Append-only, dated log of non-trivial technical decisions and their rationale — the "why", the
alternatives rejected, and the evidence they rested on. **Never edit an old entry**; if a decision
changes, add a new entry that supersedes it (and note the supersession in both). Mechanics of *how*
things work today live in code KDoc, not here; user-facing usage lives in the `README.md` files.

Newest entries on top.

---

## 2026-07-06 — Reaping a lost-beacon UI: `MockVaadin.reapInactiveUIs()`

**Context.** The `2026-07-06` F5/beacon entry below shipped `UnloadBeaconTiming.NEVER` = "the unload
beacon was lost, so the old UI lingers alive alongside the new one," but deliberately did *not* model
the heartbeat/idle-UI reap that would eventually close it in production. A downstream Vaadin tab-scope
library now needs exactly that follow-up: assert that a UI abandoned by a lost beacon eventually gets
closed and detached (so its per-UI resources are released). This unparks `ideas/heartbeat-emulation.md`.

**What real Flow does.** `VaadinService.cleanupSession()` (from `requestEnd`) runs `closeInactiveUIs()`
— for each UI with `!isUIActive(ui) && !ui.isClosing()`, calls `ui.close()` — then `removeClosedUIs()`
detaches + `session.removeUI()`s them. `isUIActive` is **time-based**: a UI is inactive once it has
missed ~3 heartbeats (`getHeartbeatTimeout() = heartbeatInterval * 3.1`, vs. the UI's last-heartbeat
timestamp).

**Decisions.**

1. **Emulate the outcome, not the timing.** Karibu is synchronous and clock-less, so we reproduce the
   *effect* of the reap (UI closed, detach listeners fire, removed from session) but not *when* it
   happens — `reapInactiveUIs()` reaps immediately when called. A test therefore cannot assert
   "not reaped before N intervals, reaped after"; that assertion tests Flow's timeout machinery, not
   app code, so it is out of scope. **Rejected: a virtual/advanceable mock clock** (stamping heartbeat
   timestamps, faking `HeartbeatHandler`) — large machinery that fights Karibu's clock-less core, and
   it only buys the ability to test Flow's own behavior, near-zero value for app authors.

2. **Flag the abandoned UI; reap by flag, not by "non-current".** In Karibu's model the *only* way a
   live, non-current UI can linger is `UnloadBeaconTiming.NEVER` — the other real reap causes (frozen
   tab, laptop asleep, background-tab throttling) can't even be expressed. So the `NEVER` branch of
   `reloadCurrentUI()` marks the old UI via `ComponentUtil.setData(ui, <key>, true)`, and
   `reapInactiveUIs()` closes exactly the flagged, non-current UIs (reusing the private `discardOldUI()`
   for close + detach + `removeUI` + current-UI juggling). **Rejected: a generic "close all
   non-current, non-closing UIs" reaper** (the working name `expireInactiveUIs()`): it defines
   eligibility by an *incidental* property rather than the *meaningful* one (this UI was abandoned by a
   lost beacon); the flag is honest, future-proof against legitimate multi-UI tests, and
   self-documenting.

3. **Never reap the current UI.** In a test the current UI is the live one under test; real Flow would
   reap even an active tab's UI if the whole browser died, but doing so here would break the harness.

4. **Naming: `reapInactiveUIs()`.** Mirrors Flow's own `closeInactiveUIs()` / `isUIActive()`
   vocabulary (discoverable by grepping flow-server), and names the *production behavior emulated*
   rather than the mock mechanics — consistent with `MockPage.reload()`. **Rejected:**
   `reapUIsThatMissedHeartbeats()` (the body never counts a heartbeat — re-introduces the
   `expireInactiveUIs` overclaim, and reads as a clause not an identifier) and `reapUnloadedUIs()`
   (backwards — these UIs are precisely the ones that *failed* to unload). "Heartbeat" and "beacon"
   live in the method KDoc, not the signature.

**Consequences / limitations.** Reproduces only the lost-beacon subset of Flow's reap causes — which
is the only subset a browserless double can produce, so it is complete within Karibu's model, not a
general heartbeat emulation. No timing is modeled. `@PreserveOnRefresh` UIs are never flagged (Flow
ignores the beacon there), so they are never reaped by this.

**Where it lives.** `MockVaadin.reapInactiveUIs()` + `UNLOAD_BEACON_LOST_KEY` marker set in
`reloadCurrentUI()`'s `NEVER` branch; `UnloadBeaconTiming.NEVER` KDoc points at it. Tests in
`MockVaadinTest` (`unload beacon timing on F5` → the three `reapInactiveUIs …` cases). Superseded idea
file: `ideas/heartbeat-emulation.md` (deleted on implementation).

---

## 2026-07-06 — F5 reload lifecycle: overlay teleport & unload-beacon timing

**Context.** [#207](https://github.com/mvysny/karibu-testing/issues/207): `MockPage.reload()` closed &
detached the old UI *before* creating the new one, which silently dropped any open
`Dialog`/`Notification` on an F5 of a `@PreserveOnRefresh` view. A downstream "Vaadin tab scope"
library also needed to test that server-side state tied to the UI lifecycle survives F5, including the
adversarial ordering where the old UI dies before the new one is created. (Such a tab-scope test is
feasible in Karibu because the faked window name is a stable constant — `createExtendedClientDetails`,
documented "persists on reload" — so the old and new UI share a tab id, and the preserved-chain lookup,
which keys on window name, is unaffected by the beacon timing.)

**What real Flow does (traced against flow-server 25.2.1).** On F5 the browser fires an unload beacon
(`navigator.sendBeacon`, a POST with `unload=true`) during `pagehide`, *before* it requests the new
document; the new UI is created later, by the client→server init request after the reloaded page
boots. Two annotation-dependent paths:

- **`@PreserveOnRefresh`**: `ServerRpcHandler.handleUnloadBeaconRequest` **ignores** the beacon
  ("Eager UI close ignored for @PreserveOnRefresh view"). The old UI stays live until the *new* UI's
  navigation runs `AbstractNavigationStateRenderer.disconnectElements()`, which (a) marks the old UI
  with the internal `ReplacedViaPreserveOnRefresh` sentinel via `Element.removeFromTree(false)`,
  (b) teleports the old UI's remaining UI-level children (dialogs/notifications) onto the new UI via
  `UIInternals.moveElementsFrom()`, then (c) `oldUI.close()`. Observed child order on the new UI:
  **route root first, then the teleported overlays** (route re-attached by `updateRoot` after the
  overlays were moved).
- **non-`@PreserveOnRefresh`**: the beacon calls `oldUI.close()`. `close()` only sets `isClosing`; the
  actual detach + `session.removeUI()` happen in `removeClosedUIs()`, run from
  `requestEnd → cleanupSession` at the *end of the beacon request* — i.e. before the new UI's init
  request begins. If the beacon is dropped, the old UI lingers until the heartbeat/idle-UI cleanup
  (`closeInactiveUIs()`, ~3× the heartbeat interval) reaps it.

**Scenario matrix — 4 cells, not 6.** `{eager, late, lost}` beacon timing × `{preserve, non-preserve}`
looks like 6, but the beacon is a no-op under `@PreserveOnRefresh`, so all three preserve cells
collapse into one. The terminal state is identical across the non-preserve eager/late columns (old
closed+removed, one live UI); they differ only in the transient ordering that mid-reload listeners
(`UIInitListener`, detach listeners) observe.

**Decisions.**

1. **Reorder rather than re-implement.** Karibu drives Flow's *real* navigation pipeline, so for the
   preserve case we simply keep the old UI alive & registered while the new UI navigates and let Flow
   do the teleport, the sentinel, the child ordering and `oldUI.close()`. Rejected: re-implementing
   `moveElementsFrom`/ordering in Karibu — it would drift from Flow.

2. **Give the reloaded UI a fresh `uiId`.** `uiId` is the key in `VaadinSession.uIs`; reusing `1` made
   `session.addUI(newUI)` evict the still-live old UI, collapsing the transient two-live-UI window.
   The eager path can still reuse `uiId 1` because the old UI is removed *before* `addUI`.

3. **Make non-preserve beacon timing configurable; default EAGER.** `KaribuConfig.unloadBeaconTiming`
   = `UnloadBeaconTiming { EAGER, LATE, NEVER }`.
   - **EAGER (default)**: close+detach+remove old, then create new. Chosen as default because it is
     both the common production ordering *and* the pre-2.7.1 Karibu behavior, and it's the
     detach-before-attach case the tab-scope library must test.
   - **LATE**: create new, then close+detach+remove old.
   - **NEVER**: old UI lingers alongside the new one (beacon lost). We deliberately do **not** model
     the heartbeat reap that would eventually close it (no time axis; see
     `ideas/heartbeat-emulation.md`).
   Ignored for `@PreserveOnRefresh` (Flow ignores the beacon there).

4. **One flag, no new public primitives.** Rejected an explicit `closeUIViaBeacon()` /
   `expireInactiveUIs()` API: there is no browser, so Karibu fires the simulated beacon *inside*
   `reloadCurrentUI()` at the configured point; a caller-invoked primitive would be redundant. A
   heartbeat-reap driver was punted to a separate idea rather than built speculatively.

**Consequences / limitations.** For non-preserve, only the terminal state and eager/late/lost
*orderings* are reproduced — not wall-clock timing (e.g. "old UI survives N heartbeats then dies").
That's inherent to a synchronous, browserless, heartbeat-less test double. Naming follows Flow's own
vocabulary (`isUnloadBeaconRequest`) and the browser Beacon API.

The EAGER default also fixes a pre-#207 defect: the old code left the old UI detached-but-not-closed
(`isClosing()` stayed `false`) and effectively leaked it; the beacon path now closes and removes it
properly, matching Flow's real `ui.close()` — a strict improvement over the pre-#207 ordering it
otherwise restores.

**Where it lives.** `MockVaadin.reloadCurrentUI()` / `discardOldUI()` / `isPreserveOnRefreshTarget()`,
`KaribuConfig.unloadBeaconTiming`, `UnloadBeaconTiming`; test matrix in `MockVaadinTest`
(`page reload F5 lifecycle`, `unload beacon timing on F5`). Superseded idea file:
`ideas/beacon-reload-timing.md` (deleted on implementation).
