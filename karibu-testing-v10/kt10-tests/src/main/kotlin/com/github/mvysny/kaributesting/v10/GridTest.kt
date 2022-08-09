package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.*
import com.vaadin.flow.component.ClickNotifier
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
import com.vaadin.flow.data.provider.*
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.LocalDateRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import com.vaadin.flow.data.selection.SelectionEvent
import com.vaadin.flow.function.ValueProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.stream.Stream
import kotlin.test.expect
import kotlin.test.fail

@DynaTestDsl
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
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]-[Age]--\n--and 7 more\n") { grid._dump(0 until 0) }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]-[Age]--\n0: name 0, 0\n1: name 1, 1\n2: name 2, 2\n3: name 3, 3\n4: name 4, 4\n--and 2 more\n") { grid._dump(0 until 5) }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]-[Age]--\n0: name 0, 0\n1: name 1, 1\n2: name 2, 2\n3: name 3, 3\n4: name 4, 4\n5: name 5, 5\n6: name 6, 6\n") {
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
            isMultiSort = true
            sort(TestPerson::name.asc, TestPerson::age.desc)
        }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]v-[Age]^--\n--and 7 more\n") { grid._dump(0 until 0) }
    }

    test("sort fails with an informative error message on missing column") {
        // https://github.com/mvysny/karibu-testing/issues/97
        expectThrows(AssertionError::class, "No such column with key 'Last Name'; available columns: [name, age]") {
            UI.getCurrent().grid<TestPerson>() {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                sort(QuerySortOrder("Last Name", SortDirection.ASCENDING))
            }
        }
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

    test("column with ValueProvider") {
        val grid = UI.getCurrent().grid<TestPerson> {
            addColumn(ValueProvider<TestPerson, String> { it.name })
        }
        grid.dataProvider = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        grid.expectRow(0, "name 0")
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
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, dataprovider='ListDataProvider<TestPerson>(11 items)'] is not enabled") {
                grid._clickRenderer(2, "name")
            }
        }
        test("fails on unsupported component type") {
            expect(false) { Label() is ClickNotifier<*> }
            val grid = Grid<TestPerson>().apply {
                setItems2((0..10).map { TestPerson("name $it", it) })
                addColumn(ComponentRenderer<Label, TestPerson> { _ -> Label() }).key = "name"
            }
            expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<TestPerson>(11 items)'] column key='name': ComponentRenderer produced Label[] which is not a button nor a ClickNotifier - please use _getCellComponent() instead") {
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
            expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<TestPerson>(11 items)'] column key='name' uses NativeButtonRenderer which is not supported by this function") {
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
        expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<?>(0 items)']: No such column with key 'foo'; available columns: [name, age]") {
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
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, dataprovider='ListDataProvider<TestPerson>(11 items)'] is not enabled") {
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
        test("get clicked column") {
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }

            }
            grid._clickItem(2, nameColumn)
            expect(nameColumn) { event.column }
        }
        test("get clicked column by columnKey") {
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }

            }
            grid._clickItem(2, nameColumn.key)
            expect(nameColumn) { event.column }
        }
        test("SingleSelect: SelectionEvent fired as well") {
            // see https://github.com/mvysny/karibu-testing/issues/96
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var selectionEvent: SelectionEvent<*, *>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
                addSelectionListener { e -> selectionEvent = e }

            }
            grid._clickItem(2, nameColumn.key)
            expect(nameColumn) { event.column }
            expect(TestPerson("name 2", 2)) { selectionEvent.firstSelectedItem.get() }
        }
        test("SingleSelect: Selection cleared when clicking an item two times") {
            // see https://github.com/mvysny/karibu-testing/issues/96
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var selectionEvent: SelectionEvent<*, *>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
                addSelectionListener { e -> selectionEvent = e }

            }
            grid._clickItem(2, nameColumn.key)
            grid._clickItem(2, nameColumn.key)
            expect(nameColumn) { event.column }
            expect(null) { selectionEvent.firstSelectedItem.orElse(null) }
        }
        test("SingleSelect: Selection properly updated when clicking another item") {
            // see https://github.com/mvysny/karibu-testing/issues/96
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var selectionEvent: SelectionEvent<*, *>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
                addSelectionListener { e -> selectionEvent = e }

            }
            grid._clickItem(2, nameColumn.key)
            grid._clickItem(1, nameColumn.key)
            expect(nameColumn) { event.column }
            expect(TestPerson("name 1", 1)) { selectionEvent.firstSelectedItem.get() }
        }
        test("MultiSelect: no SelectionEvent fired") {
            // see https://github.com/mvysny/karibu-testing/issues/96
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                setSelectionMode(Grid.SelectionMode.MULTI)
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
                addSelectionListener { fail("No selection event should be fired") }
            }
            grid._clickItem(2, nameColumn.key)
            expect(nameColumn) { event.column }
        }
        test("no selection: no SelectionEvent fired") {
            // see https://github.com/mvysny/karibu-testing/issues/96
            lateinit var event: ItemClickEvent<TestPerson>
            lateinit var nameColumn: Grid.Column<*>

            val grid = Grid<TestPerson>().apply {
                setSelectionMode(Grid.SelectionMode.NONE)
                nameColumn = addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
            }
            grid._clickItem(2, nameColumn.key)
            expect(nameColumn) { event.column }
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
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, <TestPerson>, dataprovider='ListDataProvider<TestPerson>(11 items)'] is not enabled") {
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
        test("opening editor then get field to set value") {
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2((0..10).map { TestPerson("name $it", it) })
                val binder = beanValidationBinder<TestPerson>()
                editor.binder = binder
                addColumn("name").apply {
                    val editorField = TextField()
                    editorField.apply {
                        bind(binder).bind("name")
                    }
                    setEditorComponent(editorField)
                }
            }

            // the test itself
            grid.editor._editItem(TestPerson("name 0", 0))
            val editorField = grid._get<TextField>()
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

            // the test itself. Vaadin 22+ fails with
            // BindingException: An exception has been thrown inside binding logic for the field element [checked='false', indeterminate='false']
            // older Vaadin fails directly with ClassCastException: java.lang.String cannot be cast to
            if (VaadinVersion.get.major >= 22) {
                expectThrows(RuntimeException::class, "An exception has been thrown inside binding logic") {
                    grid.editor._editItem(TestPerson("name 0", 0))
                }
            } else {
                expectThrows(ClassCastException::class, "java.lang.String cannot be cast to") {
                    grid.editor._editItem(TestPerson("name 0", 0))
                }
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

    group("selection") {
        test("_select") {
            val items = (0..10).map { TestPerson("name $it", it) }
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2(items)
            }
            grid._select(items[1])
            expect(setOf(items[1])) { grid.selectedItems }

            grid.selectionMode = Grid.SelectionMode.MULTI
            grid._select(items[1])
            expect(setOf(items[1])) { grid.selectedItems }

            grid.selectionMode = Grid.SelectionMode.NONE
            expectThrows<IllegalStateException>("selection mode is currently set to NONE") {
                grid._select(items[1])
            }
            expect(setOf()) { grid.selectedItems }
        }

        test("_selectRow") {
            val items = (0..10).map { TestPerson("name $it", it) }
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2(items)
            }
            grid._selectRow(1)
            expect(setOf(items[1])) { grid.selectedItems }

            grid.selectionMode = Grid.SelectionMode.MULTI
            grid._selectRow(1)
            expect(setOf(items[1])) { grid.selectedItems }

            grid.selectionMode = Grid.SelectionMode.NONE
            expectThrows<IllegalStateException>("selection mode is currently set to NONE") {
                grid._selectRow(1)
            }
            expect(setOf()) { grid.selectedItems }
        }

        test("_selectAll()") {
            val items = (0..10).map { TestPerson("name $it", it) }
            val grid = UI.getCurrent().grid<TestPerson> {
                setItems2(items)
            }

            expectThrows<AssertionError>("Expected multi-select but got SINGLE") {
                grid._selectAll()
            }
            expect(setOf()) { grid.selectedItems }

            grid.selectionMode = Grid.SelectionMode.NONE
            expectThrows<AssertionError>("Expected multi-select but got NONE") {
                grid._selectAll()
            }
            expect(setOf()) { grid.selectedItems }

            grid.selectionMode = Grid.SelectionMode.MULTI
            grid._selectAll()
            expect(items.toSet()) { grid.selectedItems }
        }
    }

    // https://github.com/mvysny/karibu-testing/issues/124
    group("non-pure column value providers") {
        test("_get() caches values") {
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider())
            val person = grid._get(2)
            expect("name 2") { person.name } // sanity check
            var person2 = grid._get(2)
            expect(true) { person === person2 }
            person2 = grid._get(2)
            expect(true) { person === person2 }
        }

        test("_get() calls column valueproviders") {
            // test the "lazy populator" use-case where column ValueProviders act like lazy bean populators.
            // Weird, but allowed by Vaadin.
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider()) {
                addColumn { it.age = -3; it.name }
            }
            val person = grid._get(2)
            expect(-3, "person: $person") { person.age }
        }

        test("_get() calls column valueproviders at most once") {
            // test the "lazy populator" use-case where column ValueProviders act like lazy bean populators.
            // Weird, but allowed by Vaadin.
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider()) {
                addColumn { it.age--; it.name }
            }
            var person = grid._get(2)
            expect(1, "person: $person") { person.age }
            person = grid._get(2)
            expect(1, "person: $person") { person.age }
        }

        test("_selectRow() test") {
            // test the use-case documented in https://github.com/mvysny/karibu-testing/issues/124
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider()) {
                // a column ValueProvider with a side-effect (non-pure function)
                addColumn { it.age = -3; it.name }
            }
            var age: Int = -100
            grid.addSelectionListener {
                // _selectRow calls deselectAll() first.
                if (it.firstSelectedItem.isPresent) {
                    age = it.firstSelectedItem.get().age
                }
            }
            grid._selectRow(2)
            // the selection listener should have received a cache version of the bean for which
            // the column ValueProviders have been run. Verify that age is -3 as set by the column ValueProvider.
            expect(-3) { age }
            age = -100
            grid._selectRow(4)
            expect(-3) { age }
        }
    }
}

