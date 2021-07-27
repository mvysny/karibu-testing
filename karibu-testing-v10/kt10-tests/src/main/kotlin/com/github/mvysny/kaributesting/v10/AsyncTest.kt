package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.expect
import kotlin.test.fail

internal fun DynaNodeGroup.asyncTestbatch() {
    group("from UI thread") {
        test("calling access() won't throw exception but the block won't be called immediately") {
            var checkAccess = true
            UI.getCurrent().access { if (checkAccess) fail("Shouldn't be called now") }
            checkAccess = false
        }

        test("calling accessSynchronously() calls the block immediately because the tests hold UI lock") {
            var called = false
            UI.getCurrent().accessSynchronously { called = true }
            expect(true) { called }
        }

        test("_get() processes access()") {
            var called = false
            UI.getCurrent().access { called = true }
            expect(false) { called }
            _get<UI>()
            expect(true) { called }
        }

        test("clientRoundtrip() processes all access() calls") {
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
            MockVaadin.clientRoundtrip()
            expect(4) { calledCount }
        }

        test("clientRoundtrip() propagates failures") {
            UI.getCurrent().access { throw RuntimeException("simulated") }
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
                expect(true) { VaadinResponse.getCurrent() != null }
            }
            MockVaadin.clientRoundtrip()
        }

        // https://github.com/mvysny/karibu-testing/issues/80
        test("push() does nothing but can be called") {
            UI.getCurrent().push()
            UI.getCurrent().access {
                UI.getCurrent().push()
            }
            MockVaadin.clientRoundtrip()
            UI.getCurrent().accessSynchronously {
                UI.getCurrent().push()
            }
        }
    }
    group("from bg thread") {
        lateinit var executor: ExecutorService
        beforeEach { executor = Executors.newCachedThreadPool() }
        afterEach {
            executor.shutdown()
            executor.awaitTermination(4, TimeUnit.SECONDS)
        }
        fun asyncAwait(block: (UI) -> Unit) {
            val ui = UI.getCurrent()
            executor.submit { block(ui) } .get()
        }

        test("calling access() won't throw exception but the block won't be called immediately because the tests hold UI lock") {
            asyncAwait { ui ->
                var checkAccess = true
                ui.access { if (checkAccess) fail("Shouldn't be called now") }
                checkAccess = false
            }
        }

        test("clientRoundtrip() processes all access() calls") {
            var calledCount = 0
           asyncAwait { ui ->
                ui.access(object : Command {
                    override fun execute() {
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
            asyncAwait { ui ->
                ui.access {
                    expect(true) { VaadinSession.getCurrent() != null }
                    expect(true) { VaadinService.getCurrent() != null }
                    expect(true) { VaadinRequest.getCurrent() != null }
                    expect(true) { UI.getCurrent() != null }
                    expect(true) { VaadinResponse.getCurrent() != null }
                }
            }
            MockVaadin.clientRoundtrip()
        }

        // https://github.com/mvysny/karibu-testing/issues/80
        test("push() does nothing but can be called") {
            asyncAwait { ui ->
                ui.access {
                    UI.getCurrent().push()
                }
            }
            MockVaadin.clientRoundtrip()
        }
    }
}
