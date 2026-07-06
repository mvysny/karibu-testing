package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.ExtendedClientDetails
import com.vaadin.flow.server.VaadinSession
import java.util.concurrent.atomic.AtomicInteger

/**
 * The client-side of the Karibu test double: the *browser* that drives the server-side
 * [MockVaadin]. Where [MockVaadin] fabricates and controls server-side Vaadin plumbing (session,
 * service, request), `MockBrowser` models what a real browser does *to* it - opening tabs, switching
 * between them, closing them, and reloading (F5).
 *
 * A real `HttpSession`/[VaadinSession] backs many browser tabs of the same app; each tab has its own
 * [UI] and its own `window.name` (surfaced to the app as
 * [ExtendedClientDetails.getWindowName]). Karibu identifies tabs by that `window.name`: it is the
 * browser primitive for tab identity, and exactly what a tab-scope library keys on.
 *
 * After [MockVaadin.setup] the browser already owns **one** tab - the current [UI], whose
 * `window.name` is [KaribuConfig.windowName]. Additional tabs are opened with [newTab]:
 * ```
 * val main = MockBrowser.currentWindowName
 * val other = MockBrowser.newTab()   // a second tab with a distinct window.name, now focused
 * // ... act in the second tab ...
 * MockBrowser.switchTo(main)         // focus the first tab again
 * ```
 *
 * All tabs share session-scoped state but are otherwise independent: navigating, reloading or
 * closing one tab does not disturb another. [MockVaadin.tearDown] closes every tab.
 */
public object MockBrowser {
    /**
     * The `User-Agent` header the faked browser sends. Change it *before* [MockVaadin.setup] to
     * simulate a different browser (the default is Firefox 94 on Ubuntu Linux). It is read when the
     * session is created, so changing it afterwards has no effect on the current session.
     */
    @JvmStatic
    public var userAgent: String = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:94.0) Gecko/20100101 Firefox/94.0"

    /**
     * Backs the monotonic default `window.name` generator in [newTab]. Real browsers/Flow use a
     * random suffix; a monotonic counter is deterministic and therefore reproducible across runs.
     */
    private val windowNameCounter = AtomicInteger(0)

    private val currentSession: VaadinSession
        get() = checkNotNull(VaadinSession.getCurrent()) {
            "No VaadinSession - was MockVaadin.setup() called?"
        }

    /**
     * The `window.name`s of all currently open tabs (all UIs in the current session), in no
     * particular order.
     */
    @JvmStatic
    public val tabs: List<String>
        get() = currentSession.uIs.map { it.windowName }

    /**
     * The `window.name` of the currently focused tab (the current [UI]). Handy for switching back to
     * this tab later without hard-coding its name:
     * ```
     * val here = MockBrowser.currentWindowName
     * MockBrowser.newTab(); ...; MockBrowser.switchTo(here)
     * ```
     */
    @JvmStatic
    public val currentWindowName: String
        get() = checkNotNull(UI.getCurrent()) {
            "No current UI - was MockVaadin.setup() called?"
        }.windowName

    /**
     * Opens a new browser tab - a second [UI] in the *current* [VaadinSession] - with a distinct
     * [windowName], navigates it to [path] (the app root `""` by default; a *fresh* navigation, not a
     * location-preserving reload), makes it the current tab and returns it. The existing tabs stay
     * open and untouched.
     *
     * @param windowName the new tab's `window.name`; must not clash with an already-open tab.
     * Defaults to a distinct, deterministic, generated name.
     * @param path the route to navigate the new tab to; the app root `""` by default.
     * @throws IllegalArgumentException if a tab with [windowName] is already open.
     */
    @JvmStatic
    @JvmOverloads
    public fun newTab(windowName: String = generateWindowName(), path: String = ""): UI {
        require(currentSession.uIs.none { it.windowName == windowName }) {
            "A tab with window.name '$windowName' is already open. Open tabs: $tabs"
        }
        return MockVaadin.openNewTab(windowName, path)
    }

    /**
     * Switches focus to the tab identified by [windowName] - makes its [UI] the current one, so
     * subsequent test actions ([UI.getCurrent], `_get`, `_click`, ...) act in that tab. Returns the
     * focused UI.
     *
     * @throws IllegalArgumentException if no open tab has the given [windowName].
     */
    @JvmStatic
    public fun switchTo(windowName: String): UI {
        val ui: UI = findTab(windowName)
        MockVaadin.focusUI(ui)
        return ui
    }

    /**
     * Closes the tab identified by [windowName].
     *
     * By default ([beaconLost] `= false`) this models the normal case: the browser's unload beacon is
     * delivered, so the tab's [UI] is closed, detached and removed from the session immediately -
     * exactly as an F5 reload closes the old UI.
     *
     * With [beaconLost] `= true` it models a **lost** unload beacon (the tab closed but the server
     * never learned): the UI is *not* removed but left lingering, flagged so a later
     * [MockVaadin.reapInactiveUIs] closes it the way Flow's heartbeat / idle-UI cleanup eventually
     * would. This is the same lingering state a [UnloadBeaconTiming.NEVER] reload produces, cleaned by
     * the same reaper.
     *
     * You cannot close the currently focused tab (in a browser, closing the active tab moves focus to
     * an arbitrary sibling, which is poison for a scripted test) - [switchTo] another tab first. To
     * produce a lingering-UI-to-reap, close a *background* tab with [beaconLost] `= true`.
     *
     * @throws IllegalArgumentException if [windowName] is the current tab, or no open tab has it.
     */
    @JvmStatic
    @JvmOverloads
    public fun closeTab(windowName: String, beaconLost: Boolean = false) {
        require(windowName != currentWindowName) {
            "Cannot close the currently focused tab '$windowName'; switch to another tab first."
        }
        val ui: UI = findTab(windowName)
        if (beaconLost) {
            MockVaadin.markUnloadBeaconLost(ui)
        } else {
            MockVaadin.discardUI(ui)
        }
    }

    /**
     * Simulates a browser F5 reload of the current tab (equivalent to `UI.getCurrent().page.reload()`
     * - see [MockVaadin.reloadCurrentUI] for the full lifecycle), optionally letting the reloaded tab
     * arrive with a *new* [newWindowName].
     *
     * By default the `window.name` is **preserved** across the reload, as most browsers do. Passing a
     * different [newWindowName] models the browsers/navigations that do **not** preserve it (e.g.
     * Safari with dev tools closed, or navigation via typed URL / bookmark): the server then sees the
     * reloaded page as a brand-new tab.
     */
    @JvmStatic
    @JvmOverloads
    public fun reload(newWindowName: String = currentWindowName) {
        MockVaadin.reloadCurrentUI(MockVaadin.currentUiFactory, currentSession, newWindowName)
    }

    private fun findTab(windowName: String): UI =
        currentSession.uIs.firstOrNull { it.windowName == windowName }
            ?: throw IllegalArgumentException("No open tab with window.name '$windowName'. Open tabs: $tabs")

    private fun generateWindowName(): String {
        while (true) {
            val candidate = "ROOT-tab-${windowNameCounter.incrementAndGet()}"
            if (currentSession.uIs.none { it.windowName == candidate }) {
                return candidate
            }
        }
    }
}
