@file:Suppress("DEPRECATION")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.text
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributesting.v10.mock.*
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.ExtendedClientDetails
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.router.*
import com.vaadin.flow.server.*
import com.vaadin.flow.server.auth.AnonymousAllowed
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ExecutionException
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write
import kotlin.test.expect

abstract class AbstractMockVaadinTests() {
    companion object {
        lateinit var routes: Routes
        @BeforeAll @JvmStatic fun discoverRoutes() { routes = Routes().autoDiscoverViews("com.github") }
    }
    @BeforeEach fun fakeVaadin() {
        MockVaadin.setup(routes)
        expect("""
└── MockedUI[]
    └── WelcomeView[@theme='padding spacing']
        └── Text[text='Welcome!']
""".trim()) { UI.getCurrent().toPrettyTree().trim() }
    }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class `setup-teardown tests` {
        @Test fun `smoke test that everything is mocked`() {
            expect(true) { UI.getCurrent() != null }
            expect(true) { VaadinSession.getCurrent() != null }
            expect(true) { VaadinService.getCurrent() != null }
            expect(true) { VaadinRequest.getCurrent() != null }
            expect(true) { VaadinSession.getCurrent().configuration != null }
            expect(true) { VaadinSession.getCurrent().service != null }
            expect(true) { VaadinSession.getCurrent().browser != null }
            expect(true) { VaadinSession.getCurrent().browser.locale != null }
            expect(false) { VaadinSession.getCurrent().browser.isIPhone }
            expect(true) { VaadinSession.getCurrent().browser.isFirefox }
            expect(false) { VaadinSession.getCurrent().browser.isChrome }
            expect(false) { VaadinSession.getCurrent().browser.isChromeOS }
            expect(false) { VaadinSession.getCurrent().browser.isAndroid }
            expect(false) { VaadinSession.getCurrent().browser.isEdge }
            expect(true) { VaadinResponse.getCurrent() != null }
        }

        @Test fun `current UI contains sane values`() {
            expect(true) { UI.getCurrent().locale != null }
            expect(true) { UI.getCurrent().element != null }
            expect(true) { UI.getCurrent().session != null }
            expect(VaadinSession.getCurrent()) { UI.getCurrent().session }
            expect(true) { UI.getCurrent().session.session != null }
            expect(true) { UI.getCurrent().loadingIndicatorConfiguration != null }
            expect(true) { UI.getCurrent().pushConfiguration != null }
            expect(true) { UI.getCurrent().reconnectDialogConfiguration != null }
            expect(true) { UI.getCurrent().internals != null }
            expect(true) { UI.getCurrent().page != null }
            expect(true) { UI.getCurrent().session.service.router != null }
        }

        @Test fun serializable() {
            System.setProperty("sun.io.serialization.extendedDebugInfo", "true") // https://mvysny.github.io/NotSerializableException/
            UI.getCurrent().cloneBySerialization()
            VaadinSession.getCurrent().cloneBySerialization()
            // even that it says it's Serializable it's really not.
            // VaadinService.getCurrent().cloneBySerialization()
            // VaadinRequest.getCurrent().cloneBySerialization()
            // VaadinResponse.getCurrent().cloneBySerialization()
        }

        @Test fun `setup() can be called multiple times in a row`() {
            MockVaadin.setup()
            MockVaadin.setup()
        }

        @Test fun `setup() always provides new instances`() {
            MockVaadin.setup()
            val ui = UI.getCurrent()!!
            MockVaadin.setup()
            expect(true) { UI.getCurrent()!! !== ui }
        }

        @Test fun `Vaadin-getCurrent() returns null after tearDown()`() {
            MockVaadin.tearDown()
            expect(null) { VaadinSession.getCurrent() }
            expect(null) { VaadinService.getCurrent() }
            expect(null) { VaadinRequest.getCurrent() }
            expect(null) { UI.getCurrent() }
            expect(null) { VaadinResponse.getCurrent() }
        }

        @Test fun `tearDown() can be called multiple times`() {
            MockVaadin.tearDown()
            MockVaadin.tearDown()
            MockVaadin.tearDown()
        }

        @Test fun `tearDown() calls UI detach listeners`() {
            val vl = UI.getCurrent().verticalLayout()
            var vldetachCalled = 0
            vl.addDetachListener {
                vldetachCalled++
                expect(1, "detach should be called only once") { vldetachCalled }
            }
            var detachCalled = 0
            UI.getCurrent().addDetachListener {
                detachCalled++
                expect(1, "detach should be called only once") { detachCalled }
            }
            MockVaadin.tearDown()
            expect(1, "detach should be called exactly once") { detachCalled }
            expect(1, "detach should be called exactly once") { vldetachCalled }
        }

        @Test fun `tearDown() runs UI-access{} blocks`() {
            var called = 0
            UI.getCurrent().access { called++ }
            expect(0) { called }
            MockVaadin.tearDown()
            expect(1) { called }
        }

        @Test fun `when UI-access{} throws, follow-up setup() shouldn't be affected`() {
            UI.getCurrent().access { throw RuntimeException("Simulated") }
            expectThrows<ExecutionException>("Simulated") {
                MockVaadin.tearDown()
            }
            MockVaadin.setup()
        }

        @Test fun `check listeners called`() {
            var initCalled = false
            var beforeEnterListenerCalled = false
            var afterNavigationListenerCalled = false
            MockVaadin.tearDown()
            MockVaadin.setup(routes, uiFactory = {
                object : MockedUI() {
                    override fun init(request: VaadinRequest) {
                        initCalled = true
                        addBeforeEnterListener { beforeEnterListenerCalled = true }
                        addAfterNavigationListener { afterNavigationListenerCalled = true }
                    }
                }
            })
            expect(true) { initCalled }
            // setup navigates to "" by default, which is WelcomeView. It must therefore have been instantiated and attaahed to the UI
            _expectOne<WelcomeView>()
            expect(true) { beforeEnterListenerCalled }
            expect(true) { afterNavigationListenerCalled }
        }

        @Test fun `no view set when initDefaultRoute=false`() {
            KaribuConfig.initDefaultRoute = false
            try {
                MockVaadin.tearDown()
                MockVaadin.setup(routes)
                expect("""
└── MockedUI[]
""".trim()) { UI.getCurrent().toPrettyTree().trim() }
            } finally {
                KaribuConfig.initDefaultRoute = true
            }
        }
    }

