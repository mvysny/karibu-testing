package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractRenderersTests {
    @Test fun _getPresentationValue() {
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
    }
}
