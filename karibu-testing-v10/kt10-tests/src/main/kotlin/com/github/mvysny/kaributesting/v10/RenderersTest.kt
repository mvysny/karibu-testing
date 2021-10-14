package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TemplateRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import kotlin.test.expect

internal fun DynaNodeGroup.renderersTests() {
    test("_getPresentationValue()") {
        expect("Item #25") {
            val r =
                TextRenderer<Int> { "Item #$it" }._getPresentationValue(25)
            r
        }
        expect("Span[text='Item #25']") {
            val r =
                ComponentRenderer<Span, Int>(SerializableFunction { Span("Item #$it") })
            r._getPresentationValue(25)
        }
        expect("Item #25") {
            val r = TemplateRenderer.of<Int>("<div>[[item.foo]]</div>")
                .withProperty("foo") { "Item #$it" }
            r._getPresentationValue(25)
        }
    }
}
