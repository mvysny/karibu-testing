package com.github.karibu.testing.v10

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.flow.button
import com.github.vok.karibudsl.flow.text
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.*
import com.vaadin.flow.server.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.expect
import kotlin.test.fail

class MockVaadinTest : DynaTest({

    beforeEach {
        MockVaadin.setup(Routes().autoDiscoverViews("com.github"))
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
            expect(true) { VaadinResponse.getCurrent() != null }
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
            var attachCalled = 0
            val vl = object : VerticalLayout() {
                override fun onAttach(attachEvent: AttachEvent?) {
                    super.onAttach(attachEvent)
                    attachCalled++
                }
            }
            vl.addAttachListener { attachCalled++ }
            UI.getCurrent().add(vl)
            expect(2) { attachCalled }
            expect(true) { vl.isAttached }
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
            expect("helloworld") { UI.getCurrent().router.getUrl(HelloWorldView::class.java) }
            expect("params/1") { UI.getCurrent().router.getUrl(ParametrizedView::class.java, 1) }
            expect("parent/child") { UI.getCurrent().router.getUrl(ChildView::class.java) }
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
        group("from UI thread") {
            test("calling access() won't throw exception but the block won't be called immediately") {
                UI.getCurrent().access { fail("Shouldn't be called now") }
            }

            test("calling accessSynchronously() calls the block immediately because the tests hold UI lock") {
                var called = false
                UI.getCurrent().accessSynchronously { called = true }
                expect(true) { called }
            }

            test("runUIQueue() processes all access() calls") {
                var calledCount = 0
                UI.getCurrent().access(object : Command {
                    override fun execute() {
                        if (calledCount < 4) {
                            calledCount++
                            UI.getCurrent().access(this)
                        }
                    }
                })
                expect(0) { calledCount }
                MockVaadin.runUIQueue()
                expect(4) { calledCount }
            }

            test("runUIQueue() propagates failures") {
                UI.getCurrent().access { throw RuntimeException("simulated") }
                expectThrows(ExecutionException::class, "simulated") {
                    MockVaadin.runUIQueue()
                }
            }

            test("access() has properly mocked instances") {
                UI.getCurrent().access {
                    expect(true) { VaadinSession.getCurrent() != null }
                    expect(true) { VaadinService.getCurrent() != null }
                    expect(true) { VaadinRequest.getCurrent() != null }
                    expect(true) { UI.getCurrent() != null }
                    expect(true) { VaadinResponse.getCurrent() != null }
                }
                MockVaadin.runUIQueue()
            }
        }
        group("from bg thread") {
            lateinit var executor: ExecutorService
            beforeEach { executor = Executors.newCachedThreadPool() }
            afterEach {
                executor.shutdown()
                executor.awaitTermination(4, TimeUnit.SECONDS)
            }
            fun runInBgSyncOnUI(block: UI.() -> Unit) {
                val ui = UI.getCurrent()
                executor.submit { block(ui) }.get()
            }

            test("calling access() won't throw exception but the block won't be called immediately because the tests hold UI lock") {
                runInBgSyncOnUI { access { fail("Shouldn't be called now") } }
            }

            test("runUIQueue() processes all access() calls") {
                var calledCount = 0
                runInBgSyncOnUI {
                    access(object : Command {
                        override fun execute() {
                            if (calledCount < 4) {
                                calledCount++
                                UI.getCurrent().access(this)
                            }
                        }
                    })
                }
                expect(0) { calledCount }
                MockVaadin.runUIQueue()
                expect(4) { calledCount }
            }

            test("access() has properly mocked instances") {
                runInBgSyncOnUI {
                    access {
                        expect(true) { VaadinSession.getCurrent() != null }
                        expect(true) { VaadinService.getCurrent() != null }
                        expect(true) { VaadinRequest.getCurrent() != null }
                        expect(true) { UI.getCurrent() != null }
                        expect(true) { VaadinResponse.getCurrent() != null }
                    }
                }
                MockVaadin.runUIQueue()
            }
        }
    }

    group("init listener") {
        beforeEach {
            MockVaadin.tearDown()
            TestInitListener.clearInitFlags()
            MockVaadin.setup(Routes().autoDiscoverViews("com.github"))
        }
        test("init listeners called") {
            expect(true) { TestInitListener.serviceInitCalled }
            expect(true) { TestInitListener.uiInitCalled }
            expect(true) { TestInitListener.uiBeforeEnterCalled }
        }
    }
})

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
