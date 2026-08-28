package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.addColumnFor
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import com.vaadin.flow.function.SerializablePredicate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.stream.Stream
import kotlin.test.expect

abstract class AbstractTreeGridTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class HierarchicalDataProviderTests {
        @Nested inner class _size {
            @Test fun simple() {
                expect(20) { treedp((0 until 20).toList())._size() }
            }
            @Test fun `size calculates sizes of all nodes`() {
                expect(10) { treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() })._size() }
            }
        }
    }

    @Nested inner class `lying HierarchicalDataProvider` {
        @Test fun `hasChildren=false but returns children`() {
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
        @Test fun `size=0 but returns root items`() {
            val lyingHDP = object : AbstractBackEndHierarchicalDataProvider<Int, Nothing?>() {
                override fun hasChildren(item: Int): Boolean = true
                override fun fetchChildrenFromBackEnd(query: HierarchicalQuery<Int, Nothing?>): Stream<Int> =
                        (if (query.parent != null) listOf<Int>() else listOf(0)).stream()
                override fun getChildCount(query: HierarchicalQuery<Int, Nothing?>): Int = 0
            }
            expect(0) { lyingHDP._size() }
        }
    }

    @Nested inner class TreeGridTests {
        @Nested inner class _size {
            @Test fun `0 on empty grid`() {
                expect(0) { TreeGrid<String>()._size() }
            }
            @Test fun simple() {
                expect(20) {
                    val g = TreeGrid<Int>()
                    g.setDataProvider(treedp((0 until 20).toList()))
                    g._size()
                }
            }
            @Test fun `size calculates sizes of all nodes`() {
                expect(10) {
                    val g = TreeGrid<Int>()
                    g.setDataProvider(treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() }))
                    g._expandAll()
                    g._size()
                }
            }
            @Test fun `size ignores collapsed nodes`() {
                expect(1) {
                    val g = TreeGrid<Int>()
                    g.setDataProvider(treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() }))
                    // all nodes are by default collapsed
                    g._size()
                }
            }
        }
        @Nested inner class _get {
            @Test fun `degenerate tree`() {
                val roots = listOf(TestPerson("name 0", 0))
                val grid = TreeGrid<TestPerson>().apply {
                    addHierarchyColumn { it -> it.name }
                    addColumnFor(TestPerson::age)
                    setDataProvider(treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() }))
                    _expandAll()
                }
                grid.expectRow(0, "name 0", "0")
                grid.expectRow(1, "name 1", "1")
                grid.expectRow(9, "name 9", "9")
            }
            @Test fun `balanced tree`() {
                val roots = listOf(TestPerson("name 0", 0))
                val grid = TreeGrid<TestPerson>().apply {
                    addHierarchyColumn { it.name }
                    addColumnFor(TestPerson::age)
                    setDataProvider(treedp<TestPerson>(roots) {
                        if (it.age < 3) listOf(TestPerson("${it.name} 0", it.age + 1), TestPerson("${it.name} 1", it.age + 1))
                        else listOf<TestPerson>()
                    })
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
        @Nested inner class _rowStream {
            @Test fun `empty on empty grid`() {
                expect(listOf()) { TreeGrid<String>()._rowStream().toList() }
            }
            @Test fun simple() {
                val g = TreeGrid<Int>()
                g.setDataProvider(treedp((0 until 20).toList()))
                expect((0 until 20).toList()) { g._rowStream().toList() }
            }
            @Test fun `ignores collapsed nodes`() {
                val g = TreeGrid<Int>()
                g.setDataProvider(treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() }))
                // all nodes are by default collapsed
                expect(listOf(0)) { g._rowStream().toList() }
                g._expandAll()
                expect((0..9).toList()) { g._rowStream().toList() }
            }
            @Test fun `honors filter`() {
                val g = TreeGrid<Int>()
                g.setDataProvider(treedp((0 until 20).toList()))
                expect(listOf(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)) {
                    g._rowStream { it % 2 == 0 }.toList()
                }
            }
            @Test fun `stops early without walking the rest of the tree`() {
                // the point of returning a Stream rather than a List: limit() must not
                // poll the data provider for the whole tree.
                val dp = CountingTreeDataProvider(
                    TreeData<Int>().addItems(listOf(0)) { if (it < 9) listOf(it + 1) else listOf() })
                val g = TreeGrid<Int>()
                g.setDataProvider(dp)
                g._expandAll()
                val fetchesForFullWalk: Int = dp.fetchChildrenCalls.let { before ->
                    g._rowStream().toList(); dp.fetchChildrenCalls - before
                }
                val before: Int = dp.fetchChildrenCalls
                expect(listOf(0, 1, 2)) { g._rowStream().limit(3).toList() }
                val fetchesForLimit3: Int = dp.fetchChildrenCalls - before
                expect(true, "limit(3) polled the dataprovider $fetchesForLimit3 times, " +
                        "a full walk takes $fetchesForFullWalk - the stream isn't lazy") {
                    fetchesForLimit3 < fetchesForFullWalk
                }
            }
            @Test fun `same contents as _rowSequence and _findAll`() {
                val g = TreeGrid<Int>()
                g.setDataProvider(treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() }))
                g._expandAll()
                val expected = g._rowSequence().toList()
                expect(expected) { g._rowStream().toList() }
                expect(expected) { g._findAll() }
            }
            @Test fun `filtering the walk is not the same as filtering the _findAll result`() {
                // 0 -> 1 -> ... -> 9. The filter goes into the HierarchicalQuery at every
                // level, and TreeDataProvider keeps an item whose *descendant* matches - so
                // filtering for 9 retains its whole ancestor chain. Filtering the flat list
                // afterwards cannot express that.
                val g = TreeGrid<Int>()
                g.setDataProvider(treedp(listOf(0), { if (it < 9) listOf(it + 1) else listOf<Int>() }))
                g._expandAll()
                expect((0..9).toList()) { g._rowStream { it == 9 }.toList() }
                expect(listOf(9)) { g._findAll().filter { it == 9 } }
            }
        }
        @Test fun _dump() {
            val roots = listOf(TestPerson("name 0", 0))
            val grid = TreeGrid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setDataProvider(treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() }))
                _prepare()
            }
            expect("""TreeGrid[dataprovider='TreeDataProvider<TestPerson>(? items)']
--[Name]-[Age]--
0:     └── name 0, 0
""") {
                grid._dump()
            }
            grid.expandRecursively(roots, 10)
            expect("""TreeGrid[dataprovider='TreeDataProvider<TestPerson>(? items)']
--[Name]-[Age]--
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
            // the (from, toInclusive) overload exists for Java callers; must match the IntRange one
            expect(grid._dump(0..6)) { grid._dump(0, 6) }
        }
        @Test fun `_dump balanced tree`() {
            val roots = listOf(TestPerson("name 0", 0))
            val grid = TreeGrid<TestPerson>().apply {
                addColumnFor(TestPerson::name)
                addColumnFor(TestPerson::age)
                setDataProvider(treedp<TestPerson>(roots) {
                    if (it.age < 3) listOf(TestPerson("${it.name} 0", it.age + 1), TestPerson("${it.name} 1", it.age + 1))
                    else listOf<TestPerson>()
                })
                _prepare()
            }
            expect("""TreeGrid[dataprovider='TreeDataProvider<TestPerson>(? items)']
--[Name]-[Age]--
0:     └── name 0, 0
""") {
                grid._dump()
            }
            grid._expandAll()
            expect("""TreeGrid[dataprovider='TreeDataProvider<TestPerson>(? items)']
--[Name]-[Age]--
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
        @Test fun `clickable renderer`() {
            val roots = listOf(TestPerson("name 0", 0))
            var called = false
            val grid = TreeGrid<TestPerson>().apply {
                addColumn(NativeButtonRenderer<TestPerson>("View") { person ->
                    called = true
                    expect("name 8") { person.name }
                }).key = "name"
                setDataProvider(treedp<TestPerson>(roots, { if (it.age < 9) listOf(TestPerson("name ${it.age + 1}", it.age + 1)) else listOf<TestPerson>() }))
            }
            grid._expandAll()
            grid._clickRenderer(8, "name")
            expect(true) { called }
        }
    }
}

private fun <T> treedp(roots: List<T>, childProvider: (T) -> List<T> = { listOf() }): TreeDataProvider<T> =
        TreeDataProvider(TreeData<T>().addItems(roots, childProvider))

/**
 * A [TreeDataProvider] which counts the number of times it has been polled for children,
 * so that a test can tell a lazy walk from an eager one.
 */
private class CountingTreeDataProvider<T>(data: TreeData<T>) : TreeDataProvider<T>(data) {
    var fetchChildrenCalls: Int = 0
    override fun fetchChildren(query: HierarchicalQuery<T, SerializablePredicate<T>>): Stream<T> {
        fetchChildrenCalls++
        return super.fetchChildren(query)
    }
}
