@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.data.ValueProvider
import com.vaadin.data.provider.*
import com.vaadin.shared.MouseEventDetails
import com.vaadin.shared.data.sort.SortDirection
import com.vaadin.ui.Button
import com.vaadin.ui.Component
import com.vaadin.ui.Grid
import com.vaadin.ui.TreeGrid
import com.vaadin.ui.renderers.ClickableRenderer
import com.vaadin.ui.renderers.ComponentRenderer
import kotlin.reflect.KProperty1
import kotlin.streams.toList
import kotlin.test.fail

/**
 * Returns the item on given row. Fails if the row index is invalid. The data provider is
 * sorted according to given [sortOrders] (empty by default) and filtered according
 * to given [filter] (null by default) first.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row.
 * @throws AssertionError if the row index is out of bounds.
 */
fun <T, F> DataProvider<T, F>._get(rowIndex: Int, sortOrders: List<QuerySortOrder> = listOf(), inMemorySorting: Comparator<T>? = null, filter: F? = null): T {
    require(rowIndex >= 0) { "rowIndex must be 0 or greater: $rowIndex" }
    val fetched = fetch(Query<T, F>(rowIndex, 1, sortOrders, inMemorySorting, filter))
    return fetched.toList().firstOrNull() ?: throw AssertionError("Requested to get row $rowIndex but the data provider only has ${_size(filter)} rows matching filter $filter")
}

/**
 * Returns all items in given data provider, sorted according to given [sortOrders] (empty by default) and filtered according
 * to given [filter] (null by default).
 * @return the list of items.
 */
fun <T, F> DataProvider<T, F>._findAll(sortOrders: List<QuerySortOrder> = listOf(), inMemorySorting: Comparator<T>? = null, filter: F? = null): List<T> {
    val fetched = fetch(Query<T, F>(0, Int.MAX_VALUE, sortOrders, inMemorySorting, filter))
    return fetched.toList()
}

/**
 * Returns the item on given row. Fails if the row index is invalid. Uses current Grid sorting.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row, not null.
 */
fun <T> Grid<T>._get(rowIndex: Int): T {
    val fetched = _fetch(rowIndex, 1)
    return fetched.firstOrNull() ?: throw AssertionError("Requested to get row $rowIndex but the data provider only has ${_size()} rows")
}

/**
 * For [TreeGrid] this walks the [_rowSequence].
 *
 * WARNING: Very slow operation for [TreeGrid].
 */
fun <T> Grid<T>._fetch(offset: Int, limit: Int): List<T> = when(this) {
    is TreeGrid<T> -> this._rowSequence().drop(offset).take(limit).toList()
    else -> dataCommunicator.fetchItemsWithRange(offset, limit)
}

/**
 * Returns all items in given data provider. Uses current Grid sorting.
 *
 * For [TreeGrid] this returns all displayed rows; skips children of collapsed nodes.
 * @return the list of items.
 */
fun <T> Grid<T>._findAll(): List<T> = _fetch(0, Int.MAX_VALUE)

/**
 * Returns the number of items in this data provider.
 *
 * In case of [HierarchicalDataProvider]
 * this returns the number of ALL items including all leafs.
 */
fun <T, F> DataProvider<T, F>._size(filter: F? = null): Int {
    if (this is HierarchicalDataProvider<T, F>) {
        return this._size(null, filter)
    }
    return size(Query(filter))
}

/**
 * Returns the number of items in this data provider, including child items.
 * The function traverses recursively until all children are found; then a total size
 * is returned. The function uses [HierarchicalDataProvider.size] mostly, but
 * also uses [HierarchicalDataProvider.fetchChildren] to discover children.
 * Only children matching [filter] are considered for recursive computation of
 * the size.
 *
 * Note that this can differ to `Grid._size()` since `Grid._size()` ignores children
 * of collapsed tree nodes.
 */
@JvmOverloads
fun <T, F> HierarchicalDataProvider<T, F>._size(parent: T? = null, filter: F? = null): Int {
    val query = HierarchicalQuery(filter, parent)
    val countOfDirectChildren: Int = size(query)
    val children: List<T> = fetchChildren(query).toList()
    val recursiveChildrenSizes: Int = children.sumBy { _size(it, filter) }
    return countOfDirectChildren + recursiveChildrenSizes
}

