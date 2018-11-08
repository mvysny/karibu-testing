package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener

class TestInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        serviceInitCalled = true
        event.source.addUIInitListener { e ->
            uiInitCalled = true
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
