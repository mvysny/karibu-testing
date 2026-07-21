package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.ExtendedClientDetails
import com.vaadin.flow.server.VaadinSession
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * Tests [MockBrowser]: multiple browser tabs (UIs) in one [VaadinSession], tab switching,
 * per-tab `window.name` identity, closing tabs (beacon delivered vs lost) and reload.
 */
abstract class AbstractMockBrowserTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup(Routes().autoDiscoverViews("com.github")) }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    private val session: VaadinSession get() = VaadinSession.getCurrent()

    @Nested inner class `initial tab` {
        @Test fun `setup starts with exactly one tab, seeded with KaribuConfig-windowName`() {
            expect(listOf(KaribuConfig.windowName)) { MockBrowser.tabs }
            expect(KaribuConfig.windowName) { MockBrowser.currentWindowName }
            expect(1) { session.uIs.size }
        }

        @Test fun `the faked ECD carries the seed window-name`() {
            lateinit var ecd: ExtendedClientDetails
            UI.getCurrent().page.retrieveExtendedClientDetails { ecd = it }
            MockVaadin.clientRoundtrip()
            expect(KaribuConfig.windowName) { ecd.windowName }
        }
    }

    @Nested inner class newTab {
        @Test fun `opens a second UI in the same session, made current`() {
            val main = UI.getCurrent()
            val tab = MockBrowser.newTab("tab-B")
            expect(false) { tab === main }
            expect(tab) { UI.getCurrent() }
            expect(setOf(main, tab)) { session.uIs.toSet() }
            expect("tab-B") { MockBrowser.currentWindowName }
        }

        @Test fun `navigates the new tab to the app root by default`() {
            UI.getCurrent().navigate("helloworld")
            _get<HelloWorldView>()
            MockBrowser.newTab("tab-B")
            _get<WelcomeView>()   // fresh navigation to root, not the old tab's location
        }

        @Test fun `navigates the new tab to the given path`() {
            MockBrowser.newTab("tab-B", "helloworld")
            _get<HelloWorldView>()
        }

        @Test fun `auto-generated window-name is distinct`() {
            val main = MockBrowser.currentWindowName
            MockBrowser.newTab()
            expect(true) { MockBrowser.currentWindowName != main }
            expect(2) { MockBrowser.tabs.size }
            expect(2) { MockBrowser.tabs.toSet().size }   // all names distinct
        }

        @Test fun `the tabs share the same session`() {
            val s = VaadinSession.getCurrent()
            s.setAttribute("k", "v")
            MockBrowser.newTab("tab-B")
            expect(s) { VaadinSession.getCurrent() }
            expect("v") { VaadinSession.getCurrent().getAttribute("k") }
        }

        @Test fun `an already-open window-name is rejected`() {
            MockBrowser.newTab("tab-B")
            expectThrows<IllegalArgumentException>("A tab with window.name 'tab-B' is already open") {
                MockBrowser.newTab("tab-B")
            }
            // the seed name is taken too
            expectThrows<IllegalArgumentException>("is already open") {
                MockBrowser.newTab(KaribuConfig.windowName)
            }
        }
    }

    @Nested inner class switchTo {
        @Test fun `moves focus between tabs`() {
            val main = MockBrowser.currentWindowName
            val mainUI = UI.getCurrent()
            val tabUI = MockBrowser.newTab("tab-B")

            expect(tabUI) { UI.getCurrent() }
            expect(mainUI) { MockBrowser.switchTo(main) }
            expect(mainUI) { UI.getCurrent() }
            expect(main) { MockBrowser.currentWindowName }

            MockBrowser.switchTo("tab-B")
            expect(tabUI) { UI.getCurrent() }
        }

        @Test fun `an unknown window-name is rejected`() {
            expectThrows<IllegalArgumentException>("No open tab with window.name 'nope'") {
                MockBrowser.switchTo("nope")
            }
        }
    }

    @Nested inner class closeTab {
        @Test fun `beacon delivered closes, detaches and removes the tab immediately`() {
            val tab = MockBrowser.newTab("tab-B")
            var detached = false
            tab.addDetachListener { detached = true }
            MockBrowser.switchTo(MockBrowser.tabs.first { it != "tab-B" })

            MockBrowser.closeTab("tab-B")

            expect(true) { detached }
            expect(true) { tab.isClosing }
            expect(false) { session.uIs.contains(tab) }
            expect(false) { MockBrowser.tabs.contains("tab-B") }
        }

        @Test fun `closing one tab leaves the others alive`() {
            val mainName = MockBrowser.currentWindowName
            val main = UI.getCurrent()
            MockBrowser.newTab("tab-B")
            MockBrowser.switchTo(mainName)
            MockBrowser.closeTab("tab-B")
            expect(listOf(main)) { session.uIs.toList() }
            expect(main) { UI.getCurrent() }
        }

        @Test fun `beaconLost leaves the tab lingering until reapInactiveUIs`() {
            val tab = MockBrowser.newTab("tab-B")
            val mainName = MockBrowser.tabs.first { it != "tab-B" }
            MockBrowser.switchTo(mainName)

            MockBrowser.closeTab("tab-B", beaconLost = true)
            // still there, not closed
            expect(true) { session.uIs.contains(tab) }
            expect(false) { tab.isClosing }

            var detached = false
            tab.addDetachListener { detached = true }
            MockVaadin.reapInactiveUIs()

            expect(true) { detached }
            expect(true) { tab.isClosing }
            expect(false) { session.uIs.contains(tab) }
        }

        // issue #210: Flow ignores the unload beacon for a @PreserveOnRefresh target, so closing such
        // a tab must leave its UI lingering (as a real browser does, until the heartbeat reap) rather
        // than detach it immediately - the fidelity gap the real-handler beacon delivery fixes.
        @Test fun `closing a @PreserveOnRefresh tab leaves it lingering until reapInactiveUIs`() {
            val tab = MockBrowser.newTab("tab-B", "preserveonrefresh")
            MockBrowser.switchTo(MockBrowser.tabs.first { it != "tab-B" })

            MockBrowser.closeTab("tab-B")
            // Flow ignored the beacon: the tab's UI is still there, not closed.
            expect(true) { session.uIs.contains(tab) }
            expect(false) { tab.isClosing }

            MockVaadin.reapInactiveUIs()
            // the heartbeat reap eventually closes and removes it.
            expect(true) { tab.isClosing }
            expect(false) { session.uIs.contains(tab) }
        }

        @Test fun `the current tab cannot be closed (beacon delivered)`() {
            expectThrows<IllegalArgumentException>("Cannot close the currently focused tab") {
                MockBrowser.closeTab(MockBrowser.currentWindowName)
            }
        }

        @Test fun `the current tab cannot be closed (beaconLost)`() {
            expectThrows<IllegalArgumentException>("Cannot close the currently focused tab") {
                MockBrowser.closeTab(MockBrowser.currentWindowName, beaconLost = true)
            }
        }

        @Test fun `an unknown window-name is rejected`() {
            expectThrows<IllegalArgumentException>("No open tab with window.name 'nope'") {
                MockBrowser.closeTab("nope")
            }
        }
    }

    @Nested inner class reload {
        @Test fun `preserves the window-name by default`() {
            val name = MockBrowser.currentWindowName
            val oldUI = UI.getCurrent()
            MockBrowser.reload()
            expect(false) { UI.getCurrent() === oldUI }
            expect(name) { MockBrowser.currentWindowName }
        }

        @Test fun `can arrive with a new window-name (non-preserving browser)`() {
            val oldName = MockBrowser.currentWindowName
            MockBrowser.reload("fresh-name")
            expect("fresh-name") { MockBrowser.currentWindowName }
            expect(listOf("fresh-name")) { MockBrowser.tabs }   // old tab replaced (default EAGER beacon)
            expect(false) { MockBrowser.tabs.contains(oldName) }
        }

        @Test fun `reloads only the current tab, leaving others untouched`() {
            MockBrowser.newTab("tab-B")
            val bName = MockBrowser.currentWindowName
            val bUI = UI.getCurrent()
            MockBrowser.reload("tab-B-reloaded")
            // the other tab is still open and unchanged
            expect(true) { MockBrowser.tabs.contains("tab-B-reloaded") }
            expect(false) { MockBrowser.tabs.contains(bName) }
            expect(false) { UI.getCurrent() === bUI }
            expect(2) { session.uIs.size }
        }
    }

    @Nested inner class `tearDown nukes all tabs` {
        @Test fun `every tab UI is detached and the session is cleared`() {
            val main = UI.getCurrent()
            val b = MockBrowser.newTab("tab-B")
            val c = MockBrowser.newTab("tab-C")
            val detached = mutableSetOf<UI>()
            listOf(main, b, c).forEach { ui -> ui.addDetachListener { detached.add(ui) } }

            MockVaadin.tearDown()

            expect(setOf(main, b, c)) { detached }
            expect(null) { VaadinSession.getCurrent() }
            expect(null) { UI.getCurrent() }
        }
    }
}
