@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.FooterRow
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.grid.HeaderRow
import com.vaadin.flow.data.provider.*
import com.vaadin.flow.data.renderer.ClickableRenderer
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.Renderer
import elemental.json.Json
import elemental.json.JsonValue
import java.util.stream.Stream
import kotlin.reflect.KProperty1
import kotlin.streams.toList

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
    require(rowIndex >= 0) { "rowIndex must be 0 or greater: $rowIndex" }
    val fetched = _fetch(rowIndex, 1)
    return fetched.firstOrNull() ?: throw AssertionError("Requested to get row $rowIndex but the data provider only has ${_size()}")
}

fun <T> Grid<T>._fetch(offset: Int, limit: Int): List<T> = dataCommunicator.fetch(offset, limit)

fun <T> DataCommunicator<T>.fetch(offset: Int, limit: Int): List<T> {
    val m = DataCommunicator::class.java.getDeclaredMethod("fetchFromProvider", Int::class.java, Int::class.java).apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST") val fetched: Stream<T> = m.invoke(this, offset, limit) as Stream<T>
    return fetched.toList()
}

/**
 * Returns all items in given data provider. Uses current Grid sorting.
 * @return the list of items.
 */
fun <T> Grid<T>._findAll(): List<T> = _fetch(0, Int.MAX_VALUE)

/**
 * Returns the number of items in this data provider.
 */
fun <T, F> DataProvider<T, F>._size(filter: F? = null): Int = size(Query(filter))

/**
 * Returns the number of items in this data provider.
 */
fun Grid<*>._size(): Int {
    val m = DataCommunicator::class.java.getDeclaredMethod("getDataProviderSize").apply { isAccessible = true }
    return m.invoke(dataCommunicator) as Int
}

/**
 * Gets a [Grid.Column] of this grid by its [columnKey].
 * @throws IllegalArgumentException if no such column exists.
 */
fun <T> Grid<T>._getColumnByKey(columnKey: String): Grid.Column<T> =
        requireNotNull(getColumnByKey(columnKey)) { "No such column with key '$columnKey'; available columns: ${columns.mapNotNull { it.key }}" }

/**
 * Performs a click on a [ClickableRenderer] in given [Grid] cell. Fails if [Grid.Column.renderer] is not a [ClickableRenderer].
 *
 * WARNING: Only supported for Vaadin 12 and higher.
 * @param rowIndex the row index, 0 or higher.
 * @param columnKey the column key [Grid.Column.getKey]
 */
fun <T : Any> Grid<T>._clickRenderer(rowIndex: Int, columnKey: String) {
    val column = _getColumnByKey(columnKey)
    @Suppress("UNCHECKED_CAST")
    val renderer = column.renderer as ClickableRenderer<T>
    val item = _get(rowIndex)
    renderer.onClick(item)
}

/**
 * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
 * and converts the result to string (even if the result is a [Component]).
 * @param rowIndex the row index, 0 or higher.
 * @param columnId the column ID.
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> Grid<T>._getFormatted(rowIndex: Int, columnId: String): String {
    val rowObject: T = _get(rowIndex)
    val column: Grid.Column<T> = getColumnByKey(columnId) ?: throw IllegalArgumentException("There is no column $columnId. Available columns: ${columns.map { it.id }}")
    return column._getFormatted(rowObject)
}

/**
 * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
 * and converts the result to string (even if the result is a [Component]).
 * @param rowIndex the row index, 0 or higher.
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> Grid.Column<T>._getFormatted(rowObject: T): String = "${getPresentationValue(rowObject)}"

fun <T: Any> Grid<T>._getFormattedRow(rowIndex: Int): List<String> {
    val rowObject: T = _get(rowIndex)
    return columns.filter { it.isVisible } .map { it._getFormatted(rowObject) }
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> Grid.Column<T>.getPresentationValue(rowObject: T): Any? {
    val valueProviders = renderer.valueProviders
    val valueProvider = valueProviders[internalId2] ?: return null
    // there is no value provider for NativeButtonRenderer, just return null
    val value = valueProvider.apply(rowObject)
    return "" + value
}

@Suppress("UNCHECKED_CAST")
private val <T> Grid<T>.dataGenerator2: DataGenerator<T> get() = Grid::class.java.getDeclaredMethod("getDataGenerator").run {
    isAccessible = true
    invoke(this@dataGenerator2) as DataGenerator<T>
}

/**
 * Retrieves the renderer for given [Grid.Column].
 */
