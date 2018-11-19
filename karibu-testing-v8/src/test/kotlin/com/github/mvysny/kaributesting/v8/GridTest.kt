package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.karibudsl.v8.addColumnFor
import com.github.mvysny.karibudsl.v8.getColumnBy
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Grid
import com.vaadin.ui.TextField
import kotlin.test.expect

class GridTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("_size") {
        expect(20) { ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._size() }
    }

    test("_get") {
        expect("name 5") { ListDataProvider<TestPerson>((0 until 20).map { TestPerson("name $it", it) })._get(5).name }
    }

    test("_findAll") {
        val list = (0 until 20).map { TestPerson("name $it", it) }
        expect(list) { ListDataProvider<TestPerson>(list)._findAll() }
    }

    test("_dump") {
        val dp = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.removeColumn("name")
        grid.addColumn({ it.name }, { it.toUpperCase() }).apply { id = "name"; caption = "The Name" }
        grid.dataProvider = dp
        grid.setColumns("name", "age")
        expect("--[The Name]-[Age]--\n--and 7 more\n") { grid._dump(0 until 0) }
        expect("--[The Name]-[Age]--\n0: NAME 0, 0\n1: NAME 1, 1\n2: NAME 2, 2\n3: NAME 3, 3\n4: NAME 4, 4\n--and 2 more\n") { grid._dump(0 until 5) }
        expect("--[The Name]-[Age]--\n0: NAME 0, 0\n1: NAME 1, 1\n2: NAME 2, 2\n3: NAME 3, 3\n4: NAME 4, 4\n5: NAME 5, 5\n6: NAME 6, 6\n") { grid._dump(0 until 20) }
    }

    test("expectRow()") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.dataProvider = ListDataProvider<TestPerson>((0 until 7).map { TestPerson("name $it", it) })
        grid.expectRows(7)
        grid.expectRow(0, "name 0", "0")
    }

    test("lookup finds components in header") {
        val grid = Grid<TestPerson>(TestPerson::class.java)
        grid.getHeaderRow(0).getCell(TestPerson::age.name).component = TextField("Foo!")
        expect("Foo!") { grid._get<TextField>().caption }
    }

    test("click item") {
        val grid = Grid<TestPerson>().apply {
            addColumnFor(TestPerson::name)
            addColumnFor(TestPerson::age)
            setItems((0..10).map { TestPerson("name $it", it) })
        }
        var called = false
        grid.addItemClickListener { e ->
            expect(grid.getColumnBy(TestPerson::name)) { e.column }
            expect("name 5") { e.item.name }
            expect(5) { e.rowIndex }
            called = true
        }
        grid._clickItem(5)
        expect(true) { called }
    }
})

data class TestPerson(val name: String, val age: Int)
