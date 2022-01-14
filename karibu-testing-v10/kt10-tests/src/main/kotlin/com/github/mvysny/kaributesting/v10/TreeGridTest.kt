package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v10.addColumnFor
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import java.util.stream.Stream
import kotlin.test.expect

@DynaTestDsl
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

    group("lying HierarchicalDataProvider") {
        test("hasChildren=false but returns children") {
            val lyingHDP = object : AbstractBackEndHierarchicalDataProvider<Int, Nothing?>() {
                override fun hasChildren(item: Int): Boolean = false
                override fun fetchChildrenFromBackEnd(query: HierarchicalQuery<Int, Nothing?>): Stream<Int> {
                    val p = query.parent ?: 0
                    return (if (p >= 3) listOf<Int>() else listOf(p + 1)).stream()
                }
                override fun getChildCount(query: HierarchicalQuery<Int, Nothing?>): Int = 1
            }
            expect(1) { lyingHDP._size() }
        }
        test("size=0 but returns root items") {
            val lyingHDP = object : AbstractBackEndHierarchicalDataProvider<Int, Nothing?>() {
                override fun hasChildren(item: Int): Boolean = true
                override fun fetchChildrenFromBackEnd(query: HierarchicalQuery<Int, Nothing?>): Stream<Int> =
                        (if (query.parent != null) listOf<Int>() else listOf(0)).stream()
                override fun getChildCount(query: HierarchicalQuery<Int, Nothing?>): Int = 0
            }
            expect(0) { lyingHDP._size() }
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
                    g._expandAll()
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
                    addHierarchyColumn { it -> it.name }
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
                    addHierarchyColumn { it.name }
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
        test("_dump balanced tree") {
            val roots = listOf(TestPerson("name 0", 0))
            val grid = TreeGrid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                dataProvider = treedp<TestPerson>(roots) {
                    if (it.age < 3) listOf(TestPerson("${it.name} 0", it.age + 1), TestPerson("${it.name} 1", it.age + 1))
                    else listOf<TestPerson>()
                }
            }
            expect("""--[Name]-[Age]--
0:     └── name 0, 0
""") {
                grid._dump()
            }
            grid._expandAll()
            expect("""--[Name]-[Age]--
0:     └── name 0, 0
1:         ├── name 0 0, 1
2:         │   ├── name 0 0 0, 2
3:         │   │   ├── name 0 0 0 0, 3
4:         │   │   └── name 0 0 0 1, 3
5:         │   └── name 0 0 1, 2
6:         │       ├── name 0 0 1 0, 3
--and 8 more
""") {
                grid._dump(0..6)
            }
        }
        test("clickable renderer") {
            val roots = listOf(TestPerson("name 0", 0))
            var called = false
            val grid = TreeGrid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { person ->
                    called = true
                    expect("name 8") { person.name }
                }).key = "name"
                dataProvider = treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() })
            }
            grid._expandAll()
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
    }
}

private fun <T> treedp(roots: List<T>, childProvider: (T) -> List<T> = { listOf() }): TreeDataProvider<T> =
        TreeDataProvider(TreeData<T>().addItems(roots, childProvider))
