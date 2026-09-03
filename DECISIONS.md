# Technical decisions

A living record of the design decisions behind Karibu-Testing — especially the *roads not taken*.
It exists because the other three doc homes (see [`CLAUDE.md`](CLAUDE.md)) each refuse that one
fact class on their own stated terms: a `README.md` is user-facing prose about how to *use* the
library; KDoc is the authoritative "how it works now", read by someone standing at the API who
gains nothing from an argument against a design that never shipped; and `ideas/` is
forward-looking and is *deleted* once the idea ships.

It is the *why-we-chose* record. It is not the *how-it-works* reference (KDoc), the *how-to-use*
book (`README.md`), the *what-changed* list (`CHANGELOG.md`), or the not-yet-decided scratchpad
(`ideas/`). An entry links out rather than restating.

**Format.** One entry per decision. The ID is a slug, not a number and not a date: `D_` plus a
1–4-word snake_case hint at the subject (`D_mock_browser_tabs`), so a citation carries meaning on
its own and survives insertion and reordering. The `(date)` on the heading is *decided*
provenance, not a log position — git owns the edit history, so don't narrate how an entry used to
read. Keep each entry tight: context, the decision, the alternatives rejected and why, and the
consequences a future contributor would trip over. A decision is worth logging the moment it's
*made*; implementation can lag (the `Status:` line says which).

**No entry without a real fork.** If nothing was seriously considered and rejected, it isn't a
decision — it's how the thing works, and that's KDoc. This is the guard against a diary.

**Version-pin the evidence.** A claim about Flow's internals is only true of the versions it was
traced against, and this project runs two (`stable` and `next` — see `CLAUDE.md`). Say which.

**Newest first.** Entries run reverse-chronologically by decided date, so a scan hits recent
decisions first.

**Never read wholesale.** `grep '^## D_' DECISIONS.md` is the index; there is no ToC. Cite an
entry by slug — never by position ("the entry below") and never by date, both of which rot on the
next edit. Grep tripwire: every `D_<slug>` referenced anywhere in the repo must exist as a
`^## D_` heading here.

**Entries are mutable — edit in place, don't append addendums.** Each entry is the single coherent
home for one *live* decision; keep it current as the decision is refined or extended. Two things
this does *not* license:

- **The roads not taken stay.** "We chose X, rejected Y because Z" is live content of the current
  decision, not stale history — never edit it away. It's the most valuable thing in the file.
- **A reversed *shipped* decision forks a tombstone, it is not overwritten.** When a design was
  released and then thrown away, leave the old entry as the scar, set its `Status:` to
  **Superseded by `D_<slug>`**, and write the replacement fresh. The line: *refined or extended,
  or reversed before it ever shipped* → edit in place; *reversed after a release* → tombstone +
  new entry.

---

## D_service_event_bus_compat — Reach `VaadinService`'s listeners through whichever internal holds them, and invoke them by hand (2026-09-03)

**Status:** Accepted; implemented 2026-09-03 in `MockVaadin.kt`
(`fireSessionInitListeners`/`fireServiceDestroyListeners`), mechanics in their KDoc.

