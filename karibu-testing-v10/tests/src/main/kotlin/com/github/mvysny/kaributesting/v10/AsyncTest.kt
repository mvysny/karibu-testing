package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.SemanticVersion
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractAsyncTests() {
    @Nested inner class `from UI thread` {
        @Test fun `calling access() won't throw exception but the block won't be called immediately`() {
            var checkAccess = true
            UI.getCurrent().access { if (checkAccess) fail("Shouldn't be called now") }
            checkAccess = false
        }

        @Test fun `calling accessSynchronously() calls the block immediately because the tests hold UI lock`() {
            var called = false
            UI.getCurrent().accessSynchronously { called = true }
            expect(true) { called }
        }

        @Test fun `_get() processes access()`() {
            var called = false
            UI.getCurrent().access { called = true }
            expect(false) { called }
            _get<UI>()
            expect(true) { called }
        }

        @Test fun `clientRoundtrip() processes all access() calls`() {
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

        @Test fun `clientRoundtrip() propagates failures`() {
            UI.getCurrent().access { throw RuntimeException("simulated") }
            val ex = assertThrows<ExecutionException> {
                MockVaadin.clientRoundtrip()
            }
            expect("java.lang.RuntimeException: simulated") { ex.message }
        }

        @Test fun `access() has properly mocked instances`() {
            UI.getCurrent().access {
                expect(true) { VaadinSession.getCurrent() != null }
                expect(true) { VaadinService.getCurrent() != null }
                expect(true) { UI.getCurrent() != null }
                // technically.... these guys can be null since there's no request in bg thread...
                if (VaadinVersion.get <= SemanticVersion(24, 5, 0)) {
                    expect(true) { VaadinRequest.getCurrent() != null }
                    expect(true) { VaadinResponse.getCurrent() != null }
                } else {
                    expect(null) { VaadinRequest.getCurrent() }
                    expect(null) { VaadinResponse.getCurrent() }
                }
            }
            MockVaadin.clientRoundtrip()
        }

        // https://github.com/mvysny/karibu-testing/issues/80
        @Test fun `push() does nothing but can be called`() {
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
    @Nested inner class `from bg thread` {
        lateinit var executor: ExecutorService
        @BeforeEach fun startExecutor() { executor = Executors.newCachedThreadPool() }
        @AfterEach fun stopExecutor() {
            executor.shutdown()
            executor.awaitTermination(4, TimeUnit.SECONDS)
        }
        fun asyncAwait(block: (UI) -> Unit) {
            val ui = UI.getCurrent()
            executor.submit { block(ui) } .get()
        }

        @Test fun `calling access() won't throw exception but the block won't be called immediately because the tests hold UI lock`() {
            asyncAwait { ui ->
                var checkAccess = true
                ui.access { if (checkAccess) fail("Shouldn't be called now") }
                checkAccess = false
            }
        }

        @Test fun `clientRoundtrip() processes all access() calls`() {
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

        @Test fun `access() has properly mocked instances`() {
            asyncAwait { ui ->
                ui.access {
                    expect(true) { VaadinSession.getCurrent() != null }
                    expect(true) { VaadinService.getCurrent() != null }
                    expect(true) { UI.getCurrent() != null }
                    // technically.... these guys can be null since there's no request in bg thread...
                    if (VaadinVersion.get <= SemanticVersion(24, 5, 0)) {
                        expect(true) { VaadinRequest.getCurrent() != null }
                        expect(true) { VaadinResponse.getCurrent() != null }
                    } else {
                        expect(null) { VaadinRequest.getCurrent() }
                        expect(null) { VaadinResponse.getCurrent() }
                    }
                }
            }
            MockVaadin.clientRoundtrip()
        }

        // https://github.com/mvysny/karibu-testing/issues/80
        @Test fun `push() does nothing but can be called`() {
            asyncAwait { ui ->
                ui.access {
                    UI.getCurrent().push()
                }
            }
            MockVaadin.clientRoundtrip()
        }
    }
}