/**
 * Returns the number of items in this data provider.
 *
 * For [TreeGrid] this computes the number of items the [TreeGrid] is actually showing on-screen,
 * ignoring children of collapsed nodes.
 *
 * A very slow operation for [TreeGrid] since it walks through all items returned by [_rowSequence].
 */
fun Grid<*>._size(): Int = when(this) {
    is TreeGrid<*> -> this._size()
    else -> dataCommunicator.dataProviderSize
}

/**
 * Performs a click on a [ClickableRenderer] in given [Grid] cell. Fails if [Grid.Column.getRenderer] is not a [ClickableRenderer].
 * @receiver the grid, not null.
 * @param rowIndex the row index, 0 or higher.
 * @param columnId the column ID.
 */
@JvmOverloads
fun <T : Any> Grid<T>._clickRenderer(rowIndex: Int, columnId: String, mouseEventDetails: MouseEventDetails = MouseEventDetails(),
                                     click: (Component)->Unit = { component ->
                                         fail("${this.toPrettyString()} column $columnId: ClickableRenderer produced ${component.toPrettyString()} which is not a button - you need to provide your own custom 'click' closure which knows how to click this component")
                                     }) {
    checkEditableByUser()
    val column = getColumnById(columnId)
    val renderer = column.renderer
    val item: T = _get(rowIndex)
    if (renderer is ClickableRenderer<*, *>) {
        @Suppress("UNCHECKED_CAST")
        (renderer as ClickableRenderer<T, *>)._fireEvent(object : ClickableRenderer.RendererClickEvent<T>(this, item, column, mouseEventDetails) {})
    } else if (renderer is ComponentRenderer) {
        val component = column.valueProvider.apply(item) as Component
        if (component is Button) {
            component._click()
        } else {
            click(component)
        }
    } else {
        fail("${this.toPrettyString()} column $columnId has renderer $renderer which is not supported by this method")
    }
}

/**
 * Returns the formatted value of a cell as a String. Does not use renderer to render the value - simply calls the value provider and presentation provider
 * and converts the result to string (even if the result is a [com.vaadin.ui.Component]).
 * @param rowIndex the row index, 0 or higher.
 * @param columnId the column ID.
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> Grid<T>._getFormatted(rowIndex: Int, columnId: String): String {
    val rowObject: T = dataProvider._get(rowIndex)
    val column: Grid.Column<T, *> = getColumnById(columnId)
    return column._getFormatted(rowObject)
}

/**
 * Returns the formatted value of a cell as a String. Does not use [renderer][Grid.Column.getRenderer] to render the value
 * - it only calls the [value provider][Grid.Column.getValueProvider] and [presentation provider][Grid.Column.presentationProvider]
 * and converts the result to string (even if the result is a [com.vaadin.ui.Component]).
 *
 * Calls [getPresentationValue] and converts the result to string; `null`s are converted to the string "null".
 * @param rowObject the row object. The object doesn't even have to be present in the Grid itself.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Grid.Column<T, *>._getFormatted(rowObject: T): String = "${getPresentationValue(rowObject)}"

/**
 * Returns the formatted value of a Grid row as a list of strings, one for every visible column. Calls [_getFormatted] to
 * obtain the formatted cell value.
 * @param rowIndex the row index, 0 or higher.
 */
fun <T> Grid<T>._getFormattedRow(rowObject: T): List<String> =
        columns.filterNot { it.isHidden }.map { it._getFormatted(rowObject) }

fun <T> Grid<T>._getFormattedRow(rowIndex: Int): List<String> {
    val rowObject: T = _get(rowIndex)
    return _getFormattedRow(rowObject)
}

/**
 * Returns the [Grid.Column]'s presentation provider. Never null, may be [ValueProvider.identity].
 */
@Suppress("UNCHECKED_CAST")
val <V> Grid.Column<*, V>.presentationProvider: ValueProvider<V, *>
    get() =
        javaClass.getDeclaredField("presentationProvider").run {
            isAccessible = true
            get(this@presentationProvider) as ValueProvider<V, *>
        }

/**
 * Returns the formatted value as outputted by the value provider + presentation provider. Does not use [renderer][com.vaadin.ui.Grid.Column.getRenderer] to render the value
 * - it only calls the [value provider][com.vaadin.ui.Grid.Column.getValueProvider] and [presentation provider][com.vaadin.ui.Grid.Column.presentationProvider].
 * @param rowObject the row object. The object doesn't even have to be present in the Grid itself.
 */
fun <T, V> Grid.Column<T, V>.getPresentationValue(rowObject: T): Any? = presentationProvider.apply(valueProvider.apply(rowObject))

private fun <T> Grid<T>.getSortIndicator(column: Grid.Column<T, *>): String {
    val so = sortOrder.firstOrNull { it.sorted == column }
    return when {
        so == null -> ""
        so.direction == SortDirection.ASCENDING -> "v"
        else -> "^"
    }
}

/**
 * Dumps the first [maxRows] rows of the Grid, formatting the values using the [_getFormatted] function. The output example:
 * ```
 * --[Name]--[Age]--[Occupation]--
 * 0: John, 25, Service Worker
 * 1: Fred, 40, Supervisor
 * --and 198 more
 * ```
 */
@JvmOverloads
fun <T: Any> Grid<T>._dump(rows: IntRange = 0..10): String = buildString {
    val visibleColumns: List<Grid.Column<T, *>> = columns.filterNot { it.isHidden }
    visibleColumns.joinTo(this, prefix = "--", separator = "-", postfix = "--\n") { "[${it.caption}]${getSortIndicator(it)}" }
    val dsIndices: IntRange
    val displayIndices: Set<Int>
    if (this@_dump is TreeGrid<T>) {
        val tree: PrettyPrintTree = this@_dump._dataSourceToPrettyTree()
        val lines = tree.print().split('\n').filterNotBlank().drop(1)
        dsIndices = lines.indices
        displayIndices = rows.intersect(dsIndices)
        for (i in displayIndices) {
            append("$i: ${lines[i]}\n")
        }
    } else {
        dsIndices = 0 until _size()
        displayIndices = rows.intersect(dsIndices)
        for (i in displayIndices) {
            _getFormattedRow(i).joinTo(this, prefix = "$i: ", postfix = "\n")
        }
    }
    val andMore = dsIndices.size - displayIndices.size
    if (andMore > 0) {
        append("--and $andMore more\n")
    }
}

/**
 * Expects that [Grid.getDataProvider] currently contains exactly expected [count] of items. Fails if not; [_dump]s
 * first 10 rows of the Grid on failure.
 */
fun Grid<*>.expectRows(count: Int) {
    if (_size() != count) {
        throw AssertionError("${this.toPrettyString()}: expected $count rows\n${_dump()}")
    }
}

/**
 * Expects that the row at [rowIndex] looks exactly as [expected].
 */
@Suppress("NAME_SHADOWING")
fun Grid<*>.expectRow(rowIndex: Int, vararg expected: String) {
    val expected = expected.toList()
    val actual = _getFormattedRow(rowIndex)
    if (expected != actual) {
        throw AssertionError("${this.toPrettyString()} at $rowIndex: expected $expected but got $actual\n${_dump()}")
    }
}

/**
 * Fires the [com.vaadin.ui.Grid.ItemClick] event for given [rowIndex] which invokes all [com.vaadin.ui.components.grid.ItemClickListener]s registered via
 * [Grid.addItemClickListener].
 * @param column click this column; defaults to the first visible column in the Grid since it often doesn't really matter
 * which column was clicked - only the row index matters.
 * @param mouseEventDetails optionally mock mouse buttons and/or keyboard modifiers here.
 */
@JvmOverloads
fun <T> Grid<T>._clickItem(rowIndex: Int, column: Grid.Column<T, *> = columns.first { !it.isHidden } ,
                           mouseEventDetails: MouseEventDetails = MouseEventDetails()) {
    checkEditableByUser()
    _fireEvent(Grid.ItemClick(this, column, _get(rowIndex), mouseEventDetails, rowIndex))
}

