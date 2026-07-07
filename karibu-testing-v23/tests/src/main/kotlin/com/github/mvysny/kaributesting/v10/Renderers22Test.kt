package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.renderer.LitRenderer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

abstract class AbstractRenderers22Tests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class LitRendererTests {
        // conditionally emits an anchor, as in https://github.com/mvysny/karibu-testing/issues/175
        private fun linkRenderer(): LitRenderer<Int> = LitRenderer.of<Int>(
            "<span><a href='item/\${item.id}'>\${item.name}</a></span>"
        )
            .withProperty("id") { it }
            .withProperty("name") { "Item #$it" }

        @Test fun _getPresentationValue() {
            expect("Item #25") {
                val r = LitRenderer.of<Int>("<div>\${item.foo}</div>")
                    .withProperty("foo") { "Item #$it" }
                r._getPresentationValue(25)
            }
        }
        @Test fun _getPresentationHtml() {
            expect("<span><a href='item/25'>Item #25</a></span>") {
                linkRenderer()._getPresentationHtml(25)
            }
        }
        @Test fun `_getPresentationJsoup allows querying the rendered markup`() {
            val body = linkRenderer()._getPresentationJsoup(25)
            expect(1) { body.select("a[href]").size }
            expect("item/25") { body.select("a").attr("href") }
            expect("Item #25") { body.select("a").text() }
        }
    }

    @Nested inner class GridColumnLitRenderer {
        private fun grid(): Grid<Int> {
            val grid = Grid<Int>()
            grid.addColumn(
                LitRenderer.of<Int>("<a href='item/\${item.id}'>\${item.id}</a>")
                    .withProperty("id") { it }
            ).setKey("link")
            // plain value-provider column: renderer is a ColumnPathRenderer, not a LitRenderer.
            grid.addColumn { "plain $it" }.setKey("plain")
            return grid
        }
        @Test fun _getPresentationHtml() {
            expect("<a href='item/25'>25</a>") {
                grid()._getColumnByKey("link")._getPresentationHtml(25)
            }
        }
        @Test fun _getPresentationJsoup() {
            expect("item/25") {
                grid()._getColumnByKey("link")._getPresentationJsoup(25).select("a").attr("href")
            }
        }
        @Test fun `fails on non-Lit column`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                grid()._getColumnByKey("plain")._getPresentationHtml(25)
            }
            expect(true, ex.message) { ex.message!!.contains("plain") }
        }
    }
}
