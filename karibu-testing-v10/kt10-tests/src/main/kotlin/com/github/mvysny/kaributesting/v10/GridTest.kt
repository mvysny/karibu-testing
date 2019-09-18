package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.component
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import java.lang.IllegalStateException
import kotlin.test.expect
import kotlin.test.fail

internal fun DynaNodeGroup.gridTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

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

    test("expectRow()") {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val grid = UI.getCurrent().grid<TestPerson>(dp) {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
        }
        grid.expectRows(7)
        grid.expectRow(0, "name 0", "0")
    }

    test("header2") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        expect("") { grid.addColumn({ it }).header2 }
        expect("Foo") { grid.addColumn({ it }).apply { setHeader("Foo") }.header2 }
        expect("") { grid.addColumn({ it }).apply { setHeader(Text("Foo")) }.header2 }
        expect("Foo") { grid.addColumn({ it }).apply { setHeader("Foo"); setSortProperty("name") }.header2 }
    }

    test("renderers") {
        val grid = UI.getCurrent().grid<TestPerson> {
            addColumnFor(TestPerson::name)
            addColumn(NativeButtonRenderer<TestPerson>("View", { }))
        }
        grid.dataProvider = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        // unfortunately Vaadin 10 Renderer/Column code is so complex it's impossible to obtain the value of a NativeButtonRenderer
        grid.expectRow(0, "name 0", "View")
    }

    test("lookup finds components in header") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.headerRows[0].cells[0].setComponent(TextField("Foo!"))
        expect("Foo!") { grid._get<TextField>().caption }
    }

    test("lookup finds components in footer") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
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
            val grid = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { person ->
                    called = true
                    expect("name 8") { person.name }
                }).key = "name"
                setItems((0..10).map { TestPerson("name $it", it) })
            }
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
        test("ComponentRenderer") {
            var called = false
            val grid = Grid<TestPerson>().apply {
                addColumn(ComponentRenderer<Button, TestPerson> { person -> Button("View").apply {
                    onLeftClick {
                        called = true
                        expect("name 8") { person.name }
                    }
                } }).key = "name"
                setItems((0..10).map { TestPerson("name $it", it) })
            }
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
        test("fails on disabled grid") {
            val grid = Grid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { fail("Shouldn't be called") }).key = "name"
                setItems((0..10).map { TestPerson("name $it", it) })
                isEnabled = false
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED] is not enabled") {
                grid._clickRenderer(2, "name")
            }
        }
    }

    test("sorting") {
        val grid = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            setItems((0..10).map { TestPerson("name $it", it) })
        }
        grid.sort(TestPerson::age.desc)
        expect((0..10).map { TestPerson("name $it", it) }.reversed()) { grid._findAll() }
        expect(10) { grid._get(0).age }
        expect(0) { grid._get(10).age }
    }

    test("_getColumnByKey()") {
        val grid = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
        }
        expect("name") { grid._getColumnByKey("name").key }
        expectThrows(AssertionError::class, "Grid[]: No such column with key 'foo'; available columns: [name, age]") {
            grid._getColumnByKey("foo")
        }
    }

    group("_getFormatted()") {
        // tests https://github.com/mvysny/karibu-testing/issues/18
        test("non-existing column key") {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems((0..10).map { TestPerson("name $it", it) })
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
                setItems((0..10).map { TestPerson("name $it", it) })
            }
            expect("name 0") { grid._getFormatted(0, "name") }
        }
    }

    group("_clickItem") {
        test("fails on disabled grid") {
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { fail("Shouldn't be called") }
                isEnabled = false
            }
            expectThrows(IllegalStateException::class, "The Grid[DISABLED] is not enabled") {
                grid._clickItem(2)
            }
        }
        test("simple") {
            var event: ItemClickEvent<TestPerson>? = null
            val grid = Grid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setItems((0..10).map { TestPerson("name $it", it) })
                addItemClickListener { e -> event = e }
            }
            grid._clickItem(2)
            expect(true) { event != null }
            expect("name 2") { event!!.item.name }
            expect(true) { event!!.isFromClient }
            expect(1) { event!!.button }
            expect(false) { event!!.isAltKey }
            expect(false) { event!!.isCtrlKey }
            expect(false) { event!!.isMetaKey }
            expect(false) { event!!.isShiftKey }
        }
    }
}

data class TestPerson(val name: String, val age: Int)
