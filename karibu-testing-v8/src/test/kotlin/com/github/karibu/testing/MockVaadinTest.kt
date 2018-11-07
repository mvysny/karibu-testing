package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.autoDiscoverViews
import com.github.vok.karibudsl.autoViewProvider
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.PushStateNavigation
import com.vaadin.navigator.View
import com.vaadin.server.*
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import java.lang.IllegalArgumentException
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
            expect(false) { VaadinSession.getCurrent().configuration.isProductionMode }
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

            test("runUIQueue() processes all access() calls") {
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
                MockVaadin.runUIQueue()
                expect(4) { calledCount }
            }

            test("runUIQueue() propagates failures") {
                UI.getCurrent().access { throw java.lang.RuntimeException("simulated") }
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
            fun runInBgSyncOnUI(block: UI.()->Unit) {
                val ui = UI.getCurrent()
                executor.submit { block(ui) }.get()
            }

            test("calling access() won't throw exception but the block won't be called immediately because the tests hold UI lock") {
                runInBgSyncOnUI { access { fail("Shouldn't be called now") } }
            }

            test("runUIQueue() processes all access() calls") {
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
                    }
                }
                MockVaadin.runUIQueue()
            }
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