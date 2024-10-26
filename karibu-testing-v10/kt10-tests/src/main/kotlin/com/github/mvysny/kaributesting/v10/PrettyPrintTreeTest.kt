package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.HtmlSpan
import com.github.mvysny.kaributools.header2
import com.github.mvysny.kaributools.tooltip
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
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractPrettyPrintTreeTests {
    companion object {
        lateinit var routes: Routes
        @BeforeAll @JvmStatic fun discoverRoutes() { routes = Routes().autoDiscoverViews("com.github") }
    }
    @BeforeEach fun fakeVaadin() { MockVaadin.setup(routes) }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class toPrettyTree {
        @Test fun `Simple dump`() {
            val div = Div().apply {
                text("Foo")
            }
            expect("""
                └── Div[text='Foo']
                    └── Text[text='Foo']
                """.trimIndent()) { div.toPrettyTree().trim() }
        }
        @Test fun `UI+Notification`() {
            Notification.show("foo")
            expect("""
                └── MockedUI[]
                    └── WelcomeView[@theme='padding spacing']
                        └── Text[text='Welcome!']""".trimIndent()) { currentUI.toPrettyTree().trim() }
        }
    }

    @Nested inner class toPrettyString {
        @Test fun basics() {
            expect("Text[text='foo']") { Text("foo").toPrettyString() }
            expect("Div[INVIS]") {
                Div().apply { isVisible = false }.toPrettyString()
            }
            expect("TextField[#25, value='']") {
                TextField().apply { id_ = "25" }.toPrettyString()
            }
            expect("Button[text='click me']") { Button("click me").toPrettyString() }
            expect("TextArea[label='label', value='some text']") {
                TextArea("label").apply {
                    value = "some text"
                }.toPrettyString()
            }
            expect("Grid[<String>, dataprovider='ListDataProvider<?>(0 items)']") {
                Grid<String>(String::class.java).apply {
                    setItems2(listOf())
                    _prepare()
                }.toPrettyString()
            }
            expect("Column[header='My Header', flexGrow=1]") {
                Grid<Any>().run {
                    addColumn { it }.apply {
                        header2 = "My Header"
                    }
                }.toPrettyString()
            }
            expect("Column[header='My Header', key='foo', flexGrow=1]") {
                Grid<Any>().run {
                    addColumn { it }.apply {
                        header2 = "My Header"; key = "foo"
                    }
                }.toPrettyString()
            }
            expect("Column[header='My Header', key='foo', resizable, autoWidth, sortable, flexGrow=2]") {
                Grid<Any>().run {
                    addColumn { it }.apply {
                        header2 = "My Header"; key = "foo"; isResizable = true; this.width = "256px"; this.isAutoWidth = true; this.flexGrow = 2; this.isSortable = true
                    }
                }.toPrettyString()
            }
            expect("Anchor[href='']") { Anchor().toPrettyString() }
            expect("Anchor[href='vaadin.com']") { Anchor("vaadin.com").toPrettyString() }
            expect("Image[]") { Image().toPrettyString() }
            expect("Image[@src='vaadin.com']") {
                Image(
                    "vaadin.com",
                    ""
                ).toPrettyString()
            }
            expect("TextField[#25, value='', errorMessage='failed validation']") {
                TextField().apply {
                    id_ = "25"; errorMessage = "failed validation"
                }.toPrettyString()
            }
            expect("TextField[label='foobar', value='']") {
                TextField("foobar").toPrettyString()
            }
            expect("Icon[icon='vaadin:abacus']") {
                VaadinIcon.ABACUS.create().toPrettyString()
            }
            expect("Button[icon='vaadin:abacus', @theme='icon']") {
                Button(
                    VaadinIcon.ABACUS.create()
                ).toPrettyString()
            }
            expect("FormItem[label='foo']") {
                FormLayout().addFormItem(
                    TextField(),
                    "foo"
                ).toPrettyString()
            }
            expect("Html[<b>foo bar baz <i>foobar</i></b>]") {
                Html("\n    <b>foo\nbar\n    baz\n<i>foobar</i></b>").toPrettyString()
            }
            expect("HtmlSpan[innerHTML='aaa<b>bbbb</b>ccc']") {
                HtmlSpan("aaa<b>bbbb</b>ccc").toPrettyString()
            }
            expect("MyComponentWithToString[my-div(25)]") {
                MyComponentWithToString().toPrettyString()
            }
            expect("Div[@title='foobar']") {
                Div().apply { tooltip = "foobar" }.toPrettyString()
            }
            expect("Span[text='hi', @slot='prefix']") {
                val testSpan = Span("hi")
                TextField().prefixComponent = testSpan
                testSpan.toPrettyString()
            }
            expect("Tab[label='foo', Tab{foo}]") {
                // The trailing Tab{foo} comes from the custom Tab.toString()
                Tab("foo").toPrettyString()
            }
        }
        @Nested inner class notifications {
            @Test fun basic() {
                expect("Notification['foobar']") {
                    Notification("foobar").apply { open() }.toPrettyString()
                }
            }
            @Test fun closed() {
                expect("Notification['foobar', CLOSED]") {
                    Notification("foobar").apply { close() }.toPrettyString()
                }
            }
        }
    }

    @Test fun `styles not duplicated`() {
        val div = Div()
        expect("Div[]") { div.toPrettyString() }
        div.setWidthFull()
        expect("Div[@style='width:100%']") { div.toPrettyString() }
        div.style.set("flex-shrink", "1")
        expect("Div[@style='width:100%;flex-shrink:1']") { div.toPrettyString() }
    }

    @Test fun `menu dump`() {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("menu") {
                    isEnabled = false
                    item("click me", { fail("shouldn't be called") })
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

    @Nested inner class grid {
        @Test fun `column headers`() {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                addColumn(karibuDslI18n).setHeader("Hello!")
                setItems2(listOf())
                _prepare()
            }
            expect("""
└── Grid[<String>, dataprovider='ListDataProvider<?>(0 items)']
    └── Column[header='Hello!', sortable, flexGrow=1]
""".trim()) { grid.toPrettyTree().trim() }
        }

        @Test fun `grid menu dump`() {
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("menu") {
                        isEnabled = false
                        item("click me", { fail("shouldn't be called") })
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
        @Test fun `grid filters dump`() {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                val col: Grid.Column<String> = addColumn(karibuDslI18n)
                appendHeaderRow().getCell(col).setComponent(TextField("Filter:"))
                setItems2(listOf())
                _prepare()
            }
            expect("""
└── Grid[<String>, dataprovider='ListDataProvider<?>(0 items)']
    ├── TextField[label='Filter:', value='']
    └── Column[sortable, flexGrow=1]""".trim()) { grid.toPrettyTree().trim() }
        }

        // tests https://github.com/mvysny/karibu-testing/issues/37
        @Test fun `grid filters dump for joined column`() {
            val grid: Grid<String> = UI.getCurrent().grid<String> {
                val col1: Grid.Column<String> = addColumn(karibuDslI18n).setHeader("foo")
                val col2: Grid.Column<String> = addColumn(karibuDslI18n).setHeader("bar")
                appendHeaderRow()
                prependHeaderRow().join(col1, col2).setComponent(TextField("Filter:"))
                setItems2(listOf())
                _prepare()
            }
            expect("""
└── Grid[<String>, dataprovider='ListDataProvider<?>(0 items)']
    ├── TextField[label='Filter:', value='']
    └── ColumnGroup[]
        ├── ColumnGroup[]
        │   └── Column[header='foo', sortable, flexGrow=1]
        └── ColumnGroup[]
            └── Column[header='bar', sortable, flexGrow=1]""".trim()) { grid.toPrettyTree().trim() }
        }
    }
}

class MyComponentWithToString : Div() {
    override fun toString(): String = "my-div(25)"
}
