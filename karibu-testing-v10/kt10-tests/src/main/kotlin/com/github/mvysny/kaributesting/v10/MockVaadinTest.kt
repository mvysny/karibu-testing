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
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
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

    @Nested inner class ExtendedClientDetailsTests {
        @BeforeEach @AfterEach fun resetFakeExtendedClientDetails() { fakeExtendedClientDetails = true }

        @Test fun `proper retrieval`() {
            // by default they're null but a mock one can be retrieved.
            expect(null) { UI.getCurrent().internals.extendedClientDetails }
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
            expect(null) { UI.getCurrent().internals.extendedClientDetails }
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
            fakeExtendedClientDetails = false
            expect(null) { UI.getCurrent().internals.extendedClientDetails }
            UI.getCurrent().page.retrieveExtendedClientDetails {
                fail("shouldn't be called")
            }
            MockVaadin.clientRoundtrip()
            expect(null) { UI.getCurrent().internals.extendedClientDetails }
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
                pendingJavascriptInvocationHandlers.clear()
            }

            @Test fun javascriptCallsGoThroughHandlers() {
                pendingJavascriptInvocationHandlers.add { it ->
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
                pendingJavascriptInvocationHandlers.add { it ->
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
