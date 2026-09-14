# Decisions

Why Karibu-Testing is the way it is and not otherwise — FAQ-shaped: each entry is a question and
its current answer. Rewrite the answer when it changes; delete the entry when nobody asks any more.
An entry is earned by what it would cost to reverse — a published API, a lifecycle other libraries
build on — or by research the next person would otherwise redo, cited as its `R_`. Not an entry:
which testing library, the CI host, a version bump, what a helper is called, a rename that broke
nothing — a comment at the site of the choice, or nothing; nothing about `design/` itself. An entry
is the roads not taken and what they cost; what the thing *does* is its KDoc, so open that and
delete anything this says twice. Say which flow-server version a claim about Flow's internals was
traced against. Cite by slug, never by position; `grep '^## D_' design/decisions.md` is the index.
The first entry is the ruler: every later one trims to its length. When you have written an entry,
re-read it against the one above and cut what is left over.

---

## D_f5_beacon_timing — Why does an F5 reload hand the overlay teleport back to Flow, and expose beacon timing as a knob?

`MockPage.reload()` closed and detached the old UI before creating the new one, which silently
dropped any open `Dialog` or `Notification` when a `@PreserveOnRefresh` view was refreshed
([#207](https://github.com/mvysny/karibu-testing/issues/207)). Karibu already drives Flow's real
navigation pipeline, so the fix was to reorder — keep the old UI alive until the new one has
navigated — and let Flow's `disconnectElements()` do the work. Why not reimplement the teleport and
the child ordering in Karibu: it is internal behaviour that shifts between Flow minors, so the copy
would drift while the tests went on passing, which is the one failure a test double must not have.

The non-preserve path closes the old UI by beacon, and where that lands relative to new-UI creation
is best-effort in a real browser — so a mid-reload `UIInitListener` or detach listener can observe
orderings that all end in the same terminal state. `KaribuConfig.unloadBeaconTiming` makes that
axis explicit rather than picking one and calling it the truth. `EAGER` is the default because it
was also the pre-2.7.1 behaviour, so no existing test silently changed meaning. Why not an explicit
`closeUIViaBeacon()` primitive instead of a flag: with no browser, Karibu has to fire the beacon
from inside `reloadCurrentUI()` anyway, so the primitive would be a second public route to a line
that has already run by the time a caller could reach it.

What the knob does not have is a clock. "The old UI survives N heartbeats, then dies" is not
expressible, and that assertion tests Flow's timeout machinery rather than app code
(`D_reap_inactive_uis`). How the beacon reaches Flow is `D_unload_beacon_via_rpc_handler`; what
`uiId` the reloaded UI gets is `D_mock_browser_tabs`.

## D_unload_beacon_via_rpc_handler — Why is the unload beacon delivered through Flow's real `ServerRpcHandler` rather than simulated in Kotlin?

The first cut reimplemented the beacon: Karibu decided "close the old UI" itself and mirrored
`ServerRpcHandler.isPreserveOnRefreshTarget`. That cost two things
([#210](https://github.com/mvysny/karibu-testing/issues/210)). An app with a custom
`ServerRpcHandler` — a tab-scope add-on hooking `handleUnloadBeaconRequest`, say — was invisible to
its own tests, because the beacon never reached it. And `closeTab` force-detached a
`@PreserveOnRefresh` UI, where real Flow ignores the beacon and leaves the UI lingering for the reap
(`R_flow_unload_beacon`). Karibu now hands a hand-built beacon *string* to the public
`handleRpc(UI, String, VaadinRequest)`, reaching the app's own handler through a reflective
`createRpcHandler()`.

A string, rather than Flow's own JSON types, because those types are internal and differ across the
two Vaadin lines Karibu runs on; letting Flow parse its own input keeps that surface at zero. Why
not `handleUnloadBeaconRequest` directly: it is `protected`, so reaching it costs a reflective call
of its own, and it has only been a method since 25.2, where the public `handleRpc` is ancient and
unchanged. Why not the fully public `synchronizedHandleRequest`: it re-derives the UI from a
`v-uiId` request parameter and writes a UIDL response, so a close simulation would have to fabricate
both — two more Flow-shaped things to keep matching Flow, for no observable difference.

Karibu also mirrors Flow's close/remove split rather than collapsing it, which is what lets
`closeTab` leave a preserve-target tab lingering exactly as a real browser does. That is a
behavioural change from the force-detach, and worth a changelog note. `isPreserveOnRefreshTarget`
survives the rewrite: for a preserve target the close is navigation-driven, not beacon-driven, so
the reload path still needs preserve-awareness to order new-UI creation against beacon delivery. It
reads the public `activeRouterTargetsChain`, so the drift risk is a line, not a mechanism.

## D_reap_inactive_uis — Why is a lost-beacon UI reaped by a flag rather than by emulating Flow's heartbeat clock?

`UnloadBeaconTiming.NEVER` leaves an old UI lingering, and a downstream tab-scope library needs to
assert that such a UI is eventually closed and detached so its per-UI resources are released. Flow
gets there on elapsed time (`R_flow_ui_reap`); `reapInactiveUIs()` reproduces the outcome on demand.

Why not a virtual, advanceable clock — stamped heartbeat timestamps and a faked `HeartbeatHandler`:
it is a large piece of machinery pushing against a core that is deliberately synchronous and
clock-less, and the only assertion it buys ("not reaped before N intervals, reaped after") tests
Flow's timeout machinery rather than app code. Why not a generic "close every non-current,
non-closing UI" reaper: it picks its victims by an incidental property instead of the meaningful one,
so it would also close the legitimate extra UIs of a multi-tab test (`D_mock_browser_tabs`), and its
name would overclaim, since nothing in the body counts a heartbeat. Reaping by an explicit
beacon-lost flag says what actually happened, and stays correct as more ways to hold a UI appear.

The scope limit is inherent rather than provisional: a lost beacon is the only one of Flow's reap
causes a browserless double can produce at all — a frozen tab, a sleeping laptop and a throttled
background tab cannot even be expressed here.

## D_mock_browser_tabs — Why do multiple browser tabs live on a `MockBrowser` façade keyed by `window.name`?

`MockVaadin.setup()` created exactly one `UI` and `createUI` was `internal`, so a second tab was
unreachable, and with it anything per-tab; the downstream `vaadin-tab-scope` library rests entirely
on having two. Opening, switching, closing and reloading tabs are things a browser does *to* the
server, so they went onto a new object rather than growing `MockVaadin`, which was already the
session/service/request fabricator and would have become a god-object. `MockBrowser` reaches what it
needs through `internal` helpers, so no ThreadLocal leaks into the public API.

Tabs are keyed by `window.name` — the browser's own primitive for tab identity, and what a tab-scope
library keys on — and the name→UI mapping is *derived* from `session.getUIs()` on each call. Why not
a `Map<String, UI>` registry: it is a second source of truth to maintain across reload, close and
reap, and it drifts on the first path that forgets it, where deriving cannot. A `() -> String`
factory for generated names was declined as machinery for no asked-for case.

Two edges are deliberate. `closeTab` takes a `beaconLost` boolean rather than the three-valued
`UnloadBeaconTiming`, because a close creates no new UI, so `EAGER` and `LATE` — which order the
close *against* new-UI creation — have nothing to order against and collapse into one outcome; a
boolean says that instead of offering two spellings of one behaviour. And closing the *current* tab
throws rather than guessing, because a real browser moves focus to an arbitrary sibling, which is
poison for a scripted test. To get a lingering UI to reap, close a background tab with
`beaconLost=true`.

Tabs also forced `uiId` allocation to be honest: the id is the key in `VaadinSession.uIs`, so any
fixed or old-id-plus-one scheme eventually evicts a still-live sibling tab.

## D_service_event_bus_compat — Why are `VaadinService`'s listeners reached through a runtime probe and invoked by hand?

Karibu fires `SessionInitEvent` and `ServiceDestroyEvent` itself, because it fabricates the session
rather than letting a container do it and `tearDown()` must not call `VaadinService.destroy()` — that
would also shut the service's executor down and kill reuse across tests. Neither event has a public
fire entry point, and where the listeners live changed under us between 25.2 and 25.3
(`R_flow_service_event_bus`); the symptom was `NoSuchFieldException: sessionInitListeners` out of
every test in the `next` battery. So the two helpers probe for `getEventBus()` once per JVM and keep
the field reflection as the 25.2 branch. Why not bump the compile baseline to 25.3 and call
`getEventBus()` directly: 25.3 is a beta, and that would make the released Karibu unusable on the
Vaadin version most users are actually on, to save one cached `getMethod` call.

On the 25.3 branch the listeners are invoked in a loop over `getListeners(eventClass)` rather than
through `eventBus.fireEvent(event)`, which routes every listener through the bus's `LOG_ERRORS`
handler and swallows a listener exception into a log line. `tearDown()` promises the opposite — that
an exception from a session or service destroy listener reaches the test — and surfacing those is
most of what Karibu is for. The rethrowing overload was no better: it needs an `AtomicReference`
dance to collect the failure, and wraps what it rethrows in a `RuntimeException`. The loop is also
better defined than the branch it replaces, since `getListeners` preserves registration order where
25.2's `serviceDestroyListeners` is an unordered `Set`.

Both branches sit behind those two helpers, so a third service-level event is a branch there rather
than a new mechanism, and dropping 25.2 deletes the legacy branch cleanly.

## D_context_menu_via_target — Why is a `ContextMenu` opened by firing Vaadin's own before-open event on its target?

A `ContextMenu` is not in the server-side tree until it opens, and nothing links a target back to
its menu, so a test could only touch a menu it already held a reference to
([#20](https://github.com/mvysny/karibu-testing/issues/20)). Only two routes to it exist at all
(`R_context_menu_attachment`): recover it by reflecting over the DOM listener the target still holds,
or open it and let Vaadin attach it. Karibu opens it, by firing the
`vaadin-context-menu-before-open` event the way a right-click would.

Why not the discovery route: it needs reflection into Vaadin internals, and a menu found that way is
incompletely populated — static items are there, `setDynamicContentHandler` content and items added
from a before-open listener are not — so the very menus worth testing come back wrong. That
side-effect-free accessor was proposed upstream to karibu-tools instead
([karibu-tools#16](https://github.com/mvysny/karibu-tools/issues/16)), where a neutral lookup is the
right shape; Karibu only ever wants to *drive* the menu.

What this buys is that Karibu relies on no Vaadin internals here at all — the public before-open and
`closed` DOM events and `getTarget()` — and that the menu a test inspects is the fully populated one
the user would have seen. The one concession: a disabled target still opens, matching what the
pre-existing reference-based API already did, which needs Karibu to present an enabled `event.source`
past the `ONLY_WHEN_ENABLED` gate. That is safe only because the handler reads `event.detail` and
never the source — a Vaadin-internal fact, so it is pinned in `R_context_menu_attachment` rather than
trusted to stay true.

## D_litrenderer_jsoup — Why is `LitRenderer` markup exposed as a parsed JSoup tree rather than as a raw string?

`_getPresentationValue()` reduces a `LitRenderer` cell to plain text, so a test could assert on
`Foo Bar` but not on the conditionally-emitted `<a>` around it
([#175](https://github.com/mvysny/karibu-testing/issues/175)). Why not the first instinct, a config
flag making `_getPresentationValue()` return raw HTML instead: a `String` pushes the caller into
`contains("<a")` and regexes — the brittle hand-rolling the issue asked to be rid of — and switching
an existing function's return type from a global is a trap for whoever next reads the call site.
JSoup gives `select("a[href]")` and `attr()` for free, which is the ergonomics actually requested,
so the tree is exposed directly and `_getPresentationValue()` is left alone.

The cost is a declared dependency, and it is smaller than it looks: JSoup is already on every
consumer's classpath through `flow-server`, so it adds no artifact in practice. It is `api` scope
because the function returns a JSoup type, pinned to the version Flow ships, and declared rather
than inherited so the function keeps working if Flow ever drops it.

## D_treegrid_java_walk — Why is the Java face of the lazy `TreeGrid` walk a `Stream`, with no eager `filter` twin?

Porting a ~180-file test corpus from Kotlin to Java found `_rowSequence()` to be the one Karibu
function of 23 with no idiomatic Java form: `kotlin.sequences.Sequence<T>` forces the caller through
`SequencesKt.toList(...)`, a stdlib facade nobody reaches for
([#214](https://github.com/mvysny/karibu-testing/issues/214)).

Why `Stream` and not the issue's own proposal of `_rowIterable()`, which was briefly committed and
replaced before 2.7.3 shipped: `Sequence.asIterable()` delegates rather than buffering, so the
`Iterable` would be single-pass — precisely what `Iterable` promises not to be — and Java's
`Iterable` has no `toList()`, so it would not even have served the reporter's own call sites without
`StreamSupport`. Buffering to make it honestly multi-pass was rejected in turn, since that silently
reintroduces the full expensive walk that `_rowSequence()`'s warning exists to prevent.

Two further shapes were declined as spellings rather than capabilities. A `List`-returning
`_findAllVisibleRows(filter)`, the issue's other half: on a `TreeGrid`, `_findAll()` already *is*
`_rowSequence().toList()`, so it would be a third name for one operation — and the worse name, since
`_findAll` on a `TreeGrid` already means "visible rows". That gap was discoverability, not
capability, and was closed with a KDoc cross-reference and a README paragraph. And an eager
`filter`-taking walk, now that `_rowStream(f).toList()` spells it: worth stating carefully, because
an early draft of this entry claimed it was the same operation as `_findAll().filter { }` and a test
written to pin that claim failed. It is not the same — the filter reaches the data provider at every
level (`R_hierarchical_query_filter`) — so the decline stands on the spelling argument alone, and the
per-level semantics are documented on both functions rather than left to inference.

## D_dump_inclusive_bounds — Why does `Grid._dump(from, toInclusive)` take an inclusive end where Java is exclusive nearly everywhere?

The `(Int, Int)` overload exists because the `IntRange` one, though technically Java-reachable, is in
the same nobody-would-write-that class as `SequencesKt`. Since it exists for Java, and Java is
exclusive-end everywhere a reader has been trained — `subList`, `substring`, `copyOfRange`,
`IntStream.range` — there was a real case for making it exclusive, and it was argued. It was declined
because the two overloads share a name and the worlds are not disjoint: Kotlin can call the
`(Int, Int)` form too, so `grid._dump(0, 9)` and `grid._dump(0..9)` printing different row counts
would be a worse trap than the convention mismatch, and a trap for the maintainer as much as for the
caller. What makes the mismatch survivable is that the parameter is named `toInclusive`, which
IntelliJ shows as an inline hint at a literal call site, and that the stakes are a debug string
rather than an assertion. If the exclusive reading ever wins, it gets a distinct name — never a
differently-behaving overload of `_dump`.

## D_spring_security_bridge — Why is the `SecurityContextHolder` bridge opt-in rather than installed automatically?

`@WithMockUser` populates `SecurityContextHolder`, Vaadin route security reads two servlet-request
methods, and in production a filter connects the two — a filter Karibu does not run
(`R_spring_security_request_bridge`). So `MockSpringSecurity.mock()` installs a `mockRequestFactory`
that sources both from the holder
([#94](https://github.com/mvysny/karibu-testing/issues/94),
[#180](https://github.com/mvysny/karibu-testing/issues/180)). Bridging *both* methods is the point:
the README's previous workaround overrode only `getUserPrincipal()`, so an authenticated user passed
`@PermitAll` and failed every `@RolesAllowed` gate — the kind of half-fix that makes a security test
pass while testing nothing.

Why opt-in rather than installed from `MockSpringServletService`: the Spring module is used by apps
with no security at all, and naming `SecurityContextHolder` unconditionally would
`NoClassDefFoundError` when spring-security is absent — the same reason `spring-security-core` stays
`compileOnly`.

The caveat to know: `mockRequestFactory` is a global that `tearDown()` does not reset, hence the
"call it from `@BeforeEach`" guidance. The leak is harmless — a later non-security test sees an empty
holder and therefore a `null` principal, which is what the default `FakeRequest` gives anyway — but
it is the same class of problem as `design/ideas/karibuconfig-reset.md`.

## D_login_helper_no_oauth — Why does Karibu ship `MockVaadin.login()` but no OAuth redirect emulation?

A Keycloak-OAuth app whose views were all `@PermitAll` hit a `NotFoundException` from
`MockVaadin.setup()`, and the reporter "fixed" it by relaxing the root view to `@AnonymousAllowed`,
defeating the security model under test ([#143](https://github.com/mvysny/karibu-testing/issues/143)).
The crash itself is gone twice over — setup's initial navigation is now guarded, and
`D_spring_security_bridge` covers the Spring case — leaving ergonomics: everyone was hand-copying the
same two-line principal snippet out of the README. Hence `login()` / `logout()` in core,
framework-agnostic, as sugar over the existing `FakeRequest` primitives.

The redirect stays unemulated, and that is a scope line rather than a backlog item: it is performed
by a servlet filter before Vaadin runs and needs a real browser and a running identity provider,
which is the far side of this library's whole premise. A filter-chain or `forwardToExternalUrl`
emulation would itself be untestable browser-free. The documented stance is to skip the redirect,
`login()`, then navigate and assert. A `MockSpringSecurity.loginOidc(...)` for apps reading
`OidcUser` claims was declined too: it would pull `spring-security-oauth2-client` into the module to
duplicate what Spring Security Test or a two-line context setup already do, and those apps populate
an `OAuth2AuthenticationToken` in the holder themselves, which the bridge already reads.

## D_stable_next_build_tokens — Why are the build's two Vaadin axes called `stable` and `next`, and the runners `testrun-*`?

The Gradle catalog aliases were `vaadin-v24-*` and `vaadin-v24next-*` while pointing at Vaadin 25.2
and a 25.3 prerelease, and the internal test modules carried a `kt10-` prefix naming Vaadin *10*.
Both repeatedly misled readers, human and agent, about which Vaadin version the build actually
compiles and tests against — an expensive kind of wrong in a project whose central caveat is "mind
which Vaadin version you are reasoning about". The names now encode the two real axes instead:
`{stable, next}` crossed with `{webapp, module}`. `stable` beat `latest` because the pinned version
is not necessarily the newest release; `next` beat `prerelease` because it stays correct if
`vaadin_next` later tracks a released newer minor. Published coordinates were not touched
(`D_vnn_module_names`).

Giving both test-source libraries the leaf name `tests` collapsed them onto one Gradle module
identity and produced a circular task dependency (`R_gradle_project_identity`). They were separated
by `group` rather than by reintroducing distinct leaf names, because the clean `:tests` path was the
thing the rename was for, and the libraries are never published, so their group is cosmetic.

## D_vnn_module_names — Why does `karibu-testing-v24` keep that name when the library runs only on Vaadin 25?

The `vNN` suffix marks the highest Vaadin version whose *version-specific APIs* the module supports,
not the Vaadin version it runs against. `karibu-testing-v24` supports APIs up to and including
Vaadin 24, uses nothing Vaadin 25-specific, and runs perfectly well on Vaadin 25; a module needing a
Vaadin 25-only API would be a new `karibu-testing-v25` beside it. Renaming to match the runtime is
not on the table at any price: these are published Maven coordinates, and a rename strands every
existing build file for a cosmetic gain. The cost we carry is that the name reads as "for Vaadin 24"
to anyone who has not been told otherwise, which is why the compatibility chart at the top of
`karibu-testing-v10/README.md` outranks any `v24` in a module name.
