# Technical decisions

Append-only, dated log of non-trivial technical decisions and their rationale — the "why", the
alternatives rejected, and the evidence they rested on. **Never edit an old entry**; if a decision
changes, add a new entry that supersedes it (and note the supersession in both). Mechanics of *how*
things work today live in code KDoc, not here; user-facing usage lives in the `README.md` files.

Newest entries on top.

---

## 2026-07-06 — F5 reload lifecycle: overlay teleport & unload-beacon timing

**Context.** [#207](https://github.com/mvysny/karibu-testing/issues/207): `MockPage.reload()` closed &
detached the old UI *before* creating the new one, which silently dropped any open
`Dialog`/`Notification` on an F5 of a `@PreserveOnRefresh` view. A downstream "Vaadin tab scope"
library also needed to test that server-side state tied to the UI lifecycle survives F5, including the
adversarial ordering where the old UI dies before the new one is created.

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

**Where it lives.** `MockVaadin.reloadCurrentUI()` / `discardOldUI()` / `isPreserveOnRefreshTarget()`,
`KaribuConfig.unloadBeaconTiming`, `UnloadBeaconTiming`; test matrix in `MockVaadinTest`
(`page reload F5 lifecycle`, `unload beacon timing on F5`). Superseded idea file:
`ideas/beacon-reload-timing.md` (deleted on implementation).