data class TestPerson(var name: String, var age: Int): Comparable<TestPerson> {
    override fun compareTo(other: TestPerson): Int = compareValuesBy(this, other, { it.name }, { it.age })
}

/**
 * A data provider which provides fresh instances of [TestPerson] on every fetch.
 */
class PersonBackendDataProvider(val size: Int = 10) : AbstractBackEndDataProvider<TestPerson, Void>() {
    init {
        require(size >= 0) { "size $size must not be negative" }
    }
    override fun fetchFromBackEnd(query: Query<TestPerson, Void>): Stream<TestPerson> {
        require(!query.filter.isPresent) { "query $query: filter is present but no filter is supported" }
        require(query.offset >= 0) { "query $query: negative offset ${query.offset}" }
        require(query.limit >= 0) { "query $query: negative limit ${query.limit}" }
        if (query.offset >= size) { return listOf<TestPerson>().stream() }
        val rows = query.offset until (query.offset + query.limit).coerceAtMost(size)
        val result = rows.map { TestPerson("name $it", it) }
        return result.stream()
    }

    override fun sizeInBackEnd(query: Query<TestPerson, Void>): Int = size
}

fun <T> Grid<T>.setItems2(items: Collection<T>) {
    // Vaadin 15+ uses DataView and setItems() has been moved to HasDataView,
    // introducing binary incompatibility. We thus can't have a code which calls [setItems] since that
    // will not work with both Vaadin 14 and Vaadin 15+. This is a workaround.
    dataProvider = ListDataProvider(items)
}
