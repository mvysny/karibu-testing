package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.expect

class MockVaadinTest : DynaTest({
    beforeEach { MockVaadin.setup() }

    test("Vaadin.getCurrent() returns non-null values") {
        expect(true) { VaadinSession.getCurrent() != null }
        expect(true) { VaadinService.getCurrent() != null }
        expect(true) { UI.getCurrent() != null }
    }

    test("verifyAttachCalled") {
        val attachCalled = AtomicInteger()
        val vl = object : VerticalLayout() {
            override fun attach() {
                super.attach()
                attachCalled.incrementAndGet()
            }
        }
        vl.addAttachListener { attachCalled.incrementAndGet() }
        UI.getCurrent().content = vl
        expect(2) { attachCalled.get() }
        expect(true) { vl.isAttached }
    }
})
