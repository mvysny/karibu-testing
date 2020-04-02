package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.cloneBySerialization
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.*
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.PushStateNavigation
import com.vaadin.navigator.View
import com.vaadin.server.*
import com.vaadin.shared.JavaScriptExtensionState
import com.vaadin.shared.ui.JavaScriptComponentState
import com.vaadin.ui.AbstractJavaScriptComponent
import com.vaadin.ui.HasComponents
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import java.lang.IllegalArgumentException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.servlet.http.Cookie
import kotlin.test.expect
import kotlin.test.fail

class MockVaadinTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("setup/tearDown tests") {
        test("Vaadin.getCurrent() returns non-null values") {
            expect(true) { VaadinSession.getCurrent() != null }
            expect(true) { VaadinService.getCurrent() != null }
            expect(true) { VaadinRequest.getCurrent() != null }
            expect(true) { UI.getCurrent() != null }
            expect(true) { VaadinResponse.getCurrent() != null }
            expect(true) { Page.getCurrent() != null }
            expect(true) { Page.getCurrent().webBrowser != null }
            expect(true) { Page.getCurrent().webBrowser.locale != null }
        }

        test("current UI contains sane values") {
            expect(true) { UI.getCurrent().locale != null }
            expect(true) { UI.getCurrent().session != null }
            expect(VaadinSession.getCurrent()) { UI.getCurrent().session }
            expect(true) { UI.getCurrent().session.session != null }
            expect(true) { UI.getCurrent().loadingIndicatorConfiguration != null }
            expect(true) { UI.getCurrent().pushConfiguration != null }
            expect(null) { UI.getCurrent().pushConnection }  // not using push
            expect(true) { UI.getCurrent().reconnectDialogConfiguration != null }
            expect(true) { UI.getCurrent().connectorTracker != null }
            expect(true) { UI.getCurrent().tooltipConfiguration != null }
            expect(true) { UI.getCurrent().notificationConfiguration != null }
            expect(true) { UI.getCurrent().page != null }
            expect(null) { UI.getCurrent().navigator }  // null by default
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
            expect(null) { Page.getCurrent() }
        }

        test("tearDown() can be called multiple times") {
            MockVaadin.tearDown()
            MockVaadin.tearDown()
            MockVaadin.tearDown()
        }

