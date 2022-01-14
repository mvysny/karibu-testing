package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.dom.DomEvent
import elemental.json.Json

@DynaTestDsl
internal fun DynaNodeGroup.elementUtilsTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("fireDomEvent() smoke") {
        val element = Div().element
        element._fireDomEvent(DomEvent(element, "click", Json.createObject()))
    }
}
