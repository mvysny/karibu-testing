@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.data.ValueProvider
import com.vaadin.data.provider.DataProvider
import com.vaadin.data.provider.Query
import com.vaadin.shared.MouseEventDetails
import com.vaadin.ui.Component
import com.vaadin.ui.Grid
import com.vaadin.ui.renderers.ClickableRenderer
import kotlin.streams.toList
import kotlin.test.fail

/**
 * Returns the item on given row. Fails if the row index is invalid.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row.
 */
fun <T> DataProvider<T, *>._get(rowIndex: Int): T {
    @Suppress("UNCHECKED_CAST")
    val fetched = (this as DataProvider<T, Any?>).fetch(Query<T, Any?>(rowIndex, 1, null, null, null))
    return fetched.toList().first()
}

/**
 * Returns all items in given data provider.
 * @return the list of items.
 */
fun <T> DataProvider<T, *>._findAll(): List<T> {
    @Suppress("UNCHECKED_CAST")
    val fetched = (this as DataProvider<T, Any?>).fetch(Query<T, Any?>(0, Int.MAX_VALUE, null, null, null))
    return fetched.toList()
}

/**
 * Returns the item on given row. Fails if the row index is invalid.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row, not null.
 */
fun <T> Grid<T>._get(rowIndex: Int): T = dataProvider._get(rowIndex)

/**
 * Returns all items in given data provider.
 * @return the list of items.
 */
fun <T> Grid<T>._findAll(): List<T> = dataProvider._findAll()

/**
 * Returns the number of items in this data provider.
 */
@Suppress("UNCHECKED_CAST")
fun DataProvider<*, *>._size(): Int =
        (this as DataProvider<Any?, Any?>).size(Query(null))

/**
 * Returns the number of items in this data provider.
 */
fun Grid<*>._size(): Int = dataProvider._size()

/**
 * Performs a click on a [ClickableRenderer] in given [Grid] cell. Fails if [Grid.Column.getRenderer] is not a [ClickableRenderer].
 * @receiver the grid, not null.
 * @param rowIndex the row index, 0 or higher.
 * @param columnId the column ID.
 */
@JvmOverloads
fun <T : Any> Grid<T>._clickRenderer(rowIndex: Int, columnId: String, mouseEventDetails: MouseEventDetails = MouseEventDetails()) {
    val column = getColumnBy(columnId)
    @Suppress("UNCHECKED_CAST")
    val renderer = column.renderer as ClickableRenderer<T, *>
    val item = _get(rowIndex)
    renderer._fireEvent(object : ClickableRenderer.RendererClickEvent<T>(this, item, column, mouseEventDetails) {})
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
    val column: Grid.Column<T, *> = getColumn(columnId) ?: throw IllegalArgumentException("There is no column $columnId. Available columns: ${columns.map { it.id }}")
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
fun <T: Any> Grid.Column<T, *>._getFormatted(rowObject: T): String = "${getPresentationValue(rowObject)}"

/**
 * Returns the formatted value of a Grid row as a list of strings, one for every visible column. Calls [_getFormatted] to
 * obtain the formatted cell value.
 * @param rowIndex the row index, 0 or higher.
 */
fun <T: Any> Grid<T>._getFormattedRow(rowIndex: Int): List<String> {
    val rowObject: T = dataProvider._get(rowIndex)
    return columns.filterNot { it.isHidden } .map { it._getFormatted(rowObject) }
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

/**
 * Dumps the first [maxRows] rows of the Grid, formatting the values using the [_getFormatted] function. The output example:
 * ```
 * --[Name]--[Age]--[Occupation]--
 * 0: John, 25, Service Worker
 * 1: Fred, 40, Supervisor
 * --and 198 more
 * ```
 */
fun <T: Any> Grid<T>._dump(rows: IntRange = 0..10): String = buildString {
    val visibleColumns: List<Grid.Column<T, *>> = columns.filterNot { it.isHidden }
    visibleColumns.map { "[${it.caption}]" } .joinTo(this, prefix = "--", separator = "-", postfix = "--\n")
    val dsIndices: IntRange = 0 until dataProvider._size()
    val displayIndices = rows.intersect(dsIndices)
    for (i in displayIndices) {
        _getFormattedRow(i).joinTo(this, prefix = "$i: ", postfix = "\n")
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
    if (dataProvider._size() != count) {
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
 * @param column click this column; defaults to the first visible column in the Grid.
 */
fun <T> Grid<T>._clickItem(rowIndex: Int, column: Grid.Column<T, *> = columns.first { !it.isHidden } ,
                           mouseEventDetails: MouseEventDetails = MouseEventDetails()) {
    _fireEvent(Grid.ItemClick(this, column, _get(rowIndex), mouseEventDetails, rowIndex))
}

/**
 * Retrieves the column for given [columnId].
 * @throws IllegalArgumentException if no such column exists.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Grid<T>.getColumnBy(columnId: String): Grid.Column<T, *> =
        getColumn(columnId) as Grid.Column<T, *>?
                ?: throw IllegalArgumentException("No column with ID $columnId; available column IDs: ${columns.map { it.id }.filterNotNull()}")

/**
 * Returns the component in given grid cell at [rowIndex]/[columnId]. Fails if there is something else in the cell (e.g. a String or other
 * value).
 *
 * **WARNING**: This function doesn't return the button produced by [com.vaadin.ui.renderers.ButtonRenderer]! There must be an actual component
 * produced by [Grid.Column.getValueProvider], possibly fed to [com.vaadin.ui.renderers.ComponentRenderer].
 */
fun <T> Grid<T>._getComponentAt(rowIndex: Int, columnId: String): Component {
    val item = _get(rowIndex)
    val column = getColumnBy(columnId)
    val possibleComponent = column.getPresentationValue(item)
    if (possibleComponent !is Component) {
        fail("Expected Component at $rowIndex/$columnId but got $possibleComponent")
    }
    return possibleComponent
}
