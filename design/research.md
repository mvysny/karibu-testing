# Research — Vaadin Flow's server-side internals, plus Spring Security and Gradle where the build leans on them

What the things we don't own actually do. About *them*, never us: a sentence starting "we chose"
is a `D_`. `## R_<slug> — <title>`, one claim per bullet, one provenance marker per claim —
**[docs]**, **[src]**, **[verified <date>, <version>]**, **[unverified]** (a hypothesis; a design
built on it says so). A claim is earned by its provenance, or by having cost real work to find out.
Flow changes between minors and this project runs two lines at once, so a Flow claim names the
flow-server version it was traced against — `stable` and `next` are not interchangeable here. Cite
by slug, `R_<slug>`, never by position; `grep '^## R_' design/research.md` is the index. The first
entry is the ruler: every later one trims to its length. When you have written an entry, re-read it
against the one above and cut what the provenance markers already carry.

---

## R_flow_unload_beacon — the unload beacon: how Flow detects it, handles it, and splits close from remove

- A closing or reloading tab POSTs an unload beacon during `pagehide` (`navigator.sendBeacon`,
  `unload=true`), *before* the browser requests the new document; the new UI is created later, by
  the client→server init request after the reloaded page boots. **[docs]**
- `UidlRequestHandler.getRpcHandler().handleRpc(ui, body, request)` runs it. Detection is nothing
  more than `json.has(ApplicationConstants.UNLOAD_BEACON)` (`"UNLOAD"`), and the marker makes
  `RpcRequest` skip the sync-id and client-id checks. **[src, verified 2026-07-21, flow-server
  25.2.4 and 25.3.0-alpha3]**
- `handleUnloadBeaconRequest` closes a normal UI but **ignores the beacon for a
  `@PreserveOnRefresh` target** ("Eager UI close ignored for @PreserveOnRefresh view"). It is a
  `protected` method as of 25.2; before that the same code sat inline in `handleRpc`. **[src,
  verified 2026-07-21, flow-server 25.2.4]**
- `ui.close()` only sets `isClosing`. The detach and the `session.removeUI()` happen later, in
  `removeClosedUIs()`, run from `requestEnd → cleanupSession` at the *end* of the beacon request —
  so before the new UI's init request begins. **[src, verified 2026-07-06, flow-server 25.2.1]**
- A beacon body must carry an `rpcInvocations` array: `handleInvocations()` dereferences it
  immediately, so `null` throws an NPE while an empty array is a no-op. `isCsrfTokenValid` checks
  the token whatever the XSRF configuration. **[verified 2026-07-21, flow-server 25.2.4]**
- An app replaces the handler by overriding the `protected UidlRequestHandler.createRpcHandler()`,
  the request handler itself installed through `VaadinService.createRequestHandlers()`. Flow calls
  it once and caches the instance behind `getRpcHandler()`. **[src, verified 2026-07-21]**

## R_flow_preserve_on_refresh — what an F5 does to a `@PreserveOnRefresh` UI

- The beacon being ignored (`R_flow_unload_beacon`), the old UI stays live until the *new* UI's
  navigation reaches `AbstractNavigationStateRenderer.disconnectElements()`. **[src, verified
  2026-07-06, flow-server 25.2.1]**
- `disconnectElements()` does three things in order: marks the old UI with the internal
  `ReplacedViaPreserveOnRefresh` sentinel via `Element.removeFromTree(false)`; teleports the old
  UI's remaining UI-level children — open dialogs and notifications — onto the new UI with
  `UIInternals.moveElementsFrom()`; then calls `oldUI.close()`. **[src, verified 2026-07-06,
  flow-server 25.2.1]**
- The resulting child order on the new UI is **route root first, then the teleported overlays**,
  because `updateRoot` re-attaches the route after the overlays were moved. **[verified 2026-07-06,
  flow-server 25.2.1]**
- The preserved-chain lookup keys on `window.name`, so which UI a reload resumes is independent of
  when — or whether — the beacon arrives. **[src, verified 2026-07-06]**

## R_flow_ui_reap — how Flow closes a UI whose tab stopped answering

- `VaadinService.cleanupSession()`, called from `requestEnd`, runs `closeInactiveUIs()`: for every
  UI with `!isUIActive(ui) && !ui.isClosing()` it calls `ui.close()`, and `removeClosedUIs()` then
  detaches them and removes them from the session. **[src, verified 2026-07-06, flow-server 25.2.1]**
- `isUIActive` is purely time-based — a UI is inactive once it has missed roughly three heartbeats,
  `getHeartbeatTimeout()` being `heartbeatInterval * 3.1` measured against the UI's last-heartbeat
  timestamp. **[src, verified 2026-07-06, flow-server 25.2.1]**
- Nothing tells Flow *why* a UI went quiet: a lost beacon, a frozen tab, a sleeping laptop and a
  throttled background tab all reach the reap by the same elapsed-time route. **[src, verified
  2026-07-06]**

## R_flow_service_event_bus — where `VaadinService`'s session-init and service-destroy listeners live

- Through 25.2 they are two private `VaadinService` fields, `sessionInitListeners` (a list) and
  `serviceDestroyListeners` (an unordered `Set`), and neither event has a public fire entry point.
  **[src, verified 2026-09-03, flow-server 25.2.6]**