    @Nested inner class `proper mocking` {
        @Test fun `configuration mocked as well`() {
            expect(false) { VaadinSession.getCurrent().configuration.isProductionMode }
        }

        @Test fun verifyAttachCalled() {
            var attachCallCount = 0
            var detachCallCount = 0
            val vl = object : VerticalLayout() {
                override fun onAttach(attachEvent: AttachEvent?) {
                    super.onAttach(attachEvent)
                    attachCallCount++
                }

                override fun onDetach(detachEvent: DetachEvent?) {
                    super.onDetach(detachEvent)
                    detachCallCount++
                }
            }
            vl.addAttachListener {
                expect(true) { vl.isAttached() }
                attachCallCount++
            }
            vl.addDetachListener {
                // a bug in Vaadin? I'd expect the node to be detached (null parent etc) at this point...
                // See https://github.com/vaadin/flow/issues/8809
                expect(true) { vl.isAttached() }
                detachCallCount++
            }

            // attach
            UI.getCurrent().add(vl)
            expect(2) { attachCallCount }
            expect(true) { vl.isAttached() }
            expect(0) { detachCallCount }

            // close UI - detach is not called.
            UI.getCurrent().close()
            expect(2) { attachCallCount }
            expect(true) { vl.isAttached() }
            expect(0) { detachCallCount }

            // detach
            vl.removeFromParent()
            expect(2) { attachCallCount }
            expect(false) { vl.isAttached() }
            expect(2) { detachCallCount }
        }

        @Test fun `detach on forceful UI close`() {
            val vl = UI.getCurrent().verticalLayout()
            var detachCalled = 0
            vl.addDetachListener { detachCalled++ }
            expect(true) { vl.isAttached() }

            // close UI - detach is not called.
            UI.getCurrent().close()
            expect(true) { vl.isAttached() }
            expect(0) { detachCalled }
            expect(true) { UI.getCurrent().isAttached() }

            // Mock closing of UI after request handled
            UI.getCurrent()._close()
            expect(false) { vl.isAttached() }
            expect(1) { detachCalled }
            expect(false) { UI.getCurrent().isAttached() }
        }

        @Test fun `navigation works in mocked env`() {
            // no need: when UI is initialized in MockVaadin.setup(), automatic navigation to "" is performed.
//        UI.getCurrent().navigate("")
            _get<Text> { text = "Welcome!" }
            UI.getCurrent().navigate("helloworld")
            _get<Button> { caption = "Hello, World!" }
        }

        @Test fun `navigation to parametrized view works in mocked env`() {
            UI.getCurrent().navigate("params/1")
            _get<ParametrizedView>()
        }

        @Test fun `navigation to view with parent route works in mocked env`() {
            UI.getCurrent().navigate("parent/child")
            _get<ChildView>()
        }

        @Test fun `UI-getUrl() to view works in mocked env`() {
            val routeConfig = RouteConfiguration.forSessionScope()
            expect("helloworld") { routeConfig.getUrl(HelloWorldView::class.java) }
            expect("params/1") { routeConfig.getUrl(ParametrizedView::class.java, 1) }
            expect("parent/child") { routeConfig.getUrl(ChildView::class.java) }
            expect("helloworld") { RouteConfiguration.forApplicationScope().getUrl(HelloWorldView::class.java) }
            expect("params/1") { RouteConfiguration.forApplicationScope().getUrl(ParametrizedView::class.java, 1) }
            expect("parent/child") { RouteConfiguration.forApplicationScope().getUrl(ChildView::class.java) }
        }

        // tests https://github.com/mvysny/karibu-testing/issues/11
        @Nested inner class `beforeClientResponse invoked` {
            @Test fun `on an UI`() {
                var ran = false
                UI.getCurrent().beforeClientResponse(UI.getCurrent()) {
                    expect(false, "the block was supposed to be run only once") { ran }
                    ran = true
                }
                _get<UI> {} // do the lookup which should trigger the beforeClientResponse run
                expect(true) { ran }
            }
            @Test fun `on a button nested within the UI`() {
                var ran = false
                val button = UI.getCurrent().button()
                UI.getCurrent().beforeClientResponse(button) {
                    expect(false, "the block was supposed to be run only once") { ran }
                    ran = true
                }
                _get<UI> {} // do the lookup which should trigger the beforeClientResponse run
                expect(true) { ran }
            }
        }
    }

