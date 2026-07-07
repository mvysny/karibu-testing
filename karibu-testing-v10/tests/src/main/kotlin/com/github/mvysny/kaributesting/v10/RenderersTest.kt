package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import com.vaadin.flow.function.SerializableFunction
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractRenderersTests {
    @Nested inner class getPresentationValue {
        @Test fun onTextRenderer() {
            expect("Item #25") {
                val r = TextRenderer<Int> { "Item #$it" }._getPresentationValue(25)
                r
            }
        }
        @Test fun onComponentRenderer() {
            expect("Span[text='Item #25']") {
                val r = ComponentRenderer<Span, Int>(SerializableFunction { Span("Item #$it") })
                r._getPresentationValue(25)
            }
        }
        @Test fun `return empty string when ComponentRenderer returns null`() {
            expect("") {
                val r = ComponentRenderer<Span, Int>(SerializableFunction { null })
                r._getPresentationValue(25)
            }
        }
    }
}
