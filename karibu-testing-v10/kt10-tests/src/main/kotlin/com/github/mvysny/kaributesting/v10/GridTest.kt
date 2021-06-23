package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.karibudsl.v10.component
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.grid.FooterRow
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.HeaderRow
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.component.grid.dnd.GridDropMode
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.LocalDateRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.test.expect
import kotlin.test.fail

internal fun DynaNodeGroup.gridTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("DataProvider") {
        test("_size") {
            expect(20) {
                ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._size()
            }
        }

        test("_get") {
            expect("name 5") {
                ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._get(5).name
            }
            expectThrows(AssertionError::class, "Requested to get row 30 but the data provider only has 20 rows") {
                ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._get(30)
            }
        }

        test("_findAll") {
            val list = (0 until 20).map { TestPerson("name $it", it) }
            expect(list) { ListDataProvider<TestPerson>(list)._findAll() }
        }
    }

    test("_dump") {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val grid = UI.getCurrent().grid<TestPerson>(dp) {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
        }
        expect("--[Name]-[Age]--\n--and 7 more\n") { grid._dump(0 until 0) }
        expect("--[Name]-[Age]--\n0: name 0, 0\n1: name 1, 1\n2: name 2, 2\n3: name 3, 3\n4: name 4, 4\n--and 2 more\n") { grid._dump(0 until 5) }
        expect("--[Name]-[Age]--\n0: name 0, 0\n1: name 1, 1\n2: name 2, 2\n3: name 3, 3\n4: name 4, 4\n5: name 5, 5\n6: name 6, 6\n") {
            grid._dump(
                0 until 20
            )
        }
    }

    test("_dump shows sorting indicator") {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val grid = UI.getCurrent().grid<TestPerson>(dp) {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            sort(TestPerson::name.asc, TestPerson::age.desc)
        }
        expect("--[Name]v-[Age]^--\n--and 7 more\n") { grid._dump(0 until 0) }
    }

    group("expectRow()") {
        test("simple") {
            val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
            val grid = UI.getCurrent().grid<TestPerson>(dp) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            grid.expectRows(7)
            grid.expectRow(0, "name 0", "0")
        }

        test("failed expectRow contains table dump") {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val grid = UI.getCurrent().grid<TestPerson>(dp) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            expectThrows(AssertionError::class, "--[Name]-[Age]--\n0: name 0, 0") {
                grid.expectRow(0, "name 1", "1")
            }
        }

        test("row out-of-bounds contains table dump") {
            val dp: ListDataProvider<TestPerson> = ListDataProvider((0 until 1).map { TestPerson("name $it", it) })
            val grid: Grid<TestPerson> = UI.getCurrent().grid<TestPerson>(dp) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            expectThrows(AssertionError::class, "--[Name]-[Age]--\n0: name 0, 0") {
                grid.expectRow(3, "should fail", "should fail") // should fail
            }
        }
    }

    test("header2") {
        val grid: Grid<TestPerson> = Grid(TestPerson::class.java)
        expect("") { grid.addColumn({ it }).header2 }
        expect("Foo") { grid.addColumn({ it }).apply { setHeader("Foo") }.header2 }
        expect("") { grid.addColumn({ it }).apply { setHeader(Text("Foo")) }.header2 }
        expect("Foo") { grid.addColumn({ it }).apply { setHeader("Foo"); setSortProperty("name") }.header2 }
    }

    test("header2 with joined columns") {
        lateinit var col1: Grid.Column<String>
        lateinit var col2: Grid.Column<String>
        UI.getCurrent().grid<String> {
            col1 = addColumn(karibuDslI18n).setHeader("foo")
            col2 = addColumn(karibuDslI18n).setHeader("bar")
            appendHeaderRow()
            prependHeaderRow().join(col1, col2).setComponent(TextField("Filter:"))
        }
        expect("foo") { col1.header2 }
        expect("bar") { col2.header2 }
    }

    test("renderers") {
        val grid = UI.getCurrent().grid<TestPerson> {
            addColumnFor(TestPerson::name)
            addColumn(NativeButtonRenderer<TestPerson>("View", { }))
            addColumn(ComponentRenderer<Button, TestPerson> { it -> Button(it.name) })
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale("fi", "FI"))
            addColumn(LocalDateRenderer<TestPerson>({ LocalDate.of(2019, 3, 1) }, formatter))
        }
        grid.dataProvider = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        grid.expectRow(0, "name 0", "View", "Button[caption='name 0']", "1.3.2019")
    }

    test("lookup finds components in header") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.headerRows[0].cells[0].setComponent(TextField("Foo!"))
        expect("Foo!") { grid._get<TextField>().caption }
    }

    // tests https://github.com/mvysny/karibu-testing/issues/52
    test("lookup finds components in merged header cells") {
        UI.getCurrent().grid<String> {
            val c1: Grid.Column<String> = addColumn { it }
            val c2: Grid.Column<String> = addColumn { it }
            appendHeaderRow()
            val row1: HeaderRow = prependHeaderRow()
            row1.join(row1.getCell(c1), row1.getCell(c2)).setComponent(TextField("Bar"))
        }
        _expectOne<TextField> { caption = "Bar" }
    }

    // tests https://github.com/mvysny/karibu-testing/issues/52
    test("lookup finds components in merged footer cells") {
        UI.getCurrent().grid<String> {
            val c1: Grid.Column<String> = addColumn { it }
            val c2: Grid.Column<String> = addColumn { it }
            appendFooterRow()
            val row1: FooterRow = appendFooterRow()
            row1.join(row1.getCell(c1), row1.getCell(c2)).setComponent(TextField("Bar"))
        }
        _expectOne<TextField> { caption = "Bar" }
    }

    test("lookup finds components in footer") {
        val grid = Grid(TestPerson::class.java)
        grid.appendFooterRow().cells[0].setComponent(TextField("Foo!"))
        expect("Foo!") { grid._get<TextField>().caption }
    }

    test("lookup skips empty slots in header") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.headerRows[0].cells[0].setComponent(TextField("Foo!"))
        grid.appendHeaderRow().cells[0].component = null
        expect(null) { grid.appendHeaderRow().cells[0].component }
        expect("Foo!") { grid._get<TextField>().caption }
    }

    group("click renderer") {
        test("ClickableRenderer") {
            var called = false
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { person ->
                    called = true
                    expect("name 8") { person.name }
                }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
        test("ComponentRenderer with Button") {
            var called = false
            val grid = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { person -> Button("View").apply {
                    onLeftClick {
                        called = true
                        expect("name 8") { person.name }
                    }
                } }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
        test("ComponentRenderer with ClickNotifier") {
            var called = false
            val grid = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Checkbox, TestPerson> { person -> Checkbox("View").apply {
                    addClickListener {
                        called = true
                        expect("name 8") { person.name }
                    }
                } }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
        test("fails on disabled grid") {
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { fail("Shouldn't be called") }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
                isEnabled = false
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, dataprovider='ListDataProvider2{11 items}'] is not enabled") {
                grid._clickRenderer(2, "name")
            }
        }
        test("fails on unsupported component type") {
            expect(false) { Label() is ClickNotifier<*> }
            val grid = Grid<TestPerson>().apply {
                setItems2((0..10).map { TestPerson("name $it", it) })
                addColumn(ComponentRenderer<Label, TestPerson> { _ -> Label() }).key = "name"
            }
            expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider2{11 items}'] column name: ComponentRenderer produced Label[] which is not a button nor a ClickNotifier - please use _getCellComponent() instead") {
                grid._clickRenderer(8, "name")
            }
        }
    }

    group("_getCellComponent") {
        test("fails with ClickableRenderer") {
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") {}).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider2{11 items}'] column name uses NativeButtonRenderer which is not supported by this function") {
                grid._getCellComponent(8, "name")
            }
        }
        test("ComponentRenderer with Button") {
            var called = false
            val grid = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { person -> Button("View").apply {
                    onLeftClick {
                        called = true
                        expect("name 8") { person.name }
                    }
                } }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            (grid._getCellComponent(8, "name") as Button)._click()
            expect(true) { called }
        }
        test("doesn't fail on disabled grid") {
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { _ -> Button("View") }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
                isEnabled = false
            }
            expect(true) { grid._getCellComponent(2, "name") is Button }
        }
    }

    test("sorting") {
        val grid = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            setItems2((0..10).map { TestPerson("name $it", it) })
        }
        grid.sort(TestPerson::age.desc)
        expect((0..10).map { TestPerson("name $it", it) }.reversed()) { grid._findAll() }
        expect(10) { grid._get(0).age }
        expect(0) { grid._get(10).age }
    }

    test("_getColumnByKey()") {
        val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            setItems2(listOf())
        }
        expect("name") { grid._getColumnByKey("name").key }
        expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider2{0 items}']: No such column with key 'foo'; available columns: [name, age]") {
            grid._getColumnByKey("foo")
        }
    }

    group("_getFormatted()") {
        // tests https://github.com/mvysny/karibu-testing/issues/18
        test("non-existing column key") {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            expectThrows(AssertionError::class, "No such column with key 'surname'; available columns: [name, age]") {
                grid._getFormatted(0, "surname")
            }
        }

        test("non-existing row") {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            expectThrows(AssertionError::class, "Requested to get row 0 but the data provider only has 0 rows") {
                grid._getFormatted(0, "name")
            }
        }

        test("basic") {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            expect("name 0") { grid._getFormatted(0, "name") }
        }
    }

    group("_clickItem") {
        test("fails on disabled grid") {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { fail("Shouldn't be called") }
                isEnabled = false
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, dataprovider='ListDataProvider2{11 items}'] is not enabled") {
                grid._clickItem(2)
            }
        }
        test("simple") {
            lateinit var event: ItemClickEvent<TestPerson>
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
            }
            grid._clickItem(2)
            expect("name 2") { event.item.name }
            expect(true) { event.isFromClient }
            expect(1) { event.button }
            expect(false) { event.isAltKey }
            expect(false) { event.isCtrlKey }
            expect(false) { event.isMetaKey }
            expect(false) { event.isShiftKey }
        }
    }

    group("_doubleClickItem") {
        test("fails on disabled grid") {
            val grid = UI.getCurrent().grid<TestPerson> {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemDoubleClickListener { fail("Shouldn't be called") }
                isEnabled = false
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, <TestPerson>, dataprovider='ListDataProvider2{11 items}'] is not enabled") {
                grid._doubleClickItem(2)
            }
        }
        test("simple") {
            lateinit var event: ItemClickEvent<TestPerson>
            val grid = UI.getCurrent().grid<TestPerson> {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemDoubleClickListener { e -> event = e }
            }
            grid._doubleClickItem(2)
            expect("name 2") { event.item.name }
            expect(true) { event.isFromClient }
            expect(1) { event.button }
            expect(false) { event.isAltKey }
            expect(false) { event.isCtrlKey }
            expect(false) { event.isMetaKey }
            expect(false) { event.isShiftKey }
        }
    }

    group("drag n drop") {
        test("smoke") {
            UI.getCurrent().grid<String> {
                dropMode = GridDropMode.ON_TOP
                isRowsDraggable = true
                addDragStartListener { }
                addDropListener { }
            }
        }
    }

    // https://github.com/mvysny/karibu-testing/issues/75
    group("editor") {
        test("smoke") {
            UI.getCurrent().grid<String> {
                setItems2(listOf("foo"))
                editor._editItem("foo")
            }
        }
        test("opening editor populates editor fields and runs openlisteners") {
            val editorField = TextField()
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2((0..10).map { TestPerson("name $it", it) })
                val binder = beanValidationBinder<TestPerson>()
                editor.binder = binder
                addColumn("name").apply {
                    editorField.apply {
                        bind(binder).bind("name")
                    }
                    setEditorComponent(editorField)
                }
            }

            // the test itself
            var ran = false
            grid.editor.addOpenListener { ran = true }
            grid.editor._editItem(TestPerson("name 0", 0))
            expect(true) { ran }
            expect("name 0") { editorField.value }
        }
        test("opening editor fails on incorrect binding") {
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2((0..10).map { TestPerson("name $it", it) })
                val binder = beanValidationBinder<TestPerson>()
                editor.binder = binder
                addColumn("name").apply {
                    val editor = Checkbox().apply {
                        bind(binder).bind("name")
                    }
                    setEditorComponent(editor)
                }
            }

            // the test itself
            expectThrows(ClassCastException::class, "java.lang.String cannot be cast to") {
                grid.editor._editItem(TestPerson("name 0", 0))
            }
        }
        test("closing editor fires the event") {
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid.editor._editItem(TestPerson("name 0", 0))
            var fired = false
            grid.editor.addCloseListener { fired = true }
            grid.editor.closeEditor()
            expect(true) { fired }
        }
    }
}

data class TestPerson(var name: String, var age: Int): Comparable<TestPerson> {
    override fun compareTo(other: TestPerson): Int = compareValuesBy(this, other, { it.name }, { it.age })
}

fun <T> Grid<T>.setItems2(items: Collection<T>) {
    dataProvider = ListDataProvider2(items)
}

/**
 * Need to have this class because of https://github.com/vaadin/flow/issues/8553
 */
class ListDataProvider2<T>(items: Collection<T>): ListDataProvider<T>(items) {
    override fun toString(): String = "ListDataProvider2{${items.size} items}"
}
