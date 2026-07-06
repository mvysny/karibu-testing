# F5 reload: unload-beacon timing fidelity

Status: **plan agreed — ready to implement.** Ships in the same release as the #207 overlay-teleport
fix, because a downstream "Vaadin tab scope" library needs it to test scope preservation across F5.

## Background

Follow-up to [#207](https://github.com/mvysny/karibu-testing/issues/207) (overlay teleport on
`@PreserveOnRefresh` F5). That fix reordered `MockPage.reload()` to create the new UI while the old
one is still alive, then discard the old one — see `MockVaadin.reloadCurrentUI()`.

That models exactly **one** of several real F5 orderings. Real Flow has more, driven by the browser
unload beacon.

## What real Flow does (server-side, traced against flow-server 25.2.x)

On F5 the browser fires an unload beacon (the **Beacon API**, `navigator.sendBeacon`, a POST with
`unload=true`) during `pagehide`/`unload`, **before** it requests the new document. The new UI is not
created by the bootstrap GET either — it's created by the *subsequent* client→server init request
after the new page's JS boots. So the beacon normally reaches the server **before** the new UI exists:

```
unload → sendBeacon(POST, unload=true) ──► ServerRpcHandler.handleUnloadBeaconRequest
                                             (new UI does not exist yet)
GET / → bootstrap HTML → JS boots → init request ──► BootstrapHandler.createAndInitUI → session.addUI(newUI)
```

`ServerRpcHandler.handleUnloadBeaconRequest` (flow-server 25.2.1):

```java
if (isPreserveOnRefreshTarget(ui)) {
    // "Eager UI close ignored for @PreserveOnRefresh view"   <-- beacon is a NO-OP for preserve
} else {
    ui.close();                                               // non-preserve: beacon closes old UI
}
```

`sendBeacon` is best-effort, so three timings are possible for a **non-preserve** target:

