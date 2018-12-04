package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.grid
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import java.lang.IllegalStateException
import kotlin.test.expect

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
        grid.expectRow(0, "name 0", "null")
    }

    test("lookup finds components in header") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.headerRows[0].cells[0].setComponent(TextField("Foo!"))
        expect("Foo!") { grid._get<TextField>().caption }
    }

    if (vaadinVersion >= 12) {
        // _clickRenderer() works on Vaadin 12 only
        test("click renderer") {
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
    } else {
        test("renderer retrieval fails with helpful error message") {
            expectThrows(IllegalStateException::class, "This functionality can only be used with Vaadin 12 or higher") {
                Grid<TestPerson>().apply {
                    addColumnFor(TestPerson::name)
                }.columns[0].renderer
            }
        }
    }
}

data class TestPerson(val name: String, val age: Int)
