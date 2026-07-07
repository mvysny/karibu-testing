package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import kotlin.test.expect

/**
 * This class is picked up automatically by Vaadin (since it's registered via META-INF/services). We then test elsewhere
 * that MockVaadin-mocked env indeed picked up this init listener and executed it.
 */
class TestInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        serviceInitCalled = true
        event.source.addUIInitListener { e ->
            uiInitCalled = true
            // assert that the ECD is not yet populated.
            expect(false) { e.ui.internals.extendedClientDetails.initialized }
            e.ui.addBeforeEnterListener { uiBeforeEnterCalled = true }
        }
    }

    companion object {
        var serviceInitCalled: Boolean = false
        var uiInitCalled = false
        var uiBeforeEnterCalled = false
        fun clearInitFlags() {
            serviceInitCalled = false
            uiInitCalled = false
            uiBeforeEnterCalled = false
        }
    }
}
