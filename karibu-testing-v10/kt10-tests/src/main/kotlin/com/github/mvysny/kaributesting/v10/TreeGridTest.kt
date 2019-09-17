package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.github.mvysny.karibudsl.v10.grid
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import kotlin.test.expect

internal fun DynaNodeGroup.treeGridTestbatch() {

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
        test("_get") {
            val roots = listOf(TestPerson("0", 0))
            val grid = TreeGrid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                dataProvider = treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("", it.age + 1)) else listOf<TestPerson>() })
                expandRecursively(roots, 10)
            }
            grid.expectRow(0, "0", "0")
            grid.expectRow(1, "", "1")
            grid.expectRow(9, "", "9")
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
    }
}

private fun <T> treedp(roots: List<T>, childProvider: (T) -> List<T> = { listOf() }): TreeDataProvider<T> =
        TreeDataProvider(TreeData<T>().addItems(roots, childProvider))