        test("Attempt to reuse UI fails with helpful message") {
            val ui = MockUI()
            MockVaadin.setup(uiFactory = { ui })
            expectThrows(IllegalArgumentException::class, "which is already attached to a Session") {
                MockVaadin.setup(uiFactory = { ui })
            }
        }
    }

    group("proper mocking") {
        test("configuration mocked as well") {
            expect(true) { VaadinSession.getCurrent().configuration.isProductionMode }
        }

        test("verifyAttachCalled") {
            var attachCalled = 0
            val vl = object : VerticalLayout() {
                override fun attach() {
                    super.attach()
                    attachCalled++
                }
            }
            vl.addAttachListener { attachCalled++ }
            UI.getCurrent().content = vl
            expect(2) { attachCalled }
            expect(true) { vl.isAttached }
        }

        test("wrapped session works") {
            VaadinSession.getCurrent().session.setAttribute("foo", "bar")
            expect("bar") { VaadinSession.getCurrent().session.getAttribute("foo") }
        }

        test("Browser should be mocked as well") {
            expect("127.0.0.1") { Page.getCurrent().webBrowser.address }
        }

        test("UI with push state and navigator won't fail") {
            MockVaadin.setup(uiFactory = { MyUIWithNavigator() })
        }
    }

    group("page reloading") {
        test("Page reload should re-create the UI") {
            val ui = UI.getCurrent()
            var detachCalled = false
            ui.addDetachListener { detachCalled = true }
            Page.getCurrent().reload()
            // a new UI must be created; but the Session must stay the same.
            expect(true) { UI.getCurrent() != null }
            expect(false) { UI.getCurrent() === ui }
            // the old UI must be detached properly
            expect(true) { detachCalled }
        }

        test("Page reload should preserve session") {
            val session = VaadinSession.getCurrent()
            session.setAttribute("foo", "bar")
            Page.getCurrent().reload()
            expect(true) { VaadinSession.getCurrent() === session }
            expect("bar") { VaadinSession.getCurrent().getAttribute("foo") }
        }

        test("Page reload should automatically navigate to the current URL") {
            MockVaadin.setup(uiFactory = { MyUIWithNavigator() })
            _get<DummyView>()
            UI.getCurrent().page.reload()
            _get<DummyView>()
            UI.getCurrent().navigator.navigateTo("2")
            _expectNone<DummyView>()
            _get<DummyView2>()
            UI.getCurrent().page.reload()
            _expectNone<DummyView>()
            _get<DummyView2>()
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

    test("if autoViewProvider is used with a Navigator, display a helpful message instead of standard Vaadin one") {
        autoDiscoverViews("non.existing.package")   // should remove all views

        expectThrows(RuntimeException::class, "UI failed to initialize. If you're using autoViewProvider, make sure that views are auto-discovered via autoDiscoverViews()") {
            MockVaadin.setup({ MyUIWithAutoViewProvider() })
        }
    }

    group("async") {
        group("from UI thread") {
            test("calling access() won't throw exception but the block won't be called immediately") {
                UI.getCurrent().access { fail("Shouldn't be called now") }
            }

            test("calling accessSynchronously() calls the block immediately because the tests hold UI lock") {
                var called = false
                UI.getCurrent().accessSynchronously { called = true }
                expect(true) { called }
            }

            test("clientRoundtrip() processes even calls scheduled in the access() itself") {
                var calledCount = 0
                UI.getCurrent().access(object : Runnable {
                    override fun run() {
                        if (calledCount < 4) {
                            calledCount++
                            UI.getCurrent().access(this)
                        }
                    }
                })
                expect(0) { calledCount }
                MockVaadin.clientRoundtrip()
                expect(4) { calledCount }
            }

            test("_get() processes access()") {
                var called = false
                UI.getCurrent().access(object : Runnable {
                    override fun run() {
                        called = true
                    }
                })
                expect(false) { called }
                _get<UI>()
                expect(true) { called }
            }

            test("clientRoundtrip() propagates failures") {
                UI.getCurrent().access { throw java.lang.RuntimeException("simulated") }
                expectThrows(ExecutionException::class, "simulated") {
                    MockVaadin.clientRoundtrip()
                }
            }

            test("access() has properly mocked instances") {
                UI.getCurrent().access {
                    expect(true) { VaadinSession.getCurrent() != null }
                    expect(true) { VaadinService.getCurrent() != null }
                    expect(true) { VaadinRequest.getCurrent() != null }
                    expect(true) { UI.getCurrent() != null }
                }
                MockVaadin.clientRoundtrip()
            }
        }
        group("from bg thread") {
            lateinit var executor: ExecutorService
            beforeEach { executor = Executors.newCachedThreadPool() }
            afterEach {
                executor.shutdown()
                executor.awaitTermination(4, TimeUnit.SECONDS)
            }
            fun runInBgSyncOnUI(block: UI.()->Unit) {
                val ui = UI.getCurrent()
                executor.submit { block(ui) }.get()
            }

            test("calling access() won't throw exception but the block won't be called immediately because the tests hold UI lock") {
                runInBgSyncOnUI { access { fail("Shouldn't be called now") } }
            }

            test("clientRoundtrip() processes all access() calls") {
                var calledCount = 0
                runInBgSyncOnUI {
                    access(object : Runnable {
                        override fun run() {
                            if (calledCount < 4) {
                                calledCount++
                                UI.getCurrent().access(this)
                            }
                        }
                    })
                }
                expect(0) { calledCount }
                MockVaadin.clientRoundtrip()
                expect(4) { calledCount }
            }

            test("access() has properly mocked instances") {
                runInBgSyncOnUI {
                    access {
                        expect(true) { VaadinSession.getCurrent() != null }
                        expect(true) { VaadinService.getCurrent() != null }
                        expect(true) { VaadinRequest.getCurrent() != null }
                        expect(true) { UI.getCurrent() != null }
                    }
                }
                MockVaadin.clientRoundtrip()
            }
        }
    }

    // test that Vaadin extensions work
    group("extensions") {
        test("attach to Button") {
            UI.getCurrent().button {
                MyExtension(this).text = "foo"
                val e = extensions.filterIsInstance<MyExtension>().first()
                expect("foo") { e.text }
                e.remove()
                expectList() { extensions.toList() }
            }
        }
        test("attach to UI") {
            MyExtension(UI.getCurrent()).text = "foo"
            val e = UI.getCurrent().extensions.filterIsInstance<MyExtension>().first()
            expect("foo") { e.text }
            e.remove()
            expectList() { UI.getCurrent().extensions.toList() }
        }
    }

    group("javascript component") {
        test("attach to UI") {
            UI.getCurrent().myComponent {
                text = "bar"
                expect("bar") { text }
            }
        }
    }

    group("request") {
        test("cookies") {
            currentRequest.mock.addCookie(Cookie("foo", "bar"))
            expectList("bar") { currentRequest.cookies!!.map { it.value } }
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

    group("response") {
        test("cookies") {
            currentResponse.addCookie(Cookie("foo", "bar"))
            expect("bar") { currentResponse.mock.getCookie("foo").value }
        }
    }

    group("session") {
        test("attributes") {
            VaadinSession.getCurrent().session.setAttribute("foo", "bar")
            expect("bar") { VaadinSession.getCurrent().mock.getAttribute("foo") }
        }
    }
})

@PushStateNavigation
class MyUIWithNavigator : UI() {
    override fun init(request: VaadinRequest) {
        navigator = Navigator(this, this)
        navigator.addView("", DummyView())
        navigator.addView("2", DummyView2())
    }
}

class DummyView : VerticalLayout(), View
class DummyView2 : VerticalLayout(), View

@PushStateNavigation
class MyUIWithAutoViewProvider : UI() {
    override fun init(request: VaadinRequest) {
        navigator = Navigator(this, this)
        navigator.addProvider(autoViewProvider)
    }
}

class MyExtension(target: AbstractClientConnector) : AbstractJavaScriptExtension(target) {
    class State : JavaScriptExtensionState() {
        @JvmField var text: String = ""
    }

    override fun getState(): State = super.getState() as State
    override fun getState(markAsDirty: Boolean): State = super.getState(markAsDirty) as State
    var text: String
        get() = getState(false).text
        set(value) {
            state.text = value
        }
}

class MyComponent : AbstractJavaScriptComponent() {
    class State : JavaScriptComponentState() {
        @JvmField var text: String = ""
    }

    override fun getState(): State = super.getState() as State
    override fun getState(markAsDirty: Boolean): State = super.getState(markAsDirty) as State
    var text: String
        get() = getState(false).text
        set(value) {
            state.text = value
        }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).myComponent(block: (@VaadinDsl MyComponent).()->Unit = {}) = init(MyComponent(), block)
