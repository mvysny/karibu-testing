package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.text
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect
import kotlin.test.fail

internal fun DynaNodeGroup.prettyPrintTreeTest() {
    beforeEach { MockVaadin.setup(Routes().autoDiscoverViews("com.github")) }
    afterEach { MockVaadin.tearDown() }

    test("Simple dump") {
        val div = Div().apply {
            text("Foo")
        }
        expect("""
└── Div[text='Foo']
    └── Text[text='Foo']
""".trim()) { div.toPrettyTree().trim() }
    }

    test("toPrettyString()") {
        expect("Text[text='foo']") { Text("foo").toPrettyString() }
        expect("Div[INVIS]") { Div().apply { isVisible = false } .toPrettyString() }
        expect("TextField[#25, value='']") { TextField().apply { id_ = "25" } .toPrettyString() }
        expect("Button[text='click me']") { Button("click me").toPrettyString() }
        expect("TextArea[label='label', value='some text']") { TextArea("label").apply { value = "some text" } .toPrettyString() }
        expect("Grid[]") { Grid<Any>().toPrettyString() }
        expect("Column[header='My Header']") { Grid<Any>().run { addColumn { it } .apply { header2 = "My Header" } }.toPrettyString() }
    }

    test("menu dump") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("menu") {
                    isEnabled = false
                    item("click me", { e -> fail("shouldn't be called") })
                }
                item("save as")
            }
        }
        expect("""
└── ContextMenu[]
    ├── MenuItem[DISABLED, text='menu']
    │   └── MenuItem[text='click me']
    └── MenuItem[text='save as']""".trim()) { cm.toPrettyTree().trim() }

    }
    test("grid menu dump") {
        lateinit var cm: GridContextMenu<String>
        UI.getCurrent().grid<String> {
            cm = gridContextMenu {
                item("menu") {
                    isEnabled = false
                    item("click me", { e -> fail("shouldn't be called") })
                }
                item("save as")
            }
        }
        expect("""
└── GridContextMenu[]
    ├── GridMenuItem[DISABLED, text='menu']
    │   └── GridMenuItem[text='click me']
    └── GridMenuItem[text='save as']""".trim()) { cm.toPrettyTree().trim() }
    }
}