/**
 * Retrieves the column for given [columnId].
 * @throws IllegalArgumentException if no such column exists.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Grid<T>.getColumnById(columnId: String): Grid.Column<T, *> =
        getColumn(columnId) as Grid.Column<T, *>?
                ?: throw IllegalArgumentException("${this.toPrettyString()}: No column with ID '$columnId'; available column IDs: ${columns.mapNotNull { it.id }}")

@Deprecated("replaced by getColumnById()", replaceWith = ReplaceWith("getColumnById(columnId)"))
fun <T> Grid<T>.getColumnBy(columnId: String): Grid.Column<T, *> = getColumnById(columnId)

/**
 * Returns the component in given grid cell at [rowIndex]/[columnId]. Fails if there is something else in the cell (e.g. a String or other
 * value).
 *
 * **WARNING**: This function doesn't return the button produced by [com.vaadin.ui.renderers.ButtonRenderer]! There must be an actual component
 * produced by [Grid.Column.getValueProvider], possibly fed to [com.vaadin.ui.renderers.ComponentRenderer].
 */
fun <T> Grid<T>._getComponentAt(rowIndex: Int, columnId: String): Component {
    val item = _get(rowIndex)
    val column = getColumnById(columnId)
    val possibleComponent = column.getPresentationValue(item)
    if (possibleComponent !is Component) {
        fail("Expected Component at $rowIndex/$columnId but got $possibleComponent")
    }
    return possibleComponent
}

val KProperty1<*, *>.asc get() = QuerySortOrder(name, SortDirection.ASCENDING)
val KProperty1<*, *>.desc get() = QuerySortOrder(name, SortDirection.DESCENDING)

/**
 * Sorts given grid. Affects [_findAll], [_get] and other data-fetching functions.
 */
fun <T> Grid<T>.sort(vararg sortOrder: QuerySortOrder) {
    setSortOrder(sortOrder.map { GridSortOrder(getColumnById(it.sorted), it.direction) })
}

/**
 * Returns a sequence which walks over all rows the [TreeGrid] is actually showing.
 * The sequence will *skip* children of collapsed nodes.
 *
 * Iterating the entire sequence is a very slow operation since it will repeatedly
 * poll [HierarchicalDataProvider] for list of children.
 *
 * Honors current grid ordering.
 */
fun <T> TreeGrid<T>._rowSequence(): Sequence<T> {

    @Suppress("UNCHECKED_CAST")
    fun getChildrenOf(item: T): List<T> {
        return if (isExpanded(item)) {
            (dataProvider as HierarchicalDataProvider<T, Nothing?>)
                    .fetchChildren(HierarchicalQuery(null, item)).toList()
        } else {
            listOf<T>()
        }
    }

    fun itemSubtreeSequence(item: T): Sequence<T> =
            PreorderTreeIterator(item) { getChildrenOf(it) } .asSequence()

    val roots: List<T> = _getRootItems()
    return roots.map { itemSubtreeSequence(it) } .asSequence().flatten()
}

/**
 * Returns the number of items the [TreeGrid] is actually showing. For example
 * it doesn't count in children of collapsed nodes.
 *
 * A very slow operation since it walks through all items returned by [_rowSequence].
 */
fun TreeGrid<*>._size(): Int = _rowSequence().count()

fun <T> TreeGrid<T>._dataSourceToPrettyTree(): PrettyPrintTree {
    @Suppress("UNCHECKED_CAST")
    fun getChildrenOf(item: T): List<T> {
        return if (isExpanded(item)) {
            (dataProvider as HierarchicalDataProvider<T, Nothing?>)
                    .fetchChildren(HierarchicalQuery(null, item)).toList()
        } else {
            listOf<T>()
        }
    }

    fun toPrettyTree(item: T): PrettyPrintTree {
        val self: String = _getFormattedRow(item).joinToString(postfix = "\n")
        val children: List<T> = getChildrenOf(item)
        return PrettyPrintTree(self, children.map { toPrettyTree(it) } .toMutableList())
    }

    val roots: List<T> = _getRootItems()
    return PrettyPrintTree("TreeGrid", roots.map { toPrettyTree(it) } .toMutableList())
}

@Suppress("UNCHECKED_CAST")
fun <T> TreeGrid<T>._getRootItems(): List<T> = (dataProvider as HierarchicalDataProvider<T, Nothing?>)
            .fetch(HierarchicalQuery(null, null))
            .toList()

/**
 * Expands all nodes. May invoke massive data loading.
 */
fun <T> TreeGrid<T>._expandAll(depth: Int = 100) {
    expandRecursively(_getRootItems(), depth)
}