    @Nested inner class dialogs {

        @Test fun `open dialog`() {
            // there should be no dialogs in the UI
            _expectNone<Dialog>()
            _expectNone<Div> { text = "Dialog Text" }
            val dialog = Dialog(Div().apply { text("Dialog Text") })
            dialog.open()
            _get<Dialog>()  // should be in the UI, along with its contents
            _get<Div> { text = "Dialog Text" }
            dialog.close()
            // there should be no dialogs in the UI
            _expectNone<Div> { text = "Dialog Text" }
            _expectNone<Dialog>()
        }

        @Test fun `the dialogs must be cleared up from the component tree on close`() {
            val dialog = Dialog(Div().apply { text("Dialog Text") })
            dialog.open()
            dialog.close()
            cleanupDialogs()
            expect(
                    """
└── MockedUI[]
    └── WelcomeView[@theme='padding spacing']
        └── Text[text='Welcome!']
""".trim()
            ) { UI.getCurrent().toPrettyTree().trim() }
        }
    }

    @Nested inner class `page reloading` {
        @Test fun `Page reload should re-create the UI`() {
            val ui = UI.getCurrent()
            var detachCalled = false
            ui.addDetachListener {
                expect(false, "detach should be called only once") { detachCalled }
                detachCalled = true
            }
            UI.getCurrent().page.reload()
            // a new UI must be created; but the Session must stay the same.
            expect(true) { UI.getCurrent() != null }
            expect(false) { UI.getCurrent() === ui }
            // the old UI must be detached properly
            expect(true) { detachCalled }
        }

        @Test fun `Page reload should preserve session`() {
            val session = VaadinSession.getCurrent()
            session.setAttribute("foo", "bar")
            UI.getCurrent().page.reload()
            expect(true) { VaadinSession.getCurrent() === session }
            expect("bar") { VaadinSession.getCurrent().getAttribute("foo") }
        }

        @Test fun `Page reload should automatically navigate to the current URL`() {
            _get<WelcomeView>()
            UI.getCurrent().page.reload()
            _get<WelcomeView>()
            UI.getCurrent().navigate("helloworld")
            _expectNone<WelcomeView>()
            _get<HelloWorldView>()
            UI.getCurrent().page.reload()
            _expectNone<WelcomeView>()
            _get<HelloWorldView>()
        }

        @Test fun `page reload should create new view instance`() {
            navigateTo<HelloWorldView>()
            val viewInstance = _get<HelloWorldView>()
            UI.getCurrent().page.reload()
            expect(false) { viewInstance === _get<HelloWorldView>() }
        }

        // https://github.com/mvysny/karibu-testing/issues/118
        @Test fun `page reload should preserve the view instance on @PreserveOnRefresh`() {
            navigateTo<PreserveOnRefreshView>()
            val viewInstance = _get<PreserveOnRefreshView>()
            UI.getCurrent().page.reload()
            expect(true) { viewInstance === _get<PreserveOnRefreshView>() }
        }
    }

