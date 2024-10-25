package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.*
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.grid.ColumnResizeEvent
import com.vaadin.flow.component.grid.FooterRow
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.HeaderRow
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.component.grid.dnd.GridDropMode
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.*
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.LocalDateRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import com.vaadin.flow.data.selection.SelectionEvent
import com.vaadin.flow.function.ValueProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.stream.Stream
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractGridTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class DataProviderTests {
        @Test fun _size() {
            expect(20) {
                ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._size()
            }
        }

        @Test fun _get() {
            expect("name 5") {
                ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._get(5).name
            }
            expectThrows(AssertionError::class, "Requested to get row 30 but the data provider only has 20 rows") {
                ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._get(30)
            }
        }

        @Test fun _findAll() {
            val list = (0 until 20).map { TestPerson("name $it", it) }
            expect(list) { ListDataProvider<TestPerson>(list)._findAll() }
        }
    }

    @Test fun _dump() {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val grid = UI.getCurrent().grid<TestPerson>(dp) {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            _prepare()
        }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]-[Age]--\n--and 7 more\n") { grid._dump(0 until 0) }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]-[Age]--\n0: name 0, 0\n1: name 1, 1\n2: name 2, 2\n3: name 3, 3\n4: name 4, 4\n--and 2 more\n") { grid._dump(0 until 5) }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]-[Age]--\n0: name 0, 0\n1: name 1, 1\n2: name 2, 2\n3: name 3, 3\n4: name 4, 4\n5: name 5, 5\n6: name 6, 6\n") {
            grid._dump(
                0 until 20
            )
        }
    }

    @Test fun `_dump shows sorting indicator`() {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val grid = UI.getCurrent().grid<TestPerson>(dp) {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            isMultiSort = true
            _sort(TestPerson::name.asc, TestPerson::age.desc)
            _prepare()
        }
        expect("Grid[<TestPerson>, dataprovider='ListDataProvider<TestPerson>(7 items)']\n--[Name]v-[Age]^--\n--and 7 more\n") { grid._dump(0 until 0) }
    }

    @Test fun `_sort fails with an informative error message on missing column`() {
        // https://github.com/mvysny/karibu-testing/issues/97
        expectThrows(AssertionError::class, "No such column with key 'Last Name'; available columns: [name, age]") {
            UI.getCurrent().grid<TestPerson>() {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                _sort(QuerySortOrder("Last Name", SortDirection.ASCENDING))
            }
        }
    }

    @Nested inner class expectRow {
        @Test fun simple() {
            val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
            val grid = UI.getCurrent().grid<TestPerson>(dp) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            grid.expectRows(7)
            grid.expectRow(0, "name 0", "0")
        }

        @Test fun `failed expectRow contains table dump`() {
            val dp = ListDataProvider<TestPerson>((0 until 1).map { TestPerson("name $it", it) })
            val grid = UI.getCurrent().grid<TestPerson>(dp) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            expectThrows(AssertionError::class, "--[Name]-[Age]--\n0: name 0, 0") {
                grid.expectRow(0, "name 1", "1")
            }
        }

        @Test fun `row out-of-bounds contains table dump`() {
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

    @Nested inner class expectRowRegex {
        @Test fun simple() {
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider()) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            grid.expectRowRegex(0, "name 0", "0")
            grid.expectRowRegex(0, "name.*", "0")
        }

        @Test fun `failed expectRow message contains table dump`() {
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider(1)) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                _prepare()
            }
            expectThrows(AssertionError::class, "Grid[<TestPerson>, dataprovider='PersonBackendDataProvider'] at 0: expected [name 1, 1] but got [name 0, 0]\nGrid[<TestPerson>, dataprovider='PersonBackendDataProvider']\n--[Name]-[Age]--\n0: name 0, 0") {
                grid.expectRowRegex(0, "name 1", "1")
            }
        }

        @Test fun `row out-of-bounds contains table dump`() {
            val dp: ListDataProvider<TestPerson> = ListDataProvider((0 until 1).map { TestPerson("name $it", it) })
            val grid: Grid<TestPerson> = UI.getCurrent().grid<TestPerson>(dp) {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            expectThrows(AssertionError::class, "--[Name]-[Age]--\n0: name 0, 0") {
                grid.expectRowRegex(3, "should fail", "should fail") // should fail
            }
        }
    }

    @Test fun renderers() {
        val grid = UI.getCurrent().grid<TestPerson> {
            addColumnFor(TestPerson::name)
            addColumn(NativeButtonRenderer<TestPerson>("View", { }))
            addColumn(ComponentRenderer<Button, TestPerson> { it -> Button(it.name) })
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.forLanguageTag("fi-FI"))
            addColumn(LocalDateRenderer<TestPerson>({ LocalDate.of(2019, 3, 1) }, { formatter }))
        }
        grid.dataProvider = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        grid.expectRow(0, "name 0", "View", "Button[text='name 0']", "1.3.2019")
    }

    @Test fun `column with ValueProvider`() {
        val grid = UI.getCurrent().grid<TestPerson> {
            addColumn(ValueProvider<TestPerson, String> { it.name })
        }
        grid.dataProvider = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        grid.expectRow(0, "name 0")
    }

    @Test fun `lookup finds components in header`() {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.headerRows[0].cells[0].setComponent(TextField("Foo!"))
        expect("Foo!") { grid._get<TextField>().label }
    }

    // tests https://github.com/mvysny/karibu-testing/issues/52
    @Test fun `lookup finds components in merged header cells`() {
        UI.getCurrent().grid<String> {
            val c1: Grid.Column<String> = addColumn { it }
            val c2: Grid.Column<String> = addColumn { it }
            appendHeaderRow()
            val row1: HeaderRow = prependHeaderRow()
            row1.join(row1.getCell(c1), row1.getCell(c2)).setComponent(TextField("Bar"))
        }
        _expectOne<TextField> { label = "Bar" }
    }

    // tests https://github.com/mvysny/karibu-testing/issues/52
    @Test fun `lookup finds components in merged footer cells`() {
        UI.getCurrent().grid<String> {
            val c1: Grid.Column<String> = addColumn { it }
            val c2: Grid.Column<String> = addColumn { it }
            appendFooterRow()
            val row1: FooterRow = appendFooterRow()
            row1.join(row1.getCell(c1), row1.getCell(c2)).setComponent(TextField("Bar"))
        }
        _expectOne<TextField> { label = "Bar" }
    }

    @Test fun `lookup finds components in footer`() {
        val grid = Grid(TestPerson::class.java)
        grid.appendFooterRow().cells[0].setComponent(TextField("Foo!"))
        expect("Foo!") { grid._get<TextField>().label }
    }

    @Test fun `lookup skips empty slots in header`() {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.headerRows[0].cells[0].setComponent(TextField("Foo!"))
        grid.appendHeaderRow().cells[0].component = null
        expect(null) { grid.appendHeaderRow().cells[0].component }
        expect("Foo!") { grid._get<TextField>().label }
    }

    @Nested inner class `click renderer` {
        @Test fun ClickableRenderer() {
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
        @Test fun `ComponentRenderer with Button`() {
            var called = false
            val grid = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { person -> Button("View").apply {
                    onClick {
                        called = true
                        expect("name 8") { person.name }
                    }
                } }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
        @Test fun `ComponentRenderer with ClickNotifier`() {
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
        @Test fun `fails on disabled grid`() {
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { fail("Shouldn't be called") }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
                isEnabled = false
                _prepare()
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, dataprovider='ListDataProvider<TestPerson>(11 items)'] is not enabled") {
                grid._clickRenderer(2, "name")
            }
        }
        @Test fun `fails on unsupported component type`() {
            expect(false) {
                @Suppress("KotlinConstantConditions")
                NativeLabel() is ClickNotifier<*>
            }
            val grid = Grid<TestPerson>().apply {
                setItems2((0..10).map { TestPerson("name $it", it) })
                addColumn(ComponentRenderer { _ -> NativeLabel() }).key = "name"
                _prepare()
            }
            expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<TestPerson>(11 items)'] column key='name': ComponentRenderer produced NativeLabel[] which is not a button nor a ClickNotifier - please use _getCellComponent() instead") {
                grid._clickRenderer(8, "name")
            }
        }
    }

    @Nested inner class _getCellComponent {
        @Test fun `fails with ClickableRenderer`() {
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") {}).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
                _prepare()
            }
            expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<TestPerson>(11 items)'] column key='name' uses NativeButtonRenderer which is not supported by this function") {
                grid._getCellComponent(8, "name")
            }
        }
        @Test fun `ComponentRenderer with Button`() {
            var called = false
            val grid = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { person -> Button("View").apply {
                    onClick {
                        called = true
                        expect("name 8") { person.name }
                    }
                } }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            (grid._getCellComponent(8, "name") as Button)._click()
            expect(true) { called }
        }
        @Test fun `doesn't fail on disabled grid`() {
            val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { _ -> Button("View") }).key = "name"
                setItems2((0..10).map { TestPerson("name $it", it) })
                isEnabled = false
            }
            expect(true) { grid._getCellComponent(2, "name") is Button }
        }
    }

    @Nested inner class sorting {
        @Test fun _sort() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._sort(TestPerson::age.desc)
            expect((0..10).map { TestPerson("name $it", it) }
                .reversed()) { grid._findAll() }
            expect(10) { grid._get(0).age }
            expect(0) { grid._get(10).age }
        }
        @Test fun _sortByKey() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._sortByKey("age", SortDirection.DESCENDING)
            expect((0..10).map { TestPerson("name $it", it) }
                .reversed()) { grid._findAll() }
            expect(10) { grid._get(0).age }
            expect(0) { grid._get(10).age }
        }
        @Test fun _sortByHeader() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            grid._sortByHeader("Age", SortDirection.DESCENDING)
            expect((0..10).map { TestPerson("name $it", it) }
                .reversed()) { grid._findAll() }
            expect(10) { grid._get(0).age }
            expect(0) { grid._get(10).age }
        }
    }

    @Test fun _getColumnByKey() {
        val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            setItems2(listOf())
            _prepare()
        }
        expect("name") { grid._getColumnByKey("name").key }
        expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<?>(0 items)']: No such column with key 'foo'; available columns: [name, age]") {
            grid._getColumnByKey("foo")
        }
    }

    @Test fun _getColumnByHeader() {
        val grid: Grid<TestPerson> = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            setItems2(listOf())
            _prepare()
        }
        expect("name") { grid._getColumnByHeader("Name").key }
        expectThrows(AssertionError::class, "Grid[dataprovider='ListDataProvider<?>(0 items)']: No such column with header 'foo'; available columns: [Name, Age]") {
            grid._getColumnByHeader("foo")
        }
    }

    @Nested inner class _getFormatted() {
        // tests https://github.com/mvysny/karibu-testing/issues/18
        @Test fun `non-existing column key`() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            expectThrows(AssertionError::class, "No such column with key 'surname'; available columns: [name, age]") {
                grid._getFormatted(0, "surname")
            }
        }

        @Test fun `non-existing row`() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
            }
            expectThrows(AssertionError::class, "Requested to get row 0 but the data provider only has 0 rows") {
                grid._getFormatted(0, "name")
            }
        }

        @Test fun basic() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
            }
            expect("name 0") { grid._getFormatted(0, "name") }
        }
    }

    @Nested inner class _clickItem {
        @Test fun `fails on disabled grid`() {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { fail("Shouldn't be called") }
                isEnabled = false
                _prepare()
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, dataprovider='ListDataProvider<TestPerson>(11 items)'] is not enabled") {
                grid._clickItem(2)
            }
        }
        @Test fun simple() {
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
        @Test fun `get clicked column`() {
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
        @Test fun `get clicked column by columnKey`() {
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
        @Test fun `SingleSelect - SelectionEvent fired as well`() {
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
        @Test fun `SingleSelect - Selection cleared when clicking an item two times`() {
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
        @Test fun `SingleSelect - Selection properly updated when clicking another item`() {
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
        @Test fun `MultiSelect - no SelectionEvent fired`() {
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
        @Test fun `no selection - no SelectionEvent fired`() {
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

    @Nested inner class _doubleClickItem {
        @Test fun `fails on disabled grid`() {
            val grid = UI.getCurrent().grid<TestPerson> {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems2((0..10).map { TestPerson("name $it", it) })
                addItemDoubleClickListener { fail("Shouldn't be called") }
                isEnabled = false
                _prepare()
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED, <TestPerson>, dataprovider='ListDataProvider<TestPerson>(11 items)'] is not enabled") {
                grid._doubleClickItem(2)
            }
        }
        @Test fun simple() {
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

    @Nested inner class `drag n drop`() {
        @Test fun smoke() {
            UI.getCurrent().grid<String> {
                dropMode = GridDropMode.ON_TOP
                isRowsDraggable = true
                addDragStartListener { }
                addDropListener { }
            }
        }
    }

    // https://github.com/mvysny/karibu-testing/issues/75
    @Nested inner class editor {
        @Test fun smoke() {
            UI.getCurrent().grid<String> {
                setItems2(listOf("foo"))
                editor._editItem("foo")
            }
        }
        @Test fun `opening editor populates editor fields and runs openlisteners`() {
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
        @Test fun `opening editor then get field to set value`() {
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
        @Test fun `opening editor fails on incorrect binding`() {
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

            expectThrows(RuntimeException::class, "An exception has been thrown inside binding logic") {
                grid.editor._editItem(TestPerson("name 0", 0))
            }
        }
        @Test fun `closing editor fires the event`() {
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

    @Nested inner class selection {
        @Test fun _select() {
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

        @Test fun _selectRow() {
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

        @Test fun _selectAll() {
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
    @Nested inner class `non-pure column value providers`() {
        @Test fun `_get() caches values`() {
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider())
            val person = grid._get(2)
            expect("name 2") { person.name } // sanity check
            var person2 = grid._get(2)
            expect(true) { person === person2 }
            person2 = grid._get(2)
            expect(true) { person === person2 }
        }

        @Test fun `_get() calls column valueproviders`() {
            // test the "lazy populator" use-case where column ValueProviders act like lazy bean populators.
            // Weird, but allowed by Vaadin.
            val grid = UI.getCurrent().grid<TestPerson>(PersonBackendDataProvider()) {
                addColumn { it.age = -3; it.name }
            }
            val person = grid._get(2)
            expect(-3, "person: $person") { person.age }
        }

        @Test fun `_get() calls column valueproviders at most once`() {
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

        @Test fun `_selectRow() test`() {
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

    @Nested inner class _fireColumnResizedEvent {
        @Test fun smoke() {
            lateinit var column: Grid.Column<TestPerson>
            val grid = UI.getCurrent().grid<TestPerson> {
                column = columnFor(TestPerson::name) {
                    isResizable = true
                }
            }
            lateinit var event: ColumnResizeEvent<TestPerson>
            grid.addColumnResizeListener { event = it }
            grid._fireColumnResizedEvent(column, 250)
            expect(true) { event.isFromClient }
            expect(column) { event.resizedColumn }
            expect("250px") { column.width }
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

fun Grid<*>._prepare() {
    // remove attribute added by Vaadin 23.2.0.beta1, so that the _dump() function produces
    // same results for all vaadin versions
    element.removeAttribute("multi-sort-priority")
}