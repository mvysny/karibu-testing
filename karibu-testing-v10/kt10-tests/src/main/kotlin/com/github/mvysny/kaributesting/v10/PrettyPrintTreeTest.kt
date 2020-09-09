package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect
import kotlin.test.fail

internal fun DynaNodeGroup.prettyPrintTreeTest() {
    lateinit var routes: Routes
    beforeGroup { routes = Routes().autoDiscoverViews("com.github") }
    beforeEach { MockVaadin.setup(routes) }
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
        expect("Div[INVIS]") { Div().apply { isVisible = false }.toPrettyString() }
        expect("TextField[#25, value='']") {
            TextField().apply { id_ = "25" }.toPrettyString()
        }
        expect("Button[caption='click me']") { Button("click me").toPrettyString() }
        expect("TextArea[label='label', value='some text']") { TextArea("label").apply { value = "some text" }.toPrettyString() }
        expect("Grid[<String>, dataprovider='ListDataProvider2{0 items}']") { Grid<String>(String::class.java).apply { setItems2(listOf()) }.toPrettyString() }
        expect("Column[header='My Header']") {
            Grid<Any>().run { addColumn { it }.apply { header2 = "My Header" } }.toPrettyString()
        }
        expect("Anchor[href='']") { Anchor().toPrettyString() }
        expect("Anchor[href='vaadin.com']") { Anchor("vaadin.com").toPrettyString() }
        expect("Image[src='']") { Image().toPrettyString() }
        expect("Image[src='vaadin.com']") { Image("vaadin.com", "").toPrettyString() }
        expect("TextField[#25, value='', errorMessage='failed validation']") {
            TextField().apply { id_ = "25"; errorMessage = "failed validation" }.toPrettyString()
        }
        expect("Icon[icon='vaadin:abacus']") { VaadinIcon.ABACUS.create().toPrettyString() }
        expect("Button[icon='vaadin:abacus']") { Button(VaadinIcon.ABACUS.create()).toPrettyString() }
        expect("FormItem[caption='foo']") { FormLayout().addFormItem(TextField(), "foo").toPrettyString() }
        expect("Html[<b>foo bar baz <i>foobar</i></b>]") {
            Html("\n    <b>foo\nbar\n    baz\n<i>foobar</i></b>").toPrettyString()
        }
    }

    test("menu dump") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("menu") {
                    isEnabled = false
                    item("click me", { _ -> fail("shouldn't be called") })
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

    group("grid") {
        test("column headers") {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                addColumn(karibuDslI18n).setHeader("Hello!")
                setItems2(listOf())
            }
            expect("""
└── Grid[<String>, dataprovider='ListDataProvider2{0 items}']
    └── Column[header='Hello!']
""".trim()) { grid.toPrettyTree().trim() }
        }

        test("grid menu dump") {
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("menu") {
                        isEnabled = false
                        item("click me", { _ -> fail("shouldn't be called") })
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

        // tests https://github.com/mvysny/karibu-testing/issues/37
        test("grid filters dump") {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                val col: Grid.Column<String> = addColumn(karibuDslI18n)
                appendHeaderRow().getCell(col).setComponent(TextField("Filter:"))
                setItems2(listOf())
            }
            expect("""
└── Grid[<String>, dataprovider='ListDataProvider2{0 items}']
    └── Column[]
        └── TextField[label='Filter:', value='']""".trim()) { grid.toPrettyTree().trim() }
        }

        // tests https://github.com/mvysny/karibu-testing/issues/37
        test("grid filters dump for joined column") {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                val col1: Grid.Column<String> = addColumn(karibuDslI18n).setHeader("foo")
                val col2: Grid.Column<String> = addColumn(karibuDslI18n).setHeader("bar")
                appendHeaderRow()
                prependHeaderRow().join(col1, col2).setComponent(TextField("Filter:"))
                setItems2(listOf())
            }
            expect("""
└── Grid[<String>, dataprovider='ListDataProvider2{0 items}']
    └── ColumnGroup[]
        ├── ColumnGroup[]
        │   └── Column[header='foo']
        │       └── TextField[label='Filter:', value='']
        └── ColumnGroup[]
            └── Column[header='bar']
                └── TextField[label='Filter:', value='']""".trim()) { grid.toPrettyTree().trim() }
        }
    }
}