    // Higher-fidelity F5 lifecycle: real Flow keeps the old UI alive while the new one navigates,
    // so AbstractNavigationStateRenderer.disconnectElements() can teleport UI-level overlays
    // (dialogs/notifications) onto the new UI for a @PreserveOnRefresh target, then close the old UI.
    // https://github.com/mvysny/karibu-testing/issues/207
    @Nested inner class `page reload F5 lifecycle (issue 207)` {
        @Test fun `a dialog open across F5 survives on the new UI (@PreserveOnRefresh)`() {
            navigateTo<PreserveOnRefreshView>()
            val oldUI = UI.getCurrent()
            var clicked = false
            val okButton = Button("OK") { clicked = true }
            val dialog = Dialog(okButton)
            dialog.open()
            _expectOne<Dialog>()   // flush the deferred attach, as if the dialog was opened in a prior request
            expect(true) { dialog.isOpened }
            expect(oldUI) { dialog.ui.get() }

            oldUI.page.reload()

            val newUI = UI.getCurrent()
            expect(false) { newUI === oldUI }
            // In real Flow the SAME overlay instance is moved onto the new UI - not dropped, not recreated.
            expect(true) { dialog.isOpened }
            expect(newUI) { dialog.ui.get() }
            _get<Dialog>()   // the overlay is present on the new UI
            // ...and it's still interactive: the same button instance still fires its listener.
            okButton._click()
            expect(true) { clicked }
        }

        @Test fun `the same dialog instance is teleported, not recreated`() {
            navigateTo<PreserveOnRefreshView>()
            val dialog = Dialog(Button("OK"))
            dialog.open()
            _expectOne<Dialog>()
            UI.getCurrent().page.reload()
            expect(true) { dialog === _get<Dialog>() }
        }

        @Test fun `a Notification teleports across F5 (@PreserveOnRefresh)`() {
            navigateTo<PreserveOnRefreshView>()
            val oldUI = UI.getCurrent()
            val notification = Notification("hello")
            notification.open()
            _expectOne<Notification>()
            expect(oldUI) { notification.ui.get() }

            oldUI.page.reload()

            val newUI = UI.getCurrent()
            expect(true) { notification.isOpened }
            expect(newUI) { notification.ui.get() }
            _get<Notification>()
        }

        @Test fun `an overlay is NOT carried over across F5 of a non-@PreserveOnRefresh view`() {
            navigateTo<HelloWorldView>()
            val oldUI = UI.getCurrent()
            val dialog = Dialog(Button("OK"))
            dialog.open()
            _expectOne<Dialog>()
            expect(true) { dialog.isOpened }

            oldUI.page.reload()

            val newUI = UI.getCurrent()
            _expectNone<Dialog>()   // the overlay is absent from the new UI
            // it was not teleported: it stays behind on the discarded old UI, not moved onto the new one.
            expect(false) { dialog.ui.orElse(null) === newUI }
        }

        @Test fun `child order on the new UI matches Flow (route root, then teleported overlays)`() {
            navigateTo<PreserveOnRefreshView>()
            val view = _get<PreserveOnRefreshView>()
            val dialog1 = Dialog().apply { open() }
            val dialog2 = Dialog().apply { open() }
            _expect<Dialog>(2)

            UI.getCurrent().page.reload()

            // Karibu drives Flow's real navigation pipeline, so the child ordering it produces IS
            // Flow's ordering: the preserved route root is re-attached first, then the overlays
            // teleported off the old UI (via UIInternals.moveElementsFrom) are appended after it,
            // preserving their relative order.
            expect(listOf<Component>(view, dialog1, dialog2)) { UI.getCurrent().children.toList() }
        }

        @Test fun `old UI is marked closing after F5 (@PreserveOnRefresh)`() {
            navigateTo<PreserveOnRefreshView>()
            val oldUI = UI.getCurrent()
            oldUI.page.reload()
            expect(true) { oldUI.isClosing }
        }

        @Test fun `old UI is marked closing after F5 (plain view)`() {
            navigateTo<HelloWorldView>()
            val oldUI = UI.getCurrent()
            oldUI.page.reload()
            expect(true) { oldUI.isClosing }
        }

        @Test fun `session ends with exactly one live UI after F5`() {
            navigateTo<PreserveOnRefreshView>()
            val oldUI = UI.getCurrent()
            oldUI.page.reload()
            val session = VaadinSession.getCurrent()
            expect(listOf(UI.getCurrent())) { session.uIs.toList() }
            expect(false) { session.uIs.contains(oldUI) }
        }

        // The transient window where both the old and the new UI are live at the same time is a
        // real production state (a "there is exactly one UI" assumption passes Karibu today but
        // throws in production). The new UI is created & registered before it navigates, while the
        // old one is still around.
        @Test fun `both old and new UI are alive during the new UI init (transient two-UI window)`() {
            navigateTo<PreserveOnRefreshView>()
            val oldUI = UI.getCurrent()
            var uiCountDuringInit = -1
            VaadinService.getCurrent().addUIInitListener { event ->
                uiCountDuringInit = event.ui.session.uIs.size
            }
            oldUI.page.reload()
            expect(2) { uiCountDuringInit }
        }
    }

    // The browser unload beacon (navigator.sendBeacon) closes the old UI on F5; its timing relative
    // to the new UI's creation is configurable for non-@PreserveOnRefresh targets. Flow ignores the
    // beacon for @PreserveOnRefresh. See ideas/beacon-reload-timing.md.
    @Nested inner class `unload beacon timing on F5` {
        @BeforeEach @AfterEach fun resetTiming() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.EAGER
        }

