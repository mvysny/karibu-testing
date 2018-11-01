package com.github.karibu.testing.v10

import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener

var serviceInitCalled: Boolean = false
fun clearInitFlags() {
    serviceInitCalled = false
}

class TestInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        serviceInitCalled = true
    }
}
