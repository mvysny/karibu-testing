package com.github.karibu.testing.v10

import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener

class TestInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        serviceInitCalled = true
        event.source.addUIInitListener { e ->
            uiInitCalled = true
        }
    }

    companion object {
        var serviceInitCalled: Boolean = false
        var uiInitCalled = false
        fun clearInitFlags() {
            serviceInitCalled = false
            uiInitCalled = false
        }
    }
}