        /**
         * Registers detach/init listeners on the current UI + service, reloads, and reports the
         * observed ordering and the session UI count seen during the new UI's init.
         */
        private fun reloadAndCapture(): ReloadCapture {
            val oldUI: UI = UI.getCurrent()
            val order: MutableList<String> = mutableListOf()
            var uiCountDuringNewInit = -1
            oldUI.addDetachListener { order.add("old-detach") }
            VaadinService.getCurrent().addUIInitListener { event ->
                order.add("new-init")
                uiCountDuringNewInit = event.ui.session.uIs.size
            }
            oldUI.page.reload()
            return ReloadCapture(oldUI, UI.getCurrent(), order, uiCountDuringNewInit)
        }

        // --- non-@PreserveOnRefresh: the beacon closes the old UI, timing is configurable ---

        @Test fun `EAGER (default) closes the old UI before creating the new one`() {
            navigateTo<HelloWorldView>()
            val c = reloadAndCapture()
            expect(listOf("old-detach", "new-init")) { c.eventOrder }  // detach-before-attach
            expect(1) { c.uiCountDuringNewInit }                       // old already gone
            expect(true) { c.oldUI.isClosing }
            expect(listOf(c.newUI)) { VaadinSession.getCurrent().uIs.toList() }
        }

        @Test fun `LATE closes the old UI after creating the new one`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.LATE
            navigateTo<HelloWorldView>()
            val c = reloadAndCapture()
            expect(listOf("new-init", "old-detach")) { c.eventOrder }  // attach-before-detach
            expect(2) { c.uiCountDuringNewInit }                       // both briefly live
            expect(true) { c.oldUI.isClosing }
            expect(listOf(c.newUI)) { VaadinSession.getCurrent().uIs.toList() }
        }

        @Test fun `NEVER leaves the old UI alive alongside the new one`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.NEVER
            navigateTo<HelloWorldView>()
            val c = reloadAndCapture()
            expect(listOf("new-init")) { c.eventOrder }                // old never closed/detached
            expect(2) { c.uiCountDuringNewInit }
            expect(false) { c.oldUI.isClosing }
            // both UIs linger; the current one is the new UI
            expect(setOf(c.oldUI, c.newUI)) { VaadinSession.getCurrent().uIs.toSet() }
            expect(c.newUI) { UI.getCurrent() }
        }

        // --- reapInactiveUIs(): simulate the heartbeat reap of a NEVER-abandoned (lost-beacon) UI ---

        @Test fun `reapInactiveUIs closes a NEVER-abandoned UI`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.NEVER
            navigateTo<HelloWorldView>()
            val c = reloadAndCapture()
            // precondition: both UIs linger after the lost beacon.
            expect(setOf(c.oldUI, c.newUI)) { VaadinSession.getCurrent().uIs.toSet() }
            val reapOrder: MutableList<String> = mutableListOf()
            c.oldUI.addDetachListener { reapOrder.add("old-detach") }

            MockVaadin.reapInactiveUIs()

            expect(listOf("old-detach")) { reapOrder }              // detach listener fired on reap
            expect(true) { c.oldUI.isClosing }
            expect(listOf(c.newUI)) { VaadinSession.getCurrent().uIs.toList() }  // only the new UI remains
            expect(c.newUI) { UI.getCurrent() }                     // current UI untouched
        }

        @Test fun `reapInactiveUIs never reaps the current UI`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.NEVER
            navigateTo<HelloWorldView>()
            val c = reloadAndCapture()
            MockVaadin.reapInactiveUIs()
            // the current (new) UI survives even though it is not @PreserveOnRefresh.
            expect(c.newUI) { UI.getCurrent() }
            expect(false) { c.newUI.isClosing }
            expect(true) { VaadinSession.getCurrent().uIs.contains(c.newUI) }
        }

        @Test fun `reapInactiveUIs is a no-op when no beacon was lost`() {
            navigateTo<HelloWorldView>()          // EAGER default: a plain reload closes the old UI itself
            val c = reloadAndCapture()
            MockVaadin.reapInactiveUIs()
            expect(c.newUI) { UI.getCurrent() }
            expect(false) { c.newUI.isClosing }
            expect(listOf(c.newUI)) { VaadinSession.getCurrent().uIs.toList() }
        }

        // --- @PreserveOnRefresh: Flow ignores the beacon, so the timing has NO effect ---

        private fun verifyPreserveUnaffectedByTiming() {
            navigateTo<PreserveOnRefreshView>()
            val dialog = Dialog(Button("OK"))
            dialog.open()
            _expectOne<Dialog>()
            val c = reloadAndCapture()
            // preserve always creates the new UI first, then teleports overlays off the old one and
            // discards it - regardless of the beacon timing.
            expect(listOf("new-init", "old-detach")) { c.eventOrder }
            expect(c.newUI) { dialog.ui.get() }   // overlay teleported onto the new UI
            _get<Dialog>()
            expect(true) { c.oldUI.isClosing }
            expect(listOf(c.newUI)) { VaadinSession.getCurrent().uIs.toList() }
        }

        @Test fun `@PreserveOnRefresh ignores EAGER timing`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.EAGER
            verifyPreserveUnaffectedByTiming()
        }

        @Test fun `@PreserveOnRefresh ignores LATE timing`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.LATE
            verifyPreserveUnaffectedByTiming()
        }

        @Test fun `@PreserveOnRefresh ignores NEVER timing`() {
            KaribuConfig.unloadBeaconTiming = UnloadBeaconTiming.NEVER
            verifyPreserveUnaffectedByTiming()
        }
    }

    @Nested inner class ExtendedClientDetailsTests {
        @BeforeEach @AfterEach fun resetFakeExtendedClientDetails() { KaribuConfig.fakeExtendedClientDetails = true }

        @Test fun `proper retrieval`() {
            // by default they're null but a mock one can be retrieved.
            expect(false) { UI.getCurrent().internals.extendedClientDetails.initialized }
            var ecd: ExtendedClientDetails? = null
            UI.getCurrent().page.retrieveExtendedClientDetails { ecd = it }

            // the ECD is not retrieved yet: we need to do this asynchronously
            // See https://github.com/mvysny/karibu-testing/issues/184#issuecomment-2639789774
            expect(null) { ecd }
            MockVaadin.clientRoundtrip()
            // now "ecd" is populated.
            expect(true) { ecd != null }
            expect(false) { ecd!!.isTouchDevice }
            expect(ecd) { UI.getCurrent().internals.extendedClientDetails }
        }

        @Test fun `double retrieval doesn't create new ECD instances`() {
            // by default ECD is null but a mock one can be retrieved.
            expect(false) { UI.getCurrent().internals.extendedClientDetails.initialized }
            var ecd: ExtendedClientDetails? = null
            UI.getCurrent().page.retrieveExtendedClientDetails { ecd = it }
            MockVaadin.clientRoundtrip()
            checkNotNull(ecd)

            // now try the second time. The retriever must not be called since the ECD
            // is already retrieved.
            var ecd2: ExtendedClientDetails? = null
            UI.getCurrent().page.retrieveExtendedClientDetails { ecd2 = it }
            // the closure must be run right away, and ecd2 must be populated.
            expect(true) { ecd2 === ecd }
        }

        @Test fun `nothing is fetched when fakeExtendedClientDetails=false`() {
            KaribuConfig.fakeExtendedClientDetails = false
            expect(false) { UI.getCurrent().internals.extendedClientDetails.initialized }
            UI.getCurrent().page.retrieveExtendedClientDetails {
                fail("shouldn't be called")
            }
            MockVaadin.clientRoundtrip()
            expect(false) { UI.getCurrent().internals.extendedClientDetails.initialized }
        }

        @Test fun `view is created with ECD already populated`() {
            MockVaadin.tearDown()
            var routeCreated = false
            MockVaadin.setup(routes, uiFactory = {
                object : MockedUI() {
                    override fun init(request: VaadinRequest) {
                        page.retrieveExtendedClientDetails {}
                        addBeforeEnterListener {
                            routeCreated = true
                            val ecd = currentUI.internals.extendedClientDetails
                            checkNotNull(ecd)
                        }
                    }
                }
            })
            expect(true) { routeCreated }
        }
    }

    @Test fun `VaadinSession-close() must re-create the entire session and the UI`() {
        val ui = UI.getCurrent()
        var detachCalled = false
        ui.addDetachListener { detachCalled = true }
        val session = VaadinSession.getCurrent()
        session.setAttribute("foo", "bar")
        session.close()

        // a new UI+Session must be created
        expect(true) { UI.getCurrent() != null }
        expect(true) { VaadinSession.getCurrent() != null }
        expect(false) { UI.getCurrent() === ui }
        expect(false) { VaadinSession.getCurrent() === session }
        // the old UI must be detached properly
        expect(true) { detachCalled }
        // the new session must not inherit attributes from the old one
        expect(null) { VaadinSession.getCurrent().getAttribute("foo") }
    }


    @Test fun `Reusing UI fails with helpful message`() {
        val ui = MockedUI()
        MockVaadin.setup(uiFactory = { ui })
        expectThrows(IllegalArgumentException::class, "which is already attached to a Session") {
            MockVaadin.setup(uiFactory = { ui })
        }
    }

    @Nested inner class async : AbstractAsyncTests()

    @Nested inner class `init listener` {
        @BeforeEach fun setupWithInitListener() {
            MockVaadin.tearDown()
            TestInitListener.clearInitFlags()
            MockVaadin.setup(routes)
        }
        @Test fun `init listeners called`() {
            expect(true) { TestInitListener.serviceInitCalled }
            expect(true) { TestInitListener.uiInitCalled }
            expect(true) { TestInitListener.uiBeforeEnterCalled }
        }
    }

    @Nested inner class request {
        @Test fun cookies() {
            currentRequest.mock.addCookie(Cookie("foo", "bar"))
            expectList("bar") { currentRequest.cookies!!.map { it.value } }
        }
    }

    @Nested inner class response {
        @Test fun cookies() {
            currentResponse.addCookie(Cookie("foo", "bar"))
            expect("bar") { currentResponse.mock.getCookie("foo").value }
        }

        @Test fun `cookies in UI-init()`() {
            MockVaadin.tearDown()
            var initCalled = false
            MockVaadin.setup(uiFactory = {
                currentRequest.mock.addCookie(Cookie("foo", "bar"))
                object : UI() {
                    override fun init(request: VaadinRequest) {
                        expectList("bar") { currentRequest.cookies!!.map { it.value } }
                        initCalled = true
                    }
                }
            })
            expect(true) { initCalled }
        }
    }

    @Nested inner class session {
        @Test fun attributes() {
            VaadinSession.getCurrent().session.setAttribute("foo", "bar")
            expect("bar") { VaadinSession.getCurrent().mock.getAttribute("foo") }
        }
        @Test fun reinitializeSession() {
            var id = VaadinSession.getCurrent().session.id
            VaadinSession.getCurrent().session.setAttribute("foo", "bar")
            expect(true) { VaadinSession.getCurrent().hasLock() }

            VaadinService.reinitializeSession(VaadinRequest.getCurrent())
            // test that attributes are preserved
            expect("bar") { VaadinSession.getCurrent().session.getAttribute("foo") }
            expect(true) { id != VaadinSession.getCurrent().session.id }
            expect(true) { VaadinSession.getCurrent().hasLock() }

            id = VaadinSession.getCurrent().session.id
            // reinitialize again
            VaadinService.reinitializeSession(VaadinRequest.getCurrent())
            // test that attributes are preserved
            expect("bar") { VaadinSession.getCurrent().session.getAttribute("foo") }
            expect(true) { id != VaadinSession.getCurrent().session.id }
            expect(true) { VaadinSession.getCurrent().hasLock() }
        }
    }

    @Nested inner class `multiple threads` {
        // don't extract this into a testBatch method - references 'lateinit routes'
        @Test fun `UIs-Sessions not reused between threads`() {
            fun newVaadinThread(): Pair<UI, VaadinSession> {
                val uiref = AtomicReference<UI>()
                val sessionref = AtomicReference<VaadinSession>()
                thread {
                    MockVaadin.setup()
                    uiref.set(UI.getCurrent())
                    sessionref.set(VaadinSession.getCurrent())
                }.join()
                return uiref.get()!! to sessionref.get()!!
            }

            val pair1 = newVaadinThread()
            val pair2 = newVaadinThread()
            expect(false) { pair1.first == pair2.first }
            expect(false) { pair1.second == pair2.second }
        }
        @Test fun `executor example`() {
            // a simple service which only counts the number of calls
            class MyService {
                private var count = 0
                private val lock = ReentrantReadWriteLock()

                fun callService() {
                    lock.write { Thread.sleep(10); count++ }
                }

                fun getCount(): Int = lock.read { count }
            }

            val service = MyService()

            // an ExecutorService which configures Vaadin for every thread created.
            val e: ExecutorService = Executors.newFixedThreadPool(4) { runnable ->
                Thread {
                    MockVaadin.setup(routes)
                    runnable.run()
                    MockVaadin.tearDown()
                }
            }

            try {
                // submit a task to all threads
                repeat(4) {
                    e.submit {
                        try {
                            UI.getCurrent().navigate("helloworld")
                            _get<Button> { caption = "Hello, World!" }.onLeftClick {
                                service.callService()
                            }
                            _get<Button> { caption = "Hello, World!" }._click()
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            } finally {
                e.shutdown()
                e.awaitTermination(10, TimeUnit.SECONDS)
            }

            // make sure that every thread called the service
            expect(4) { service.getCount() }
        }

        @Nested inner class javascript {
            @AfterEach fun cleanup() {
                KaribuConfig.pendingJavascriptInvocationHandlers.clear()
            }

            @Test fun javascriptCallsGoThroughHandlers() {
                KaribuConfig.pendingJavascriptInvocationHandlers.add { it ->
                    if (it.invocation.expression.contains("return this.getBoundingClientRect();")) {
                        it.complete(ObjectMapper().nodeFactory.textNode("Success!"))
                    }
                }
                lateinit var result: JsonNode
                UI.getCurrent().button {
                    element.executeJs("return this.getBoundingClientRect();").then { result = it }
                }
                MockVaadin.clientRoundtrip()
                expect("Success!") { result.textValue() }
            }

            @Test fun javascriptHandlersCalledAutomatically() {
                KaribuConfig.pendingJavascriptInvocationHandlers.add {
                    if (it.invocation.expression.contains("return this.getBoundingClientRect();")) {
                        it.complete(ObjectMapper().nodeFactory.textNode("Success!"))
                    }
                }
                lateinit var result: JsonNode
                val btn = UI.getCurrent().button {
                    onClick {
                        element.executeJs("return this.getBoundingClientRect();")
                            .then { result = it }
                    }
                }
                btn._click()
                MockVaadin.clientRoundtrip() // still necessary
                expect("Success!") { result.textValue() }
            }
        }
    }

    @Nested inner class VaadinServiceTests {
        @Test fun `Registering custom VaadinService is possible`() {
            open class MyMockService(servlet: VaadinServlet, deploymentConfiguration: DeploymentConfiguration) : VaadinServletService(servlet, deploymentConfiguration) {
                override fun isAtmosphereAvailable(): Boolean = false
                override fun getMainDivId(session: VaadinSession, request: VaadinRequest): String = "ROOT-1"
                override fun createVaadinSession(request: VaadinRequest): VaadinSession = MockVaadinSession(this) { MockedUI() }
                private val config: DeploymentConfiguration by lazy {
                    FakeDeploymentConfiguration(super.getDeploymentConfiguration())
                }
                override fun getDeploymentConfiguration(): DeploymentConfiguration = config
            }
            MockVaadin.tearDown()
            MockVaadin.setup(servlet = object : MockVaadinServlet(routes) {
                override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
                    val service = MyMockService(this, deploymentConfiguration)
                    service.init()
                    return service
                }
            })
            expect<Class<*>>(MyMockService::class.java) { VaadinService.getCurrent().javaClass }
        }
        @Test fun `VaadinService listeners should be invoked`() {
            MockVaadin.tearDown()
            var sessionInitListenerInvocationCount = 0
            var uiInitListenerInvocationCount = 0
            var sessionDestroyListenerInvocationCount = 0
            var serviceDestroyListenerInvocationCount = 0
            MockVaadin.setup(servlet = object : MockVaadinServlet(routes) {
                override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
                    val service = MockService(this, deploymentConfiguration)
                    service.init()
                    service.addSessionInitListener { sessionInitListenerInvocationCount++ }
                    service.addUIInitListener { uiInitListenerInvocationCount++ }
                    service.addSessionDestroyListener { sessionDestroyListenerInvocationCount++ }
                    service.addServiceDestroyListener { serviceDestroyListenerInvocationCount++ }
                    return service
                }
            })
            expect(1) { sessionInitListenerInvocationCount }
            expect(1) { uiInitListenerInvocationCount }
            expect(0) { sessionDestroyListenerInvocationCount }
            expect(0) { serviceDestroyListenerInvocationCount }
            MockVaadin.tearDown()
            expect(1) { sessionInitListenerInvocationCount }
            expect(1) { uiInitListenerInvocationCount }
            expect(1) { sessionDestroyListenerInvocationCount }
            expect(1) { serviceDestroyListenerInvocationCount }
        }
    }
}