@Suppress("UNCHECKED_CAST")
val <T> Grid.Column<T>.renderer: Renderer<T> get() {
    check(Grid.Column::class.java.declaredMethods.any { it.name == "getRenderer" }) {
        "This functionality can only be used with Vaadin 12 or higher. It is not possible to retrieve Renderer from Grid.Column on Vaadin 11 and lower, because of missing getRenderer() function."
    }
    return Grid.Column::class.java.getDeclaredMethod("getRenderer").run {
        invoke(this@renderer) as Renderer<T>
    }
}

@Suppress("UNCHECKED_CAST")
private val <T> Grid.Column<T>.internalId2: String get() = Grid.Column::class.java.getDeclaredMethod("getInternalId").run {
    isAccessible = true
    invoke(this@internalId2) as String
}

val Renderer<*>.template: String get() {
    val template = Renderer::class.java.getDeclaredField("template").run {
        isAccessible = true
        get(this@template) as String?
    }
    return template ?: ""
}

/**
 * Sets and retrieves the column header as set by [Grid.Column.setHeader] (String). The result value is undefined if a component has been set as the header.
 */
var <T> Grid.Column<T>.header2: String
    get() {
        val e: Renderer<*>? = Class.forName("com.vaadin.flow.component.grid.AbstractColumn").getDeclaredField("headerRenderer").run {
            isAccessible = true
            get(this@header2) as Renderer<*>?
        }

        return e?.template ?: ""
    }
    set(value) {
        setHeader(value)
    }

private fun <T> Grid<T>.getSortIndicator(column: Grid.Column<T>): String {
    val so = sortOrder.firstOrNull { it.sorted == column }
    return when {
        so == null -> ""
        so.direction == SortDirection.ASCENDING -> "v"
        else -> "^"
    }
}

/**
 * Dumps given range of [rows] of the Grid, formatting the values using the [_getFormatted] function. The output example:
 * ```
 * --[Name]--[Age]--[Occupation]--
 * 0: John, 25, Service Worker
 * 1: Fred, 40, Supervisor
 * --and 198 more
 * ```
 */
fun <T: Any> Grid<T>._dump(rows: IntRange = 0..10): String = buildString {
    val visibleColumns: List<Grid.Column<T>> = columns.filter { it.isVisible }
    visibleColumns.map { "[${it.header2}]${getSortIndicator(it)}" }.joinTo(this, prefix = "--", separator = "-", postfix = "--\n")
    val dsIndices: IntRange = 0 until _size()
    val displayIndices = rows.intersect(dsIndices)
    for (i in displayIndices) {
        _getFormattedRow(i).joinTo(this, prefix = "$i: ", postfix = "\n")
    }
    val andMore = dsIndices.size - displayIndices.size
    if (andMore > 0) {
        append("--and $andMore more\n")
    }
}

fun Grid<*>.expectRows(count: Int) {
    if (_size() != count) {
        throw AssertionError("${this.toPrettyString()}: expected $count rows\n${_dump()}")
    }
}

fun Grid<*>.expectRow(rowIndex: Int, vararg row: String) {
    val expected = row.toList()
    val actual = _getFormattedRow(rowIndex)
    if (expected != actual) {
        throw AssertionError("${this.toPrettyString()} at $rowIndex: expected $expected but got $actual\n${_dump()}")
    }
}

/**
 * Returns `com.vaadin.flow.component.grid.AbstractColumn`
 */
internal val HeaderRow.HeaderCell.column: Any
    get() {
        val getColumn = abstractCellClass.getDeclaredMethod("getColumn")
        getColumn.isAccessible = true
        return getColumn.invoke(this)
    }