**Context.** Karibu has to fire `SessionInitEvent` and `ServiceDestroyEvent` itself: `MockVaadin`
fabricates the session rather than letting a servlet container do it, and `tearDown()` must not call
`VaadinService.destroy()` (that also shuts the service's executor down, killing reuse across tests).
Neither event has a public "fire" entry point, so Karibu reached into `VaadinService`'s private
`sessionInitListeners` / `serviceDestroyListeners` collections by reflection.

**What changed (traced flow-server 25.2.6 vs. 25.3.0-beta1).** 25.3 deletes both fields. Every
service-level listener now lives in one `VaadinServiceEventBus eventBus`, a
`Map<Class<? extends EventObject>, CopyOnWriteArrayList<SerializableConsumer<?>>>`;
`addSessionInitListener`/`addServiceDestroyListener` wrap the listener into a consumer keyed by the
*event* class. `VaadinService.getEventBus()`, `VaadinServiceEventBus.getListeners(Class)` and
`fireEvent(EventObject)` are all public. Symptom before the fix: `NoSuchFieldException:
sessionInitListeners` out of `MockVaadin.setup()` — i.e. **every** test in the `next` battery.

**Decisions.**

1. **Probe for `getEventBus()` at runtime; keep the field reflection as the 25.2 branch.** Karibu
   compiles against `stable` (25.2) and runs on both, so the new path has to be reflective too — a
   cached `Method?` that is `null` on 25.2. Rejected: bumping the compile baseline to 25.3 to call
   `getEventBus()` directly. 25.3 is a beta; that would make the released Karibu unusable on the
   Vaadin version most users are actually on, to save one `getMethod` call made once per JVM.

2. **Call the listeners directly via `getListeners(eventClass)`, not `eventBus.fireEvent(event)`.**
   `fireEvent(EventObject)` routes every listener through the bus's `LOG_ERRORS` handler, which
   swallows a listener exception into a log line. `tearDown()` documents the opposite — that
   exceptions thrown by session/service destroy listeners propagate to the test — and Karibu's whole
   value is surfacing those. `getListeners` returns the consumers in registration order, so invoking
   them in a loop reproduces the old field-iteration behaviour, exceptions and all — and is in fact
   better-defined than the 25.2 branch, whose `serviceDestroyListeners` is an unordered `Set`. (The
   overload `fireEvent(E, SerializableBiConsumer)` could rethrow, but only by collecting into an
   `AtomicReference` the way `VaadinService.destroy()` does — more machinery for the same result,
   and it wraps in `RuntimeException`.)

**Consequences.** The two `fireXListeners` helpers are the only place that knows about the split;
adding a third service-level event means adding a branch there, not a new mechanism. If Karibu ever
drops Vaadin 25.2, the legacy branch and both `Field` lazies delete cleanly.

---

## D_treegrid_java_walk — Java reaches the lazy TreeGrid walk through `Stream`, and there is no eager `filter` twin (2026-08-28)

**Status:** Accepted; shipped in 2.7.3. `TreeGrid._rowStream()`, `@JvmOverloads` on
`TreeGrid._rowSequence()`, `Grid._dump(from, toInclusive)` — all in `Grid.kt`, mechanics in their
KDoc; README "Java" section; tests in `TreeGridTest`/`GridTest`.

**Context.** [#214](https://github.com/mvysny/karibu-testing/issues/214), raised while porting a
~180-file test corpus from Kotlin to Java. Of 23 Karibu functions the corpus used, `_rowSequence()`
was the only one with no idiomatic Java form: `kotlin.sequences.Sequence<T>` forces a Java caller
through `SequencesKt.toList(...)`, a stdlib facade nobody would think to reach for. The issue
proposed a `List`-returning `TreeGrid._findAllVisibleRows(filter)` plus a lazy `_rowIterable(filter)`.

**Decisions.**

1. **No `_findAllVisibleRows()` — `Grid._findAll()` already is it.** `_findAll()` is
   `_fetch(0, _saneFetchLimit)`, and `_fetch` special-cases `TreeGrid` to
   `_rowSequence().drop(offset).take(limit).toList()`; `_saneFetchLimit` is `Int.MAX_VALUE / 1000`
   (≈2.1M), i.e. unbounded in practice. So on a `TreeGrid`, `_findAll()` *is*
   `_rowSequence().toList()`, returns `List<T>`, and from Java is `GridKt._findAll(tree)` — no
   `SequencesKt`, no explicit `null`. All eleven call sites in the reporting corpus were
   `_rowSequence().toList()` with no filter, so they were already served. Adding
   `_findAllVisibleRows` would have been a third name for one operation, and the worse of the two
   names: `_findAll` on a `TreeGrid` *already* means "visible rows", as its KDoc says. The gap was
   discoverability, not capability, so it was closed with a KDoc cross-reference on `_rowSequence`
   and a README paragraph rather than with API.

2. **The Java shape of the lazy walk is `TreeGrid._rowStream(filter): Stream<T>`.** Kotlin callers
   keep `_rowSequence()`; `_rowStream()` exists for Java. We do not ship two lazy Java shapes —
   that would be the same "third name for one operation" `_findAllVisibleRows()` was declined for.
   Three reasons for `Stream`, in increasing weight:
   - The caveat stops being a caveat. `Sequence.asIterable()` delegates rather than buffering, so
     an `Iterable` twin would be single-pass — precisely what `Iterable` promises not to be, hence
     the KDoc warning it needed. `Stream` is *specified* as single-consumption and throws
     `IllegalStateException` on reuse: same semantics, enforced by the type instead of documented
     around it.
   - Java's `Iterable` has no `toList()`, so an `Iterable` caller either hand-rolls a loop into an
     `ArrayList` or reaches for `StreamSupport` — meaning it would not even serve the reporter's
     eleven call sites. `Stream` has `toList()`, `filter()`, `limit()` and short-circuiting, so the
     lazy walk, the eager walk and the stop-early walk are one door.
   - It costs no new surface: `Grid.kt` already imports `java.util.stream.Stream` and
     `kotlin.streams.toList`, `asStream()` is the same stdlib package, and `jvmTarget` is 21
     (`build.gradle.kts`), so `Stream.toList()` (Java 16+) is available to every caller.

   **Rejected: `_rowIterable(filter): Iterable<T>`** — the issue's own proposal, and briefly
   committed before 2.7.3 shipped (nothing depended on it, so it was replaced rather than
   tombstoned). It loses on all three counts above. Also rejected: buffering to make the
   `Iterable` genuinely multi-pass, since that silently reintroduces the full expensive walk that
   `_rowSequence()`'s own doc warning exists to prevent.

3. **No eager `filter`-taking walk — and *not* because `_findAll().filter { }` is the same thing.**
   It is not the same operation, and an earlier version of this entry wrongly claimed it was.
   `_rowSequence`'s filter goes into the `HierarchicalQuery` used at every level (`getChildrenOf`
   is `checkedFetch(HierarchicalQuery(filter, item))`), so what it means is the data provider's
   business, never a predicate over the flattened list. Measured, not assumed — a first draft
   claimed the filter simply prunes rejected subtrees, and the test written to pin that claim
   failed: on a `0 -> 1 -> … -> 9` chain backed by Vaadin's `TreeDataProvider`,
   `_rowStream { it == 9 }` returns `[0..9]` while `_findAll().filter { it == 9 }` returns `[9]`,
   because `TreeDataProvider` keeps an item when *it or any descendant* matches, so the ancestors
   of a match survive. A back-end provider applying the predicate strictly per level does prune the
   subtree instead. Both are unreachable from `_findAll()`. The decline stands on the *correct*
   rationale: with `_rowStream` the filtered eager walk is `_rowStream(tree, f).toList()` in Java
   and `_rowSequence(f).toList()` in Kotlin, so a dedicated eager overload adds a spelling, not a
   capability. The per-level semantics are documented on `_rowSequence`, on `_rowStream` and in the
   README, and pinned by the test above.

4. **`@JvmOverloads` on `TreeGrid._rowSequence()`** — the `HierarchicalDataProvider` twin already
   had it; its absence on the `TreeGrid` one was an oversight, and forced Java callers to pass the
   `null` filter explicitly.

5. **`Grid._dump(from, toInclusive)` exists, and its bounds stay inclusive.**
   `kotlin.ranges.IntRange` does have a Java-reachable constructor, so the range overload was
   reachable but in the same nobody-would-write-that class as `SequencesKt`; the `(Int, Int)` form
   delegates to it. Pushback argued for an *exclusive* end, since the overload exists for Java and
   Java is exclusive-end nearly everywhere a reader has been trained (`subList`, `substring`,
   `copyOfRange`, `IntStream.range`), and the parameter name isn't visible at the call site.
   Declined: the two overloads share a name, and the worlds are not disjoint — Kotlin can call the
   `(Int, Int)` form too, so `grid._dump(0, 9)` and `grid._dump(0..9)` printing different row
   counts is a worse trap than the convention mismatch, and it is a trap for the *maintainer* as
   well as the caller. Mitigations: the parameter is named `toInclusive` (IntelliJ shows inline
   parameter hints for literal arguments), the KDoc says so, and the stakes are a debug string,
   never an assertion. If the exclusive reading ever wins, the way to do it is a distinct name, not
   a differently-behaving overload of `_dump`.

**Not done (also in the issue, judged not worth the API).** A `Consumer`-typed adder for
`KaribuConfig.pendingJavascriptInvocationHandlers`: the `return Unit.INSTANCE;` a Java lambda needs
for `Function1<_, Unit>` is a real annoyance, but the field is a `MutableList` `var`, so an
`addX(Consumer)` helper buys asymmetry with `remove`/`clear` for one niche hook.

**Evidence.** Java-callability of every new and claimed-existing form (`_rowStream(tree)`,
`_rowStream(tree, filter)`, `.limit(3).toList()`, `_rowSequence(tree)`, `_findAll(tree)`,
`_dump(grid, 0, 6)`) verified by compiling a Java caller against the built classes, not inferred
from `@JvmOverloads`. Laziness pinned by a test counting `TreeDataProvider.fetchChildren` calls for
`limit(3)` versus a full walk. Full battery green on all four `testrun-*` environments.

---

## D_unload_beacon_via_rpc_handler — Deliver the F5/tab-close unload beacon through Flow's *real* `ServerRpcHandler` (2026-07-21)

**Status:** Accepted; implemented 2026-07-21. Partly supersedes `D_f5_beacon_timing` (its decision
1, for the non-preserve path).

**Context.** [#210](https://github.com/mvysny/karibu-testing/issues/210): the `D_f5_beacon_timing`
design *reimplemented* the browser unload beacon in Kotlin — Karibu decided "close the old UI"
itself and mirrored `ServerRpcHandler.isPreserveOnRefreshTarget`. Two costs: (a) a custom
`ServerRpcHandler` (e.g. a tab-scope add-on hooking `handleUnloadBeaconRequest`) was **invisible** to
Karibu tests — the beacon never reached it; (b) `MockBrowser.closeTab` force-detached a
`@PreserveOnRefresh` UI, whereas real Flow *ignores* the beacon for such a UI and leaves it lingering
until the heartbeat reap. The proposal: route beacon simulation through the app's actual handler.

**What real Flow does (traced against flow-server 25.2.4 & 25.3.0-alpha3).** A closing tab POSTs an
unload beacon; `UidlRequestHandler.getRpcHandler().handleRpc(ui, body, request)` runs it. Beacon
detection is just `json.has(ApplicationConstants.UNLOAD_BEACON)` (`"UNLOAD"`); the marker makes
`RpcRequest` skip the sync-id and client-id checks. `handleUnloadBeaconRequest` (extracted into a
`protected` method in 25.2; inline in `handleRpc` before that) closes a normal UI but **ignores the
beacon for a `@PreserveOnRefresh` target**. The app customizes the handler by overriding
`UidlRequestHandler.createRpcHandler()` (installed via `VaadinService.createRequestHandlers()`).

**Decisions.**

1. **Route through the *public* `handleRpc(UI, String, VaadinRequest)`, not the protected
   `handleUnloadBeaconRequest`.** `deliverUnloadBeacon(ui)` builds a minimal beacon **string** —
   `{"csrfToken":<ui token>,"rpcInvocations":[],"UNLOAD":0}` — and lets Flow parse it. Consequences:
   Karibu never touches Flow's version-specific internal JSON types (it only produces a String); the
   empty `rpcInvocations` keeps `handleInvocations()` a no-op (a `null` array NPEs — it is
   dereferenced immediately); the CSRF token matches the UI's so `isCsrfTokenValid` passes whatever
   the XSRF config; and it works on every supported version (public `handleRpc` is ancient), unlike
   `handleUnloadBeaconRequest` which is 25.2+.

2. **Reach the app's *custom* handler via `UidlRequestHandler.createRpcHandler()`, reflectively.**
   `deliverUnloadBeacon` pulls the `UidlRequestHandler` from `service.getRequestHandlers()` and
   invokes its `protected createRpcHandler()` by reflection (one reflective call; consistent with
   Karibu's existing reflection style). This is what makes a custom `ServerRpcHandler` observable —
   the whole point of the issue. Rejected: the fully-public `synchronizedHandleRequest(session,
   request, response, body)` — it re-derives the UI from a `v-uiId` request param and then
   `writeUidl()`s a response, both pointless (and fragile) noise for a close simulation.

3. **Split close (Flow's) from finalize (Karibu's).** `handleRpc` only sets `UI.isClosing`; Flow's
   end-of-request `removeClosedUIs()` does the detach + `session.removeUI`. Karibu mirrors that split:
   `deliverUnloadBeacon` sets `isClosing` (or not, for preserve), then `finalizeClosedUI(ui)` removes
   it **iff** `isClosing`. The `EAGER`/`LATE`/`NEVER` knob is now *when Karibu delivers the beacon and
   finalizes* relative to new-UI creation — not *what the beacon does*. `NEVER` = don't deliver.

4. **`closeTab` lets Flow decide the tab's fate.** Beacon delivered → normal UI removed immediately;
   `@PreserveOnRefresh` UI (beacon ignored → `isClosing` stays false) is flagged
   `UNLOAD_BEACON_LOST_KEY` and left lingering for `reapInactiveUIs`, exactly as a real browser leaves
   it for the heartbeat. **Behavioral change** vs. the prior force-detach.

5. **Kept `isPreserveOnRefreshTarget` (did *not* fully delete the mirror).** For a preserve target the
   close is *navigation-driven* (the new UI's `disconnectElements()` teleports overlays off the old UI
   and closes it), not beacon-driven — the beacon is a no-op there. So the reload path still needs
   preserve-awareness to order new-UI-creation vs. beacon delivery (enforced by
   `@PreserveOnRefresh ignores {EAGER,LATE,NEVER} timing` tests). The "mirror" is a 3-line annotation
   read on the public `activeRouterTargetsChain`, negligible drift risk. Partial win #3; accepted.

**Consequences / limitations.** A custom `ServerRpcHandler` now sees the beacon (new tests:
`unload beacon reaches a custom ServerRpcHandler (issue 210)`), and closing a `@PreserveOnRefresh`
tab now lingers-then-reaps instead of detaching (new test in `MockBrowserTest`). `createRpcHandler()`
is invoked per delivery, yielding a fresh handler instance each time (Flow caches one via
`getRpcHandler()`); fine for the stateless norm, revisit if a stateful custom handler needs it. The
`closeTab`-preserve behavioral change may warrant a major-version note in the changelog.

Against `D_f5_beacon_timing`: for the non-preserve path the old UI is no longer closed by Karibu
directly but by Flow's real handler. The preserve path (nav-driven teleport + close) is unchanged.

**Where it lives.** `MockVaadin.deliverUnloadBeacon()` / `obtainServerRpcHandler()` /
`finalizeClosedUI()` / `reloadCurrentUI()` / `discardUI()`; `MockBrowser.closeTab` KDoc. Tests:
`MockVaadinTest` (`unload beacon reaches a custom ServerRpcHandler (issue 210)`, plus the unchanged
`unload beacon timing on F5` matrix) and `MockBrowserTest` (`closing a @PreserveOnRefresh tab leaves
it lingering until reapInactiveUIs`).

---

## D_stable_next_build_tokens — Rename stale `v24`/`kt10` build tokens to `stable`/`next` + `testrun-*` (2026-07-07)

**Status:** Accepted; implemented 2026-07-07. The module-naming rule it establishes is restated in
`CLAUDE.md`.

**Context.** The Gradle Vaadin-dependency aliases were named `vaadin-v24-*` / `vaadin-v24next-*`
but actually pointed at Vaadin **25.2.1** and **25.3.0-alpha3** (`libs.versions.toml`). The "v24"
read as "Vaadin 24" and repeatedly misled readers (human and agent) about which version the build
compiles/tests against. The internal test modules carried the same rot: a `kt10-` prefix (Vaadin
**10** — ice age, and redundant with the parent module path) and `vaadin24` in the runner names.

**Decisions.**

1. **Catalog aliases → `stable` / `next`**, mirroring the existing `vaadin` / `vaadin_next` version
   keys: `vaadin-v24-*` → `vaadin-stable-*`, `vaadin-v24next-*` → `vaadin-next-*`. Chose `stable`
   over `latest` (the pinned one isn't necessarily the newest release) and `next` over `prerelease`
   (`next` stays correct if `vaadin_next` later tracks a released newer minor rather than an alpha).

2. **Internal (non-published) test modules renamed**, dropping `kt10`/`kt23`/`vaadin24`:
   - `karibu-testing-v10:kt10-tests` → `karibu-testing-v10:tests`
   - `karibu-testing-v23:kt23-tests` → `karibu-testing-v23:tests`
   - `kt10-testrun-vaadin24{,-module}` → `testrun-stable-{webapp,module}`
   - `kt10-testrun-vaadin24next{,-module}` → `testrun-next-{webapp,module}`
   Runner names now encode the two real axes: `{stable|next}` Vaadin version × `{webapp|module}`
   packaging (webapp = WAR-style with `flow-build-info.json`; module = jar-reusable-component
   without it). Published coordinates (`karibu-testing-v10/-v23/-v24/...`) were **not** touched.

3. **Distinct `group` on the two `:tests` libs** (`…kaributesting.v10` / `…kaributesting.v23`).
   Giving both the leaf name `tests` made them share the module identity
   `com.github.mvysny.kaributesting:tests`; since `v23:tests` depends on `v10:tests`, Gradle's
   conflict resolution substituted one for the other (`v10:tests -> v23:tests`), producing a
   self-referential **circular task dependency** (`compileKotlin → jar → compileKotlin`). Project
   module identity is `group` + project *name* (the leaf dir) — **not** `archivesName` (an
   `archivesName` override was tried first and did *not* break the cycle). Differentiating `group`
   was chosen over reintroducing distinct leaf names so the clean `:tests` path could be kept; the
   libs are never published, so their group is cosmetic.

**Module-naming rule (also recorded in CLAUDE.md).** The `vNN` suffix on a *published*
`karibu-testing-vNN` module marks the highest Vaadin version whose version-specific APIs it supports
— not the version it runs against. `karibu-testing-v24` supports APIs up to Vaadin 24, runs fine on
Vaadin 25, and is a frozen published coordinate; a Vaadin-25-specific API would get a new
`karibu-testing-v25` module.

---

## D_login_helper_no_oauth — Browser-free `MockVaadin.login()`/`logout()`, and no OAuth redirect emulation (2026-07-07)

**Status:** Accepted; implemented 2026-07-07. Builds on `D_spring_security_bridge`.

**Context.** [Issue #143](https://github.com/mvysny/karibu-testing/issues/143): a Keycloak-OAuth app
with all views `@PermitAll` hit a `NotFoundException` from `MockVaadin.setup()` navigating to `""`,
and the reporter "fixed" it by relaxing the root view to `@AnonymousAllowed` — defeating the security
model under test. The issue predates two things that already changed the picture: (a) setup's initial
navigation is now guarded by `initDefaultRoute && registry.getNavigationTarget("").isPresent`
(`MockVaadin.kt`), so an app with no `""` route no longer crashes at setup; (b) the
`MockSpringSecurity.mock()` bridge (`D_spring_security_bridge`). What remained was ergonomics:
everyone hand-copied the same two-line `userPrincipalInt` + `isUserInRole` snippet from the README.

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

## D_spring_security_bridge — Bridge `SecurityContextHolder` onto the request, opt-in (2026-07-07)

**Status:** Accepted; implemented 2026-07-07 as `MockSpringSecurity.mock()` in
`karibu-testing-v10-spring`.

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
   `testrun-*` battery and its `src/test` had only a compile smoke test. Since `mock()` only
   touches `mockRequestFactory` + `FakeRequest` + `SecurityContextHolder`, a plain `MockVaadin.setup()`
   test suffices; the test populates `SecurityContextHolder` directly (exactly what `@WithMockUser`
   does under the hood), avoiding a full `SpringExtension`/`spring-security-test` context.

**Known caveat (accepted).** `MockVaadin.mockRequestFactory` is a global that `tearDown()` does not
reset (same class of issue as `ideas/karibuconfig-reset.md`) — hence the "call from `@BeforeEach`"
guidance. The leak is harmless: a later non-security test runs with an empty `SecurityContextHolder`,
so `getUserPrincipal()` returns `null`, identical to the default `FakeRequest`.

**Where it lives.** `karibu-testing-v10-spring`: `MockSpringSecurity.kt` (mechanics in its KDoc);
`compileOnly`/`testImplementation` `spring-security-core` in `build.gradle.kts`; tests in
`MockSpringSecurityTest`. README "Spring Security" section rewritten, replacing the principal-only
snippet previously documented there.

---

## D_litrenderer_jsoup — Expose `LitRenderer` markup as a parsed JSoup tree, not a raw string (2026-07-07)

**Status:** Accepted; implemented 2026-07-07 as `_getPresentationHtml()` / `_getPresentationJsoup()`
in `Renderers.kt` + `Grid.kt`.

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

## D_context_menu_via_target — Open a `ContextMenu` by firing Vaadin's own before-open event on the target (2026-07-07)

**Status:** Accepted; implemented 2026-07-07 in `ContextMenu.kt` + `LocatorJ`.

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

## D_mock_browser_tabs — Multiple browser tabs in one session live on a `MockBrowser` façade, keyed by `window.name` (2026-07-06)

**Status:** Accepted; implemented 2026-07-06. Supersedes decision 2 of `D_f5_beacon_timing` (the
`uiId` reuse). Graduated from `ideas/multiple-uis-per-session.md` + `ideas/configurable-window-name.md`
(both deleted).

**Context.** `MockVaadin.setup()` created exactly one `UI` (`createUI` was `internal`), so there was
no public way to have a **second tab** — a second `UI` sharing the same `VaadinSession` — nor to vary a
tab's `window.name`. Anything fundamentally *per-tab* was untestable. The downstream `vaadin-tab-scope`
library rests entirely on this: two tabs → two independent scopes, no cross-tab leakage, independent
lifecycles. The two parked ideas turned out to be one feature.

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

**Against `D_f5_beacon_timing` decision 2** ("give the reloaded UI a fresh `uiId`; the eager path can
reuse `uiId 1`"). With multiple tabs, reusing `uiId 1` on an eager reload evicts *another* open tab
(same `uiId` key), and `oldUI.uiId + 1` on late/never/preserve can collide with a sibling tab.
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
`MockBrowserTest`.

---

## D_reap_inactive_uis — Reap a lost-beacon UI by flag, emulating Flow's outcome and not its clock (2026-07-06)

**Status:** Accepted; implemented 2026-07-06 as `MockVaadin.reapInactiveUIs()`. Graduated from
`ideas/heartbeat-emulation.md` (deleted).

**Context.** `D_f5_beacon_timing` shipped `UnloadBeaconTiming.NEVER` = "the unload
beacon was lost, so the old UI lingers alive alongside the new one," but deliberately did *not* model
the heartbeat/idle-UI reap that would eventually close it in production. A downstream Vaadin tab-scope
library needs exactly that follow-up: assert that a UI abandoned by a lost beacon eventually gets
closed and detached (so its per-UI resources are released).

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
`MockVaadinTest` (`unload beacon timing on F5` → the three `reapInactiveUIs …` cases).

---

## D_f5_beacon_timing — F5 reload lifecycle: let Flow teleport the overlays, and make beacon timing a knob (2026-07-06)

**Status:** Accepted and shipped in 2.7.1; **partly superseded** — decision 1 by
`D_unload_beacon_via_rpc_handler` (the non-preserve path now runs through Flow's real
`ServerRpcHandler`), decision 2 by `D_mock_browser_tabs` (`uiId` is now always the next free one).
Decisions 3 and 4 stand. Graduated from `ideas/beacon-reload-timing.md` (deleted).

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
   `moveElementsFrom`/ordering in Karibu — it would drift from Flow. (For the *non-preserve* path this
   didn't go far enough — Karibu still closed the UI itself; see `D_unload_beacon_via_rpc_handler`.)

2. **Give the reloaded UI a fresh `uiId`.** `uiId` is the key in `VaadinSession.uIs`; reusing `1` made
   `session.addUI(newUI)` evict the still-live old UI, collapsing the transient two-live-UI window.
   The eager path can still reuse `uiId 1` because the old UI is removed *before* `addUI`. (That last
   sentence held only while there was one tab; see `D_mock_browser_tabs`.)

3. **Make non-preserve beacon timing configurable; default EAGER.** `KaribuConfig.unloadBeaconTiming`
   = `UnloadBeaconTiming { EAGER, LATE, NEVER }`.
   - **EAGER (default)**: close+detach+remove old, then create new. Chosen as default because it is
     both the common production ordering *and* the pre-2.7.1 Karibu behavior, and it's the
     detach-before-attach case the tab-scope library must test.
   - **LATE**: create new, then close+detach+remove old.
   - **NEVER**: old UI lingers alongside the new one (beacon lost). We deliberately do **not** model
     the heartbeat reap that would eventually close it (no time axis; `D_reap_inactive_uis` later
     added a caller-driven reap, still with no clock).
   Ignored for `@PreserveOnRefresh` (Flow ignores the beacon there).

4. **One flag, no new public primitives.** Rejected an explicit `closeUIViaBeacon()` /
   `expireInactiveUIs()` API: there is no browser, so Karibu fires the simulated beacon *inside*
   `reloadCurrentUI()` at the configured point; a caller-invoked primitive would be redundant. A
   heartbeat-reap driver was punted to a separate idea rather than built speculatively (it became
   `D_reap_inactive_uis`).

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
(`page reload F5 lifecycle`, `unload beacon timing on F5`).
