package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.data.renderer.LitRenderer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractRenderers22Tests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class LitRendererTests {
        @Test fun _getPresentationValue() {
            expect("Item #25") {
                val r = LitRenderer.of<Int>("<div>\${item.foo}</div>")
                    .withProperty("foo") { "Item #$it" }
                r._getPresentationValue(25)
            }
        }
    }
}
