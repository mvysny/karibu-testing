package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.LitRenderer
import com.vaadin.flow.data.renderer.TemplateRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.renderers22Tests() {
    beforeEach { MockVaadin.setup() }

    group("LitRenderer") {
        test("_getPresentationValue()") {
            expect("Item #25") {
                val r = LitRenderer.of<Int>("<div>\${item.foo}</div>")
                    .withProperty("foo") { "Item #$it" }
                r._getPresentationValue(25)
            }
        }
    }
}

