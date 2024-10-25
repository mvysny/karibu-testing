package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.dom.DomEvent
import elemental.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractElementUtilsTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `fireDomEvent() smoke`() {
        val element = Div().element
        element._fireDomEvent(DomEvent(element, "click", Json.createObject()))
    }
}
