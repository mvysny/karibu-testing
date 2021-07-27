@file:Suppress("DEPRECATION")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.cloneBySerialization
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.text
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributesting.v10.mock.MockService
import com.github.mvysny.kaributesting.v10.mock.MockVaadinServlet
import com.github.mvysny.kaributesting.v10.mock.MockVaadinSession
import com.github.mvysny.kaributesting.v10.mock.MockedUI
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.router.*
import com.vaadin.flow.server.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.servlet.http.Cookie
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write
import kotlin.test.expect

internal fun DynaNodeGroup.mockVaadinTest() {
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.github") }
    beforeEach {
        MockVaadin.setup(routes)
        expect("""
└── MockedUI[]
    └── WelcomeView[]
        └── Text[text='Welcome!']
""".trim()) { UI.getCurrent().toPrettyTree().trim() }
    }
    afterEach { MockVaadin.tearDown() }

    group("setup/teardown tests") {
        test("smoke test that everything is mocked") {
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
            expect(false) { VaadinSession.getCurrent().browser.isTooOldToFunctionProperly }
            expect(false) { VaadinSession.getCurrent().browser.isChrome }
            expect(false) { VaadinSession.getCurrent().browser.isChromeOS }
            expect(false) { VaadinSession.getCurrent().browser.isAndroid }
            expect(false) { VaadinSession.getCurrent().browser.isEdge }
            expect(true) { VaadinResponse.getCurrent() != null }
        }

        test("current UI contains sane values") {
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
            expect(true) { UI.getCurrent().router != null }
        }

        test("serializable") {
            UI.getCurrent().cloneBySerialization()
            VaadinSession.getCurrent().cloneBySerialization()
            // even that it says it's Serializable it's really not.
            // VaadinService.getCurrent().cloneBySerialization()
            // VaadinRequest.getCurrent().cloneBySerialization()
            // VaadinResponse.getCurrent().cloneBySerialization()
        }

        test("setup() can be called multiple times in a row") {
            MockVaadin.setup()
            MockVaadin.setup()
        }

        test("setup() always provides new instances") {
            MockVaadin.setup()
            val ui = UI.getCurrent()!!
            MockVaadin.setup()
            expect(true) { UI.getCurrent()!! !== ui }
        }

        test("Vaadin.getCurrent() returns null after tearDown()") {
            MockVaadin.tearDown()
            expect(null) { VaadinSession.getCurrent() }
            expect(null) { VaadinService.getCurrent() }
            expect(null) { VaadinRequest.getCurrent() }
            expect(null) { UI.getCurrent() }
            expect(null) { VaadinResponse.getCurrent() }
        }

        test("tearDown() can be called multiple times") {
            MockVaadin.tearDown()
            MockVaadin.tearDown()
            MockVaadin.tearDown()
        }
    }

    group("proper mocking") {
        test("configuration mocked as well") {
            expect(false) { VaadinSession.getCurrent().configuration.isProductionMode }
        }

        test("verifyAttachCalled") {
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
                expect(true) { vl.isAttached }
                attachCallCount++
            }
            vl.addDetachListener {
                // a bug in Vaadin? I'd expect the node to be detached (null parent etc) at this point...
                // See https://github.com/vaadin/flow/issues/8809
                expect(true) { vl.isAttached }
                detachCallCount++
            }

            // attach
            UI.getCurrent().add(vl)
            expect(2) { attachCallCount }
            expect(true) { vl.isAttached }
            expect(0) { detachCallCount }

            // close UI - detach is not called.
            UI.getCurrent().close()
            expect(2) { attachCallCount }
            expect(true) { vl.isAttached }
            expect(0) { detachCallCount }

            // detach
            vl.removeFromParent()
            expect(2) { attachCallCount }
            expect(false) { vl.isAttached }
            expect(2) { detachCallCount }
        }

        test("detach on forceful UI close") {
            val vl = UI.getCurrent().verticalLayout()
            var detachCalled = 0
            vl.addDetachListener { detachCalled++ }
            expect(true) { vl.isAttached }

            // close UI - detach is not called.
            UI.getCurrent().close()
            expect(true) { vl.isAttached }
            expect(0) { detachCalled }
            expect(true) { UI.getCurrent().isAttached }

            // Mock closing of UI after request handled
            UI.getCurrent()._close()
            expect(false) { vl.isAttached }
            expect(1) { detachCalled }
            expect(false) { UI.getCurrent().isAttached }
        }

        test("navigation works in mocked env") {
            // no need: when UI is initialized in MockVaadin.setup(), automatic navigation to "" is performed.
//        UI.getCurrent().navigate("")
            _get<Text> { text = "Welcome!" }
            UI.getCurrent().navigate("helloworld")
            _get<Button> { caption = "Hello, World!" }
        }

        test("navigation to parametrized view works in mocked env") {
            UI.getCurrent().navigate("params/1")
            _get<ParametrizedView>()
        }

        test("navigation to view with parent route works in mocked env") {
            UI.getCurrent().navigate("parent/child")
            _get<ChildView>()
        }

        test("UI.getUrl() to view works in mocked env") {
            val routeConfig = RouteConfiguration.forRegistry(UI.getCurrent().router.registry)
            expect("helloworld") { routeConfig.getUrl(HelloWorldView::class.java) }
            expect("params/1") { routeConfig.getUrl(ParametrizedView::class.java, 1) }
            expect("parent/child") { routeConfig.getUrl(ChildView::class.java) }
            expect("helloworld") { RouteConfiguration.forApplicationScope().getUrl(HelloWorldView::class.java) }
            expect("params/1") { RouteConfiguration.forApplicationScope().getUrl(ParametrizedView::class.java, 1) }
            expect("parent/child") { RouteConfiguration.forApplicationScope().getUrl(ChildView::class.java) }
        }

        // tests https://github.com/mvysny/karibu-testing/issues/11
        group("beforeClientResponse invoked") {
            test("on an UI") {
                var ran = false
                UI.getCurrent().beforeClientResponse(UI.getCurrent()) {
                    expect(false, "the block was supposed to be run only once") { ran }
                    ran = true
                }
                _get<UI> {} // do the lookup which should trigger the beforeClientResponse run
                expect(true) { ran }
            }
            test("on a button nested within the UI") {
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

    group("dialogs") {

        test("open dialog") {
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

        test("the dialogs must be cleared up from the component tree on close") {
            val dialog = Dialog(Div().apply { text("Dialog Text") })
            dialog.open()
            dialog.close()
            cleanupDialogs()
            expect(
                    """
└── MockedUI[]
    └── WelcomeView[]
        └── Text[text='Welcome!']
""".trim()
            ) { UI.getCurrent().toPrettyTree().trim() }
        }
    }

    group("page reloading") {
        test("Page reload should re-create the UI") {
            val ui = UI.getCurrent()
            var detachCalled = false
            ui.addDetachListener { detachCalled = true }
            UI.getCurrent().page.reload()
            // a new UI must be created; but the Session must stay the same.
            expect(true) { UI.getCurrent() != null }
            expect(false) { UI.getCurrent() === ui }
            // the old UI must be detached properly
            expect(true) { detachCalled }
        }

        test("Page reload should preserve session") {
            val session = VaadinSession.getCurrent()
            session.setAttribute("foo", "bar")
            UI.getCurrent().page.reload()
            expect(true) { VaadinSession.getCurrent() === session }
            expect("bar") { VaadinSession.getCurrent().getAttribute("foo") }
        }

        test("Page reload should automatically navigate to the current URL") {
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
    }

    test("VaadinSession.close() must re-create the entire session and the UI") {
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


    test("Reusing UI fails with helpful message") {
        val ui = MockedUI()
        MockVaadin.setup(uiFactory = { ui })
        expectThrows(IllegalArgumentException::class, "which is already attached to a Session") {
            MockVaadin.setup(uiFactory = { ui })
        }
    }

    group("async") {
        asyncTestbatch()
    }

    group("init listener") {
        beforeEach {
            MockVaadin.tearDown()
            TestInitListener.clearInitFlags()
            MockVaadin.setup(routes)
        }
        test("init listeners called") {
            expect(true) { TestInitListener.serviceInitCalled }
            expect(true) { TestInitListener.uiInitCalled }
            expect(true) { TestInitListener.uiBeforeEnterCalled }
        }
    }

    group("request") {
        test("cookies") {
            currentRequest.mock.addCookie(Cookie("foo", "bar"))
            expectList("bar") { currentRequest.cookies!!.map { it.value } }
        }
    }

    group("response") {
        test("cookies") {
            currentResponse.addCookie(Cookie("foo", "bar"))
            expect("bar") { currentResponse.mock.getCookie("foo").value }
        }

        test("cookies in UI.init()") {
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

    group("session") {
        test("attributes") {
            VaadinSession.getCurrent().session.setAttribute("foo", "bar")
            expect("bar") { VaadinSession.getCurrent().mock.getAttribute("foo") }
        }
    }

    group("multiple threads") {
        // don't extract this into a testBatch method - references 'lateinit routes'
        test("UIs/Sessions not reused between threads") {
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
        test("executor example") {
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
    }

    group("VaadinService") {
        test("Registering custom VaadinService is possible") {
            open class MyMockService(servlet: VaadinServlet, deploymentConfiguration: DeploymentConfiguration) : VaadinServletService(servlet, deploymentConfiguration) {
                override fun isAtmosphereAvailable(): Boolean = false
                override fun getMainDivId(session: VaadinSession, request: VaadinRequest): String = "ROOT-1"
                override fun createVaadinSession(request: VaadinRequest): VaadinSession = MockVaadinSession(this) { MockedUI() }
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
        test("VaadinService listeners should be invoked") {
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
    override fun setParameter(event: BeforeEvent, parameter: Int?) {
        parameter!!
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