private val abstractCellClass = Class.forName("com.vaadin.flow.component.grid.AbstractRow\$AbstractCell")
private val abstractColumnClass = Class.forName("com.vaadin.flow.component.grid.AbstractColumn")

/**
 * Returns `com.vaadin.flow.component.grid.AbstractColumn`
 */
private val FooterRow.FooterCell.column: Any
    get() {
        val getColumn = abstractCellClass.getDeclaredMethod("getColumn")
        getColumn.isAccessible = true
        return getColumn.invoke(this)
    }

/**
 * Retrieves the cell for given [property]; it matches [Grid.Column.getKey] to [KProperty1.name].
 * @return the corresponding cell
 * @throws IllegalArgumentException if no such column exists.
 */
fun HeaderRow.getCell(property: KProperty1<*, *>): HeaderRow.HeaderCell {
    val cell = cells.firstOrNull { (it.column as Grid.Column<*>).key == property.name }
    require(cell != null) { "This grid has no property named ${property.name}: $cells" }
    return cell
}

/**
 * Retrieves column key from the `AbstractColumn` receiver. The problem here is that receiver can be `ColumnGroup` which doesn't have
 * a key.
 */
private val Any.columnKey: String?
    get() {
        abstractColumnClass.cast(this)
        val method = abstractColumnClass.getDeclaredMethod("getBottomLevelColumn")
        method.isAccessible = true
        val gridColumn = method.invoke(this) as Grid.Column<*>
        return gridColumn.key
    }

/**
 * Retrieves the cell for given [property]; it matches [Grid.Column.getKey] to [KProperty1.name].
 * @return the corresponding cell
 * @throws IllegalArgumentException if no such column exists.
 */
fun FooterRow.getCell(property: KProperty1<*, *>): FooterRow.FooterCell {
    val cell = cells.firstOrNull { it.column.columnKey == property.name }
    require(cell != null) { "This grid has no property named ${property.name}: $cells" }
    return cell
}

val HeaderRow.HeaderCell.renderer: Renderer<*>?
    get() {
        val method = abstractColumnClass.getDeclaredMethod("getHeaderRenderer")
        method.isAccessible = true
        val renderer = method.invoke(column)
        return renderer as Renderer<*>?
    }

val FooterRow.FooterCell.renderer: Renderer<*>?
    get() {
        val method = abstractColumnClass.getDeclaredMethod("getFooterRenderer")
        method.isAccessible = true
        val renderer = method.invoke(column)
        return renderer as Renderer<*>?
    }

/**
 * Returns or sets the component in grid's footer cell. Returns `null` if the cell contains String, something else than a component or nothing at all.
 */
var FooterRow.FooterCell.component: Component?
    get() {
        val cr = (renderer as? ComponentRenderer<*, *>) ?: return null
        return cr.createComponent(null)
    }
    set(value) {
        setComponent(value)
    }

private val gridSorterComponentRendererClass = Class.forName("com.vaadin.flow.component.grid.GridSorterComponentRenderer")

/**
 * Returns or sets the component in grid's header cell. Returns `null` if the cell contains String, something else than a component or nothing at all.
 */
var HeaderRow.HeaderCell.component: Component?
    get() {
        val r = renderer
        if (!gridSorterComponentRendererClass.isInstance(r)) return null
        val componentField = gridSorterComponentRendererClass.getDeclaredField("component")
        componentField.isAccessible = true
        return componentField.get(r) as Component
    }
    set(value) {
        setComponent(value)
    }

val KProperty1<*, *>.asc get() = QuerySortOrder(name, SortDirection.ASCENDING)
val KProperty1<*, *>.desc get() = QuerySortOrder(name, SortDirection.DESCENDING)
/**
 * Sorts given grid. Affects [_findAll], [_get] and other data-fetching functions.
 */
fun <T> Grid<T>.sort(vararg sortOrder: QuerySortOrder) {
    sort(sortOrder.map { GridSortOrder(getColumnByKey(it.sorted), it.direction) })
}
