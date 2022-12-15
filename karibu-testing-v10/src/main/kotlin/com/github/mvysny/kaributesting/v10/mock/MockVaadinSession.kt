package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession

/**
 * A Vaadin Session with one important difference:
 *
 * * Creates a new session when this one is closed. This is used to simulate a logout
 *   which closes the session - we need to have a new fresh session to be able to continue testing.
 *   In order to do that, simply override [close], call `super.close()` then call
 *   [MockVaadin.afterSessionClose].
 */
public open class MockVaadinSession(service: VaadinService,
                                    public val uiFactory: () -> UI
) : VaadinSession(service) {
    override fun close() {
        super.close()
        MockVaadin.afterSessionClose(this, uiFactory)
    }
}