@Route("params")
class ParametrizedView : VerticalLayout(), HasUrlParameter<Int> {
    var parameter: Int? = null
    lateinit var qp: QueryParameters
    override fun setParameter(event: BeforeEvent, parameter: Int?) {
        this.parameter = parameter!!
        qp = event.location.queryParameters
    }
}

@Route("helloworld")
class HelloWorldView : VerticalLayout() {
    init {
        button("Hello, World!")
    }
}

@Route("")
@PWA(name = "My Foo PWA", shortName = "Foo PWA")
@AnonymousAllowed
class WelcomeView : VerticalLayout() {
    init {
        width = null
        text("Welcome!")
    }
}

@Route("child", layout = ParentView::class)
class ChildView : VerticalLayout()

@RoutePrefix("parent")
class ParentView : VerticalLayout(), RouterLayout

@Route("preserveonrefresh")
@PreserveOnRefresh
class PreserveOnRefreshView : VerticalLayout()

/**
 * Captures what an F5 [com.vaadin.flow.component.page.Page.reload] did: the ordering of old-UI
 * detach vs. new-UI init (as a list of `"old-detach"` / `"new-init"` markers), and the number of
 * UIs live in the session at the moment the new UI was initialized.
 */
private class ReloadCapture(
    val oldUI: UI,
    val newUI: UI,
    val eventOrder: List<String>,
    val uiCountDuringNewInit: Int,
)
