# Architecture

How the pieces compose — what no single symbol can say and what would be expensive to overturn:
wiring and dependency direction, the lifecycle story, the flows a newcomer needs, where to start
reading. **Normative: the code conforms.** Change this file first, then the code. Not here: why
(`decisions.md` — cite the `D_`), what Vaadin, Spring or Gradle do (`research.md` — cite the `R_`),
one symbol's behaviour (its KDoc), the module map (`AGENTS.md`). Cap 12 KB — over it, research or
KDoc content has crept in.

## Wiring

- Dependencies point one way: `karibu-testing-v10` is the core, `v23` layers Vaadin 23+ components
  on it, `v24` carries no code and republishes `v23` under a stable coordinate. `v10-groovy`,
  `v10-pro-groovy` and `v10-spring` hang off `v10`. Nothing points back up, and no module references
  a `testrun-*` project.
- Vaadin is `compileOnly` in every published module, so the consuming app's own Vaadin version is
  the one that runs. JSoup is the single `api` dependency (`D_litrenderer_jsoup`); everything else a
  consumer needs arrives from Vaadin itself.
- Two upstream libraries are load-bearing and neither is reimplemented here: `fake-servlet5` supplies
  the `jakarta.servlet` fakes (`FakeHttpSession`, `FakeRequest`, `FakeServletConfig`), and
  `kaributools` supplies general Vaadin utilities such as `VaadinVersion`.
- `MockVaadin` is the *server-side* double. It fabricates the whole chain — `MockVaadinServlet` →
  `MockService` → `VaadinSession` → the current `UI` — and pins the request, response, session, UI
  and UI factory in `ThreadLocal` strong references, because Vaadin itself holds only soft ones and a
  GC would otherwise wipe the UI out from under a running test.
- `MockBrowser` is the *client-side* half of the same double: tabs, `window.name`, reload, close. It
  reaches the server side through `internal` helpers on `MockVaadin` and never the other way round,
  so no ThreadLocal leaks into the public API (`D_mock_browser_tabs`).
- Two seams a host plugs into. `TestingLifecycleHook` is the *lookup* seam: what to await either
  side of a component lookup, what counts as a component's children and label, and what to do with
  pending JavaScript — so it is where a framework teaches the locators about its own components.
  `MockVaadin.mockRequestFactory` decides what a request looks like, and the Spring Security bridge
  is one implementation of it (`D_spring_security_bridge`).
- The locator functions (`_get`, `_find`, `_expectOne`, …) walk Vaadin's own server-side component
  tree down from `currentUI`; there is no parallel model to keep in sync. Java reaches the same
  traversal through `LocatorJ` and `SearchSpecJ`.

## Flows

**`MockVaadin.setup()`** builds, in one synchronous call, everything a servlet container and a
browser would have built across several requests: servlet → `VaadinServletService` → `FakeHttpSession`
→ request from `mockRequestFactory` → `VaadinSession` → `UI` → the initial navigation. Each layer is
made *current* as it appears, so by the time a test's first line runs, `UI.getCurrent()` and friends
answer. Two joints in that chain are not mere construction: session-init listeners have to be fired
by hand, because Karibu creates the session rather than a container (`D_service_event_bus_compat`);
and a client roundtrip is emulated before the initial navigation, so a `UIInitListener` sees
`ExtendedClientDetails` exactly where production would put it.

**An F5 reload** (`MockPage.reload()` → `MockVaadin.reloadCurrentUI()`) is the flow most of the UI
lifecycle work hangs off, and it forks on `@PreserveOnRefresh`:

- **Preserve:** the old UI is kept alive and registered while the new UI navigates, which is what
  lets Flow's own navigation teleport the overlays across and close it. Karibu only orders the two
  (`D_f5_beacon_timing`, `R_flow_preserve_on_refresh`).
- **Non-preserve:** Karibu delivers a synthetic unload beacon to Flow's real `ServerRpcHandler` and
  finalizes the close afterwards, mirroring Flow's own close/remove split
  (`D_unload_beacon_via_rpc_handler`). Where that lands relative to new-UI creation — or whether it
  lands at all — is `KaribuConfig.unloadBeaconTiming`; an undelivered beacon leaves the old UI
  lingering until `reapInactiveUIs()` (`D_reap_inactive_uis`).

There is no time axis anywhere in this: every step happens because a test called something.

## The test battery

The library's own tests do not sit beside the code they test, because each test has to run against
four environments:

- `karibu-testing-v10/tests` and `karibu-testing-v23/tests` are test-source *libraries* — test
  classes under `src/main`, no `src/test` of their own, never published. `v23:tests` depends on
  `v10:tests`, so the whole battery is reachable from one project.
- The four `karibu-testing-v24/testrun-*` projects each depend on `karibu-testing-v23:tests` and run
  that entire battery in a different simulated environment. The name encodes the two axes: Vaadin
  version (`stable`, the pinned release, or `next`, the prerelease) crossed with packaging (`webapp`,
  WAR-style with `flow-build-info.json` on the classpath, or `module`, jar-style without it).

So the battery is written once and multiplied by the runners, which is why a test belongs in a
`tests` library rather than beside a runner, and why an environment-specific failure is debugged by
running that one runner. The naming is `D_stable_next_build_tokens`; why the published modules keep
their `vNN` names is `D_vnn_module_names`.

## Where to start reading

`MockVaadin.kt` — `setup()`, `createSession()`, `createUI()` and `reloadCurrentUI()` are the whole
lifecycle in one file, and every other file in the core is either a component helper hanging off
`currentUI` or part of the fake plumbing under `mock/`.

## What is deliberately absent

- **No clock.** Nothing models heartbeat timing or elapsed-time expiry (`D_reap_inactive_uis`).
- **No OAuth redirect emulation** (`D_login_helper_no_oauth`).
- **No way to find an unopened `ContextMenu`** (`D_context_menu_via_target`).
- **No eager, filter-taking `TreeGrid` walk** (`D_treegrid_java_walk`).
