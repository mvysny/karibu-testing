package com.github.mvysny.kaributesting.v10.spring;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringVaadinSession;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Vaadin Session with two important differences:
 *
 * <ul>
 *     <li>Provides a lock that's always held. This is needed order for the test methods to able to
 *   talk to Vaadin components directly, since you can only do that with a session lock held.</li>
 * <li>Creates a new session when this one is closed. This is used to simulate a logout
 *   which closes the session - we need to have a new fresh session to be able to continue testing.
 *   In order to do that, simply override {@link #close()}, call `super.close()` then call
 *   {@link MockVaadin#afterSessionClose}.</li>
 *   </ul>
 */
public class MockSpringVaadinSession extends SpringVaadinSession {
    /**
     * We need to pretend that we have the UI lock during the duration of the test method, otherwise
     * Vaadin would complain that there is no session lock.
     * The easiest way is to simply always provide a locked lock :)
     */
    @NotNull
    private final ReentrantLock lock = new ReentrantLock();

    @NotNull
    private final Function0<UI> uiFactory;

    public MockSpringVaadinSession(@NotNull VaadinService service, @NotNull Function0<UI> uiFactory) {
        super(service);
        this.uiFactory = uiFactory;
        lock.lock();
    }

    @Override
    public Lock getLockInstance() {
        return lock;
    }

    @Override
    public void close() {
        super.close();
        MockVaadin.afterSessionClose(this, uiFactory);
    }
}
