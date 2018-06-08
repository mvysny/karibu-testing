package com.github.karibu.testing.v10

import com.github.mvysny.dynatest.DynaTest
import com.github.vok.karibudsl.flow.addColumnFor
import com.github.vok.karibudsl.flow.grid
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import kotlin.test.expect

class GridTest : DynaTest({

    beforeEach { MockVaadin.setup() }

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
})

data class TestPerson(val name: String, val age: Int)