- 25.3 deletes both fields. Every service-level listener now lives in one `VaadinServiceEventBus
  eventBus`, a `Map<Class<? extends EventObject>, CopyOnWriteArrayList<SerializableConsumer<?>>>`;
  `addSessionInitListener` and `addServiceDestroyListener` wrap the listener into a consumer keyed
  by the *event* class. **[src, verified 2026-09-03, flow-server 25.3.0-beta1]**
- `VaadinService.getEventBus()`, `VaadinServiceEventBus.getListeners(Class)` and
  `fireEvent(EventObject)` are all public in 25.3, and `getListeners` returns the consumers in
  registration order. **[src, verified 2026-09-03, flow-server 25.3.0-beta1]**
- `fireEvent(EventObject)` routes every listener through the bus's `LOG_ERRORS` handler, which
  swallows a listener exception into a log line. The `fireEvent(E, SerializableBiConsumer)` overload
  can rethrow, but only by collecting into an `AtomicReference` the way `VaadinService.destroy()`
  does, and it wraps what it rethrows in a `RuntimeException`. **[src, verified 2026-09-03]**
- `VaadinService.destroy()` shuts the service's executor down as well as firing the destroy event.
  **[src, verified 2026-09-03, flow-server 25.2.6]**

## R_context_menu_attachment — a `ContextMenu` is not in the server-side tree until it opens

- `OverlayAutoAddController` attaches the menu to the UI (`ui.addToModalComponent`) only on open and
  removes it on close; the `opened`-property path defers the add to `beforeClientResponse`, which is
  never flushed without a browser. **[verified 2026-07-07, Vaadin 25.2.1 and 25.3.0-alpha3]**
- There is no reverse lookup. The target keeps no reference to its menu, and Vaadin offers no
  `Component.getContextMenu()` and no registry — the only link is `ContextMenu.getTarget()`.
  **[verified 2026-07-07, Vaadin 25.2.1]**
- `ContextMenuBase.setTarget` registers a `vaadin-context-menu-before-open` DOM listener on the
  target whose bound method-ref captures the menu, so reflection over that listener is the one
  discovery route there is. **[src, verified 2026-07-07]**
- Firing that DOM event runs Vaadin's own `beforeOpenHandler`, which calls `onBeforeOpenMenu()`
  (populating dynamic content) and `overlayAutoAddController.add()` (attaching synchronously). The
  menu attaches as a **UI sibling**, not under the target. **[verified 2026-07-07, Vaadin 25.2.1]**
- A menu found without opening it is incompletely populated: static `addItem` entries are present,
  but dynamic content — `GridContextMenu.setDynamicContentHandler`, or items added from an open or
  before-open listener — is not. **[verified 2026-07-07]**
- The before-open listener is registered `ONLY_WHEN_ENABLED`, so `ElementListenerMap.fireEvent`
  drops it on a disabled element. The handler reads only `event.detail`, never `event.source`.
  **[src, verified 2026-07-07]**

## R_hierarchical_query_filter — a hierarchical filter is per level, and `TreeDataProvider` keeps a match's ancestors

- The filter handed to a hierarchical walk goes into the `HierarchicalQuery` used at *every* level —
  `getChildrenOf` is `checkedFetch(HierarchicalQuery(filter, item))` — so what it means is the data
  provider's business, never a predicate over the flattened list. **[src, verified 2026-08-28,
  flow-server 25.2]**
- Vaadin's own `TreeDataProvider` keeps an item when **it or any descendant** matches. On a
  `0 -> 1 -> … -> 9` chain, filtering for `it == 9` yields `[0..9]`, not `[9]`. **[verified
  2026-08-28, Vaadin 25.2]**
- A back-end provider that applies the predicate strictly per level prunes the rejected subtree
  instead, which is the opposite outcome from the same filter. **[unverified]**

## R_spring_security_request_bridge — how Spring Security reaches the request, and what Vaadin route security reads

- `@WithMockUser`, `@WithUserDetails` and `@WithSecurityContext` populate `SecurityContextHolder`
  and nothing else. **[docs]**
- In production a servlet filter wraps the request in `SecurityContextHolderAwareRequestWrapper`,
  and that wrapper is the only reason `getUserPrincipal()` and `isUserInRole()` answer from the
  holder at all. **[docs]**
- Vaadin route security — `NavigationAccessControl` and `AccessAnnotationChecker` — reads exactly
  those two request methods, and consults no Spring type. **[src, verified 2026-07-07, Vaadin 25.2]**
- Anonymous is not logged in: the wrapper maps both `null` and an `AnonymousAuthenticationToken` to
  a `null` principal, deciding it through `AuthenticationTrustResolverImpl.isAnonymous()` rather
  than a raw `instanceof`, so a custom anonymous token counts too. **[src]**
- Role matching goes through Spring's `GrantedAuthorityDefaults` prefix, `ROLE_` by default:
  `@RolesAllowed("ADMIN")` → `isUserInRole("ADMIN")` → authority `ROLE_ADMIN`. **[docs]**

## R_gradle_project_identity — a Gradle project's module identity is `group` plus project name

- Two subprojects sharing a leaf directory name *and* a `group` share one module identity. Gradle's
  conflict resolution then substitutes one for the other, and where one depends on the other that
  yields a self-referential circular task dependency, `compileKotlin → jar → compileKotlin`.
  **[verified 2026-07-07]**
- `archivesName` is not part of the identity: overriding it leaves both the identity and the cycle
  exactly as they were. **[verified 2026-07-07]**