- **eager** — beacon lands before the new UI is created: old UI `close()`d first, then new UI created.
  There can be a brief instant with *zero* live UIs. (The common ordering, and the pre-#207 Karibu ordering.)
- **late** — beacon lands after the new UI init: old + new both live for a moment, then old closed.
- **lost** — beacon never lands: old UI stays live (not closing) alongside the new UI. In real Flow it
  is eventually reaped by the heartbeat / idle-UI cleanup — **we deliberately do not model that clock
  here** (see [heartbeat-emulation.md](heartbeat-emulation.md)); under Karibu the old UI simply lingers.

Detach timing note: `ui.close()` only sets `isClosing`. The actual detach + session removal happens in
`removeClosedUIs()`, called from `requestEnd → cleanupSession`. Because the beacon is a *separate*
request, that cleanup runs at the **end of the beacon request** — before the new UI's init request
begins. So in the eager case the old UI is genuinely detached+removed before the new UI is created,
which is precisely the **detach-before-attach** ordering the tab-scope library needs. Karibu
(synchronous, no time axis) collapses close+detach+remove into one step.

## The scenario matrix — 4 cells, not 6

Naive product = 3 beacon-timings × {preserve, non-preserve} = 6. But **the beacon is a no-op under
`@PreserveOnRefresh`** (Flow ignores it), so all three preserve cells are identical → **4 distinct
scenarios**:

| | beacon eager | beacon late | beacon lost |
|---|---|---|---|
| **non-preserve** | old closed(+detached,+removed) *then* new created; possible zero-UI instant | new created (two live UIs) *then* old closed | new created; old lingers alive (never reaped here) |
| **`@PreserveOnRefresh`** | *(all three identical)* old stays live → new navigates → overlays teleport (`moveElementsFrom`) → old closed | ← same | ← same |

- The **terminal state** is identical across the eager/late non-preserve columns (old closed+removed,
  one live UI); they differ only in the **transient ordering** that mid-reload listeners
  (`UIInitListener`, detach listeners, scope-destroy callbacks) observe.
- Preserve ordering is **fixed** by Flow and beacon-independent; it's what `reloadCurrentUI()` already
  implements and must not change.

## Why it matters (downstream driver)

A "Vaadin tab scope" library scopes beans to a browser tab (keyed by window name) and must preserve
scoped beans across F5. The dangerous case is **non-preserve + eager**: the old UI is detached
*before* the new UI exists. A scope that naively destroys tab state on old-UI-detach loses it — a bug
only reproducible if Karibu can put old-UI detach *before* new-UI attach. Today's `reloadCurrentUI()`
only does "late", so that bug can't be written as a test. Hence this is a release blocker.

A Karibu tab-scope test is meaningful because the faked window name is a stable constant
(`createExtendedClientDetails(windowName = "ROOT-2521314-0.2626611481")`, documented "persists on
reload"), so old and new UI share a tab id, and the preserved-chain lookup (keyed on window name) is
unaffected by the timing.

## Agreed design — one flag, no new methods

```kotlin
/**
 * Controls when the browser unload beacon (navigator.sendBeacon) is simulated during an F5 reload,
 * relative to the creation of the new UI. Only affects non-@PreserveOnRefresh targets — Flow ignores
 * the beacon for @PreserveOnRefresh (the old UI is closed by the new UI's navigation instead).
 */
public enum class UnloadBeaconTiming {
    /** Beacon arrives before the new UI is created: the old UI is closed, detached and removed
     *  first, then the new UI is created. The common production ordering (and the pre-#207 Karibu
     *  ordering). Default. */
    EAGER,
    /** Beacon arrives after the new UI is created: both UIs are briefly live, then the old UI is
     *  closed, detached and removed. */
    LATE,
    /** Beacon is never delivered ("beacon lost"): the old UI is left alive alongside the new one and
     *  is not closed. Note: real Flow would eventually reap it via the heartbeat timeout; Karibu does
     *  not model that clock (see heartbeat-emulation.md). */
    NEVER,
}

// KaribuConfig (fits the existing fakeExtendedClientDetails / initDefaultRoute pattern):
public var unloadBeaconTiming: UnloadBeaconTiming = UnloadBeaconTiming.EAGER
```

There is **no** public beacon primitive: `page.reload()` is called deep in app code, there is no
browser, so Karibu simulates the beacon *inside* `reloadCurrentUI()` at the point the flag dictates.
There is **no** `expireInactiveUIs()` here either — heartbeat reaping is out of scope (its own idea).

### Control flow

```
reloadCurrentUI(oldUI):
    if isPreserveOnRefreshTarget(oldUI):          # mirror ServerRpcHandler.isPreserveOnRefreshTarget:
                                                  #   oldUI.internals.activeRouterTargetsChain has @PreserveOnRefresh
        # beacon IGNORED; teleport path, unchanged from #207
        createUI(uiId = oldUI.uiId + 1)           # Flow teleports overlays off old, closes old
        detach + remove oldUI
    else:
        when KaribuConfig.unloadBeaconTiming:
            EAGER -> close+detach+remove oldUI ; createUI(uiId = 1)    # old gone before new
            LATE  -> createUI(uiId = oldUI.uiId + 1) ; close+detach+remove oldUI
            NEVER -> createUI(uiId = oldUI.uiId + 1)                   # old lingers, untouched
```

EAGER can reuse `uiId = 1` (old UI removed before `addUI`, so no eviction collision); LATE/NEVER need
the fresh `uiId` to avoid evicting the still-live old UI from `session.uIs` (the collision fixed in #207).

## Consequence for the #207 fix

The #207 fix currently implements **LATE** for non-preserve. With the EAGER default this flips to the
pre-#207 "close old first" ordering (but with correct `isClosing`/removal, i.e. Flow's real beacon
`close()` — a strict improvement over pre-#207 which left `isClosing==false` and leaked). All existing
#207 tests use a *preserve* target, so they are unaffected by the default.

## Test matrix to add (kt10-tests, runs in all four environments)

- non-preserve × {EAGER, LATE, NEVER}: old-UI detach vs new-UI init ordering (via `UIInitListener` /
  detach listener), `isClosing`, and session UI count at each phase (EAGER: old gone before new; LATE:
  2 during new init then 1; NEVER: 2 permanently).
- preserve × {EAGER, LATE, NEVER}: identical outcome (flag ignored) — overlays teleported, old closed,
  one live UI.
- Downstream-style: a UI-detach listener that would "destroy tab state" must run *after* the new UI is
  attached under LATE, *before* it under EAGER — the app must survive both.

## Deferred / linked

- [heartbeat-emulation.md](heartbeat-emulation.md) — reaping the NEVER-lingering UI faithfully (needs
  simulated time / heartbeat counting).
- [karibuconfig-reset.md](karibuconfig-reset.md) — `KaribuConfig` flags (incl. `unloadBeaconTiming`)
  are not reset between tests today; whether/how to reset.
