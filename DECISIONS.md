# Technical decisions

Append-only, dated log of non-trivial technical decisions and their rationale — the "why", the
alternatives rejected, and the evidence they rested on. **Never edit an old entry**; if a decision
changes, add a new entry that supersedes it (and note the supersession in both). Mechanics of *how*
things work today live in code KDoc, not here; user-facing usage lives in the `README.md` files.

Newest entries on top.

---

## 2026-07-07 — Browser-free login helper + OAuth stance: `MockVaadin.login()`/`logout()` (issue #143)

**Context.** [Issue #143](https://github.com/mvysny/karibu-testing/issues/143): a Keycloak-OAuth app
with all views `@PermitAll` hit a `NotFoundException` from `MockVaadin.setup()` navigating to `""`,
and the reporter "fixed" it by relaxing the root view to `@AnonymousAllowed` — defeating the security
model under test. The issue predates two things that already changed the picture: (a) setup's initial
navigation is now guarded by `initDefaultRoute && registry.getNavigationTarget("").isPresent`
(`MockVaadin.kt`), so an app with no `""` route no longer crashes at setup; (b) the
`MockSpringSecurity.mock()` bridge (entry below). What remained was ergonomics: everyone hand-copied
the same two-line `userPrincipalInt` + `isUserInRole` snippet from the README.

**Decisions.**

1. **Ship `MockVaadin.login(userName, roles)` / `logout()` in core (v10), framework-agnostic.**
   Sugar over the existing `FakeRequest.userPrincipalInt` + `isUserInRole` primitives, backed by a
   new **public** `MockPrincipal` (previously an `internal` test-only class, now promoted so both the
   helper and user code can name it). `@JvmStatic @JvmOverloads` keep the Java/Groovy call site clean
   (`MockVaadin.login("admin", List.of("ADMIN"))`) — no `LocatorJ` mirror needed. Placed on
   `MockVaadin` alongside `setup`/`tearDown` for discoverability.
2. **Do NOT emulate the OAuth redirect flow.** The external redirect to the IdP is done by a servlet
   filter before Vaadin runs and needs a real browser + running IdP — squarely outside a browser-free
   library. The documented stance: skip the redirect, `login()` (or `MockSpringSecurity.mock()` +
   `@WithMockUser`), then navigate and assert on authenticated behavior. Rejected any filter-chain or
   `forwardToExternalUrl` emulation as untestable browser-free and out of scope.
3. **OIDC-claims case stays docs-only, no new dependency.** Apps that read `OidcUser` claims off the
   principal populate an `OAuth2AuthenticationToken` in `SecurityContextHolder` themselves (which
   `MockSpringSecurity.mock()` already reads). Adding a `MockSpringSecurity.loginOidc(...)` helper was
   rejected: it would pull `spring-security-oauth2-client` into the module and duplicate what Spring
   Security Test / a two-line context setup already do. Documented in the Spring README instead.

**Where it lives.** `Security.kt` (public `MockPrincipal`), `MockVaadin.login`/`logout` (mechanics in
their KDoc); tests reuse `SecurityTestbatch` (`AbstractSecurityTests`) — `user`/`admin` converted to
`login()`, plus a `logout()` test and a login→navigate→logout→reroute check against
`NavigationAccessControl`. README "Security/Principal/isUserInRole" section rewritten (adds the OAuth
cookbook and updates the deprecated `.mock` → `.fake`); Spring README gains an "OAuth / OpenID
Connect" section.

---

## 2026-07-07 — Spring Security support: `MockSpringSecurity.mock()` (issues #94, #180)

**Context.** [Issue #94](https://github.com/mvysny/karibu-testing/issues/94) asked for Spring
Security Test's `@WithMockUser` to work under Karibu; [#180](https://github.com/mvysny/karibu-testing/issues/180)
is the same gap seen from `NavigationAccessControl` redirecting to login instead of the error view.
Root cause: `@WithMockUser`/`@WithUserDetails`/`@WithSecurityContext` only populate
`SecurityContextHolder`. In production a servlet filter (`SecurityContextHolderAwareRequestWrapper`)
copies that context onto the request, and Vaadin route security
(`NavigationAccessControl`/`AccessAnnotationChecker`) reads **exactly** `request.getUserPrincipal()`
and `request.isUserInRole()`. Karibu runs no filter, so those two request methods stayed empty.

**The README's prior workaround was insufficient.** It overrode only `getUserPrincipal()`, never
`isUserInRole()` — so an authenticated user passed `@PermitAll` but **failed every `@RolesAllowed`
gate**. Bridging *both* methods is the actual fix.

**Decisions.**

1. **Opt-in helper, not auto-install.** `MockSpringSecurity.mock(rolePrefix = "ROLE_")` installs a
   `MockVaadin.mockRequestFactory` that sources principal + roles from `SecurityContextHolder`.
   Auto-installing (e.g. from `MockSpringServletService`) was rejected: the Spring module is used by
   non-security apps too, and referencing `SecurityContextHolder` unconditionally would
   `NoClassDefFoundError` when spring-security is absent. Kept `spring-security-core` a `compileOnly`
   dependency so non-security users never need it on the classpath.
2. **Mirror `SecurityContextHolderAwareRequestWrapper` faithfully.** Anonymous is *not* logged in:
   both `null` and `AnonymousAuthenticationToken` map to a `null` principal, via
   `AuthenticationTrustResolverImpl.isAnonymous()` (picks up custom anonymous tokens too) rather than
   a raw `instanceof`. The holder is read **lazily inside** the overridden methods, so it works
   whether the user logs in before or after `MockVaadin.setup()`, and login/logout mid-test is
   honored (verified by a test).
3. **Configurable role prefix, default `ROLE_`.** Mirrors Spring's `GrantedAuthorityDefaults`:
   `@RolesAllowed("ADMIN")` → `isUserInRole("ADMIN")` → matches authority `ROLE_ADMIN`. `""` supports
   apps gating on raw authorities.
4. **Kotlin source in the (otherwise-Java) Spring module.** The `kotlin` plugin is already applied to
   every subproject, so a Kotlin file gives the `mock(rolePrefix = "ROLE_")` default-arg ergonomics;
   `@JvmStatic @JvmOverloads` keep the Java call site clean (`MockSpringSecurity.mock()`).
5. **Self-contained behavioral test in the Spring module.** The module wasn't part of the
   `kt10-testrun-*` battery and its `src/test` had only a compile smoke test. Since `mock()` only
   touches `mockRequestFactory` + `FakeRequest` + `SecurityContextHolder`, a plain `MockVaadin.setup()`
   test suffices; the test populates `SecurityContextHolder` directly (exactly what `@WithMockUser`
   does under the hood), avoiding a full `SpringExtension`/`spring-security-test` context.

**Known caveat (accepted).** `MockVaadin.mockRequestFactory` is a global that `tearDown()` does not
reset (same class of issue as `ideas/karibuconfig-reset.md`) — hence the "call from `@BeforeEach`"
guidance. The leak is harmless: a later non-security test runs with an empty `SecurityContextHolder`,
so `getUserPrincipal()` returns `null`, identical to the default `FakeRequest`.

**Where it lives.** `karibu-testing-v10-spring`: `MockSpringSecurity.kt` (mechanics in its KDoc);
`compileOnly`/`testImplementation` `spring-security-core` in `build.gradle.kts`; tests in
`MockSpringSecurityTest`. README "Spring Security" section rewritten. Supersedes the principal-only
snippet previously documented there.

---

## 2026-07-07 — Assert on `LitRenderer` HTML via JSoup (issue #175)

**Context.** [Issue #175](https://github.com/mvysny/karibu-testing/issues/175): `_getFormattedRow()`
/ `_getPresentationValue()` reduce a `LitRenderer` cell to plain text (JSoup `textRecursively` over
the rendered template), so tests could assert `Foo Bar` but not that the cell contains, say, a
conditionally-emitted `<a>` tag. The reporter wanted to inspect the markup without rolling their own
template renderer.

**Alternatives.**
1. **Config flag to make `_getPresentationValue()` return the raw template HTML** (the maintainer's
   first instinct on the issue). Rejected: a raw `String` just pushes the user into `contains("<a")`
   / regex land — the brittle "roll my own" the issue wanted to avoid — and overloading the return
   type of an existing function via global config is a sharp edge.
2. **Expose the parsed JSoup tree.** Chosen. JSoup already gives a solid, well-known query API
   (`select("a[href]")`, `attr()`, …), which is exactly the downstream ergonomics asked for.

**Decisions.**

1. **New functions, `_getPresentationValue()` unchanged (non-breaking).**
   `LitRenderer._getPresentationHtml()` returns the raw rendered template; `_getPresentationJsoup()`
   returns the parsed `<body>` `Element` (via `Jsoup.parseBodyFragment(...).body()` — a Lit template
   is a fragment, not a full page, so returning `body()` avoids the synthetic `<html>/<head>`
   wrapper while `select()` still recurses). Both are also exposed on `Grid.Column` (delegating down;
   fail with a clear message if the column's renderer isn't a `LitRenderer`) so they sit next to
   `_getFormatted`. `_getPresentationValue()`'s LitRenderer branch was refactored to reuse
   `_getPresentationJsoup().textRecursively`.

2. **JSoup made an explicit `api` dependency of `karibu-testing-v10`.** JSoup is *already* on every
   consumer's classpath transitively (`com.vaadin:flow-server` → `org.jsoup:jsoup`), so this adds no
   new artifact in practice; we depend on it explicitly (pinned to the same 1.22.2 Flow ships) so the
   API keeps working even if Flow ever drops it, and `api` scope because `_getPresentationJsoup()`
   returns a JSoup type. No Java-facing mirror (`LocatorJ`-style) was added: the Kotlin extension
   functions are already callable from Java as `RenderersKt`/`GridKt` statics; the README documents
   that instead.

---

## 2026-07-07 — Open/click a `ContextMenu` via its target component (issue #20)

**Context.** [Issue #20](https://github.com/mvysny/karibu-testing/issues/20): tests could only
interact with a `ContextMenu` if they held a reference to it; `_find(ContextMenu.class)` returned
nothing. Root cause (verified against Vaadin 25.2.1 and 25.3.0-alpha3): a `ContextMenu` is **not in
the server-side element tree** until it is *opened*. `OverlayAutoAddController` attaches the menu to
the UI (`ui.addToModalComponent`) only on open and removes it on close; the `opened`-property path
defers the add to `beforeClientResponse`, which is never flushed browserlessly. The target holds no
reference back to the menu — the only link is `ContextMenu.getTarget()`. Vaadin exposes no reverse
lookup (no `Component.getContextMenu()`, no registry), so this had to be solved on our side.

**Two mechanisms were found (both reflection-free at the dispatch layer):**
1. **Discovery** — the target's element retains a `vaadin-context-menu-before-open` DOM listener
   (registered by `ContextMenuBase.setTarget`) whose bound method-ref captures the menu; it can be
   recovered by reflection. Rejected here: it needs internal reflection *and* a discovered-but-unopened
   menu is **incompletely populated** (static `addItem` items are present, but dynamic content —
   `GridContextMenu.setDynamicContentHandler`, or items added in open/before-open listeners — is not).
2. **Faithful open** — firing the `vaadin-context-menu-before-open` DOM event on the target runs
   Vaadin's real `beforeOpenHandler`, which calls `onBeforeOpenMenu()` (populates dynamic content)
   **and** `overlayAutoAddController.add()` (synchronously attaches the menu). **Chosen.**

Mechanism 1's neutral, side-effect-free accessor was instead proposed upstream to karibu-tools
([karibu-tools#16](https://github.com/mvysny/karibu-tools/issues/16)); karibu-testing only ever wants
to *drive* the menu, for which mechanism 2 is strictly better (fully-populated, tree-visible menu via
Vaadin's own code path).

**Decisions.**

1. **New target-based API built on mechanism 2.** `Component._openContextMenu(): ContextMenu` and
   `Grid._openContextMenu(item, column): GridContextMenu` fire the before-open event, then locate the
   now-attached menu by `getTarget() === this` (searched from `currentUI`, since the menu attaches as
   a UI sibling — *not* under the target; using the `Component`-receiver `_find` was the first-cut bug).
   `ContextMenuBase._close()` sets `opened=false` and fires the `closed` DOM event to detach.
   Convenience `Component._clickContextMenuItemWith{Caption,ID,Icon}` (+ `Grid` overloads with
   item/column) open → click → close in a `try/finally`.

2. **Auto-close in the convenience API.** Matches what a user does: open, click, menu closes itself.
   `_openContextMenu`/`_close` remain as the low-level pair for inspection/assertions.

3. **No menu → throw; multiple menus → throw (unsupported).** A component with no context menu (or one
   whose dynamic handler vetoes opening) fails clearly; multiple menus on one target is explicitly
   unsupported rather than guessing.

4. **Disabled targets still open (parity with the reference-based API).** The before-open listener is
   `ONLY_WHEN_ENABLED`, so `ElementListenerMap.fireEvent` drops it on a disabled element. Since the
   existing `_clickItemMatching`/`checkMenuItemEnabled` deliberately does *not* gate a ContextMenu on
   its target's enabled state, `fireContextMenuBeforeOpen` presents an enabled `event.source` (the UI
   element) when the target is disabled — the handler only reads `event.detail`, never the source.
   Invisible targets still fail, enforced by the existing item-visibility checks.

**Consequences.** `_find<ContextMenu>()` now works *while the menu is open* (it's a real UI child then),
and pretty-tree shows it. The reflection into Vaadin internals is avoided entirely; the only Vaadin
contract relied upon is the public `vaadin-context-menu-before-open` / `closed` DOM events and
`getTarget()`. Java parity via `LocatorJ._clickContextMenuItemWith*`.

**Where it lives.** `ContextMenu.kt`: `_openContextMenu`, `Grid._openContextMenu`, `_close`,
`_clickContextMenuItemWith{Caption,ID,Icon}` (+ grid), private `fireContextMenuBeforeOpen`;
`LocatorJ`; tests in `ContextMenuTest.AbstractContextMenuTests."open via target component"`.

---

## 2026-07-06 — Multiple browser tabs in one session: `MockBrowser`

**Context.** `MockVaadin.setup()` created exactly one `UI` (`createUI` was `internal`), so there was
no public way to have a **second tab** — a second `UI` sharing the same `VaadinSession` — nor to vary a
tab's `window.name`. Anything fundamentally *per-tab* was untestable. The downstream `vaadin-tab-scope`
library rests entirely on this: two tabs → two independent scopes, no cross-tab leakage, independent
lifecycles. Unparks and merges `ideas/multiple-uis-per-session.md` + `ideas/configurable-window-name.md`
(they turned out to be one feature).

**What real Flow does.** One `VaadinSession` backs many tabs; each has its own `UI` and its own
`window.name` (surfaced as `ExtendedClientDetails.getWindowName()`); `VaadinSession.getUIs()` returns
all of them. Tabs share session state but are otherwise independent. Closing a tab fires the unload
beacon that closes its `UI`; a lost beacon leaves it to the heartbeat reap.

**Decisions.**

1. **New `MockBrowser` object, not more methods on `MockVaadin`.** `MockVaadin` is the *server-side*
   test double (fabricates session/service/request); tab open/switch/close/reload are *client-side*
   browser actions. A dedicated `MockBrowser` façade keeps `MockVaadin` from becoming a god-object and
   gives both parked ideas (`window.name` + tabs) one coherent home. `MockBrowser` reaches server-side
   internals via small `internal` helpers on `MockVaadin` (`openNewTab`, `focusUI`, `discardUI`,
   `markUnloadBeaconLost`, `currentUiFactory`, `reloadCurrentUI`) — no public ThreadLocal leakage.

2. **Identify tabs by `window.name`, derive the mapping (no registry).** Tests key on the string, not
   `UI` objects (`newTab(name)`, `switchTo(name)`, `closeTab(name)`); `newTab` still *returns* the `UI`
   to act on. The name→UI mapping is **derived** on demand from `session.getUIs()` — no
   `Map<String,UI>` to drift across reload/close. Source of truth is a per-UI `window.name` stored as
   component data (`WINDOW_NAME_KEY`) eagerly at `createUI`, because the faked ECD is populated
   *lazily*; the faked ECD honors the same value. `currentWindowName` lets a test switch back to a tab
   without hard-coding names.

3. **`KaribuConfig.windowName` seeds tab #1 only.** The global knob sets the first tab's identity
   before `setup()` reads it (the one thing `MockBrowser` can't retro-set); `newTab` gives further tabs
   distinct **monotonic** (`ROOT-tab-N`) names — deterministic/reproducible, unlike Flow's random
   suffix — and `reload(newWindowName=…)` can change a tab's name on F5 (modelling Safari/typed-URL
   non-preservation). Rejected a `() -> String` factory as premature.

4. **`closeTab(name, beaconLost=false)` — a boolean, not the 3-value `UnloadBeaconTiming`.** A close
   creates no new UI, so the enum's `EAGER`/`LATE` (early/late *relative to the new UI*) have nothing
   to order against and collapse to one outcome; only delivered-vs-lost is real. A boolean states that
   honestly instead of offering two synonymous enum values. `beaconLost=true` reuses the exact
   `UNLOAD_BEACON_LOST_KEY` marking of a `NEVER` reload, so the same `reapInactiveUIs()` cleans both.

5. **Closing the current tab throws `IllegalArgumentException`, uniformly.** In a browser, closing the
   active tab moves focus to an arbitrary sibling — poison for a scripted test. Refuse rather than
   guess; `switchTo` another tab first. Throws even with `beaconLost=true` (the tab is gone from the
   browser's view regardless). Produce a lingering-UI-to-reap by closing a *background* tab with
   `beaconLost=true`. Relaxable later if a real need appears.

6. **`tearDown()` nukes every tab.** `closeCurrentUI` only handled the focused UI; teardown now also
   discards all background tabs (`discardBackgroundUIs`) so no UI (opened tab or lost-beacon lingerer)
   leaks into the next test.

7. **`userAgent` moved to `MockBrowser`.** Browser identity belongs on the browser; `MockVaadin.userAgent`
   remains as a `@Deprecated` alias delegating to `MockBrowser.userAgent` (source-compatible).

**Supersedes** decision #2 of the F5/beacon entry below ("give the reloaded UI a fresh `uiId`; the
eager path can reuse `uiId 1`"). With multiple tabs, reusing `uiId 1` on an eager reload evicts *another*
open tab (same `uiId` key), and `oldUI.uiId + 1` on late/never/preserve can collide with a sibling tab.
`createUI` now always assigns the **next free** `uiId` (`max(uIs.uiId) + 1`, or `1` for a fresh
session) — which still yields `uiId 1` for the single-tab eager case, so that entry's observable
single-tab behavior is unchanged.

**Consequences / limitations.** No wall-clock heartbeat timing (inherited from the beacon/reap design);
`MockBrowser.reload()` skips the client-side `Page.reload()` JS command that the `page.reload()` path
issues (irrelevant to a browserless test). Java sees `MockBrowser.newTab()` etc. via `@JvmStatic` /
`@JvmOverloads`.

**Where it lives.** `MockBrowser` (all mechanics in its KDoc); `KaribuConfig.windowName`; `MockVaadin`
internal helpers (`openNewTab`, `focusUI`, `discardUI`, `markUnloadBeaconLost`, `currentUiFactory`,
`discardBackgroundUIs`) and the per-UI `windowName` plumbing in `createUI`/`MockPage`; test matrix in
`MockBrowserTest`. Superseded idea files: `ideas/multiple-uis-per-session.md`,
`ideas/configurable-window-name.md` (deleted on implementation).

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
