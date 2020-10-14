package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * A Vaadin Session with two important differences:
 *
 * * Provides a lock that's always held. This is needed order for the test methods to able to
 *   talk to Vaadin components directly, since you can only do that with a session lock held.
 * * Creates a new session when this one is closed. This is used to simulate a logout
 *   which closes the session - we need to have a new fresh session to be able to continue testing.
 *   In order to do that, simply override [close], call `super.close()` then call
 *   [MockVaadin.afterSessionClose].
 */
public open class MockVaadinSession(service: VaadinService,
                                    public val uiFactory: () -> UI
) : VaadinSession(service) {
    /**
     * We need to pretend that we have the UI lock during the duration of the test method, otherwise
     * Vaadin would complain that there is no session lock.
     * The easiest way is to simply always provide a locked lock :)
     */
    private val lock: ReentrantLock = ReentrantLock().apply { lock() }

    override fun getLockInstance(): Lock = lock
    override fun close() {
        super.close()
        MockVaadin.afterSessionClose(this, uiFactory)
    }
}