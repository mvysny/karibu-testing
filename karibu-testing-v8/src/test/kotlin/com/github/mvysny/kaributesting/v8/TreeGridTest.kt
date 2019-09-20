package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.karibudsl.v8.addColumnFor
import com.vaadin.data.TreeData
import com.vaadin.data.provider.TreeDataProvider
import com.vaadin.shared.MouseEventDetails
import com.vaadin.ui.TreeGrid
import com.vaadin.ui.renderers.ButtonRenderer
import kotlin.test.expect

class TreeGridTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("HierarchicalDataProvider") {
        group("_size") {
            test("simple") {
                expect(20) { treedp((0 until 20).toList())._size() }
            }
            test("size calculates sizes of all nodes") {
                expect(10) { treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() })._size() }
            }
        }
    }

    group("TreeGrid") {
        group("_size") {
            test("0 on empty grid") {
                expect(0) { TreeGrid<String>()._size() }
            }
            test("simple") {
                expect(20) {
                    val g = TreeGrid<Int>()
                    g.dataProvider = treedp((0 until 20).toList())
                    g._size()
                }
            }
            test("size calculates sizes of all nodes") {
                expect(10) {
                    val g = TreeGrid<Int>()
                    g.dataProvider = treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() })
                    g.expandRecursively(listOf(0), 10)
                    g._size()
                }
            }
            test("size ignores collapsed nodes") {
                expect(1) {
                    val g = TreeGrid<Int>()
                    g.dataProvider = treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() })
                    // all nodes are by default collapsed
                    g._size()
                }
            }
        }
        group("_get") {
            test("degenerate tree") {
                val roots = listOf(TestPerson("name 0", 0))
                val grid = TreeGrid<TestPerson>().apply {
                    addColumnFor(TestPerson::name)
                    addColumnFor(TestPerson::age)
                    dataProvider = treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() })
                    _expandAll()
                }
                grid.expectRow(0, "name 0", "0")
                grid.expectRow(1, "name 1", "1")
                grid.expectRow(9, "name 9", "9")
            }
            test("balanced tree") {
                val roots = listOf(TestPerson("name 0", 0))
                val grid = TreeGrid<TestPerson>().apply {
                    addColumnFor(TestPerson::name)
                    addColumnFor(TestPerson::age)
                    dataProvider = treedp<TestPerson>(roots) {
                        if (it.age < 3) listOf(TestPerson("${it.name} 0", it.age + 1), TestPerson("${it.name} 1", it.age + 1))
                        else listOf<TestPerson>()
                    }
                    _expandAll()
                }
                // expected tree:
                // --[Name]-[Age]--
                //0:     └── name 0, 0
                //1:         ├── name 0 0, 1
                //2:         │   ├── name 0 0 0, 2
                //3:         │   │   ├── name 0 0 0 0, 3
                //4:         │   │   └── name 0 0 0 1, 3
                //5:         │   └── name 0 0 1, 2
                //6:         │       ├── name 0 0 1 0, 3
                grid.expectRow( 0, "name 0", "0")
                grid.expectRow( 1, "name 0 0", "1")
                grid.expectRow( 2, "name 0 0 0", "2")
                grid.expectRow( 3, "name 0 0 0 0", "3")
                grid.expectRow( 4, "name 0 0 0 1", "3")
                grid.expectRow( 5, "name 0 0 1", "2")
                grid.expectRow( 6, "name 0 0 1 0", "3")
                grid.expectRow( 7, "name 0 0 1 1", "3")
                grid.expectRow( 8, "name 0 1", "1")
                grid.expectRow( 9, "name 0 1 0", "2")
                grid.expectRow(10, "name 0 1 0 0", "3")
                grid.expectRow(11, "name 0 1 0 1", "3")
                grid.expectRow(12, "name 0 1 1", "2")
                grid.expectRow(13, "name 0 1 1 0", "3")
                grid.expectRow(14, "name 0 1 1 1", "3")
            }
        }
        test("_dump") {
            val roots = listOf(TestPerson("name 0", 0))
            val grid = TreeGrid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                dataProvider = treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() })
            }
            expect("""--[Name]-[Age]--
0:     └── name 0, 0
""") {
                grid._dump()
            }
            grid.expandRecursively(roots, 10)
            expect("""--[Name]-[Age]--
0:     └── name 0, 0
1:         └── name 1, 1
2:             └── name 2, 2
3:                 └── name 3, 3
4:                     └── name 4, 4
5:                         └── name 5, 5
6:                             └── name 6, 6
--and 3 more
""") {
                grid._dump(0..6)
            }
        }
        test("click renderer") {
            val roots = listOf(TestPerson("name 0", 0))
            var called = false
            val grid = TreeGrid<TestPerson>().apply {
                addColumnFor(TestPerson::name) {
                    setRenderer(ButtonRenderer<TestPerson> { e ->
                        called = true
                        expect("name 8") { e.item.name }
                        expect("name") { e.column.id }
                        expect(true) { e.mouseEventDetails.isCtrlKey }
                    })
                }
                dataProvider = treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() })
            }
            grid._expandAll()
            grid._clickRenderer(8, "name", MouseEventDetails().apply { isCtrlKey = true })
            expect(true) { called }
        }
    }
})

private fun <T> treedp(roots: List<T>, childProvider: (T) -> List<T> = { listOf() }): TreeDataProvider<T> =
        TreeDataProvider(TreeData<T>().addItems(roots, childProvider))
