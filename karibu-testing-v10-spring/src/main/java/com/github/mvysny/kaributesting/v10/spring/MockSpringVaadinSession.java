package com.github.mvysny.kaributesting.v10.spring;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;

/**
 * A Vaadin Session with one important difference:
 *
 * <ul>
 * <li>Creates a new session when this one is closed. This is used to simulate a logout
 *   which closes the session - we need to have a new fresh session to be able to continue testing.
 *   In order to do that, simply override {@link #close()}, call `super.close()` then call
 *   {@link MockVaadin#afterSessionClose}.</li>
 *   </ul>
 * @deprecated use {@link com.github.mvysny.kaributesting.v10.mock.MockVaadinSession}.
 */
@Deprecated
public class MockSpringVaadinSession extends VaadinSession {
    /**
     * Creates new UIs.
     */
    @NotNull
    private final Function0<UI> uiFactory;

    /**
     * Creates a new testing VaadinSession tied to a VaadinService.
     *
     * @param service
     *            the Vaadin service for the new session
     * @param uiFactory creates new UIs.
     */
    public MockSpringVaadinSession(@NotNull VaadinService service, @NotNull Function0<UI> uiFactory) {
        super(service);
        this.uiFactory = uiFactory;
    }

    @Override
    public void close() {
        super.close();
        MockVaadin.afterSessionClose(this, uiFactory);
    }
}
