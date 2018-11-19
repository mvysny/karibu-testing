package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.FooterRow
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.HeaderRow
import com.vaadin.flow.data.provider.DataGenerator
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.data.renderer.TemplateRenderer
import com.vaadin.flow.dom.Element
import elemental.json.Json
import elemental.json.JsonValue
import kotlin.reflect.KProperty1
import kotlin.streams.toList

/**
 * Returns the item on given row. Fails if the row index is invalid.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row, not null.
 */
fun <T : Any> DataProvider<T, *>._get(rowIndex: Int): T {
    @Suppress("UNCHECKED_CAST")
    val fetched = (this as DataProvider<T, Any?>).fetch(Query<T, Any?>(rowIndex, 1, null, null, null))
    return fetched.toList().first()
}

/**
 * Returns all items in given data provider.
 * @return the list of items.
 */
fun <T : Any> DataProvider<T, *>._findAll(): List<T> {
    @Suppress("UNCHECKED_CAST")
    val fetched = (this as DataProvider<T, Any?>).fetch(Query<T, Any?>(0, Int.MAX_VALUE, null, null, null))
    return fetched.toList()
}

/**
 * Returns the item on given row. Fails if the row index is invalid.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row, not null.
 */
fun <T : Any> Grid<T>._get(rowIndex: Int): T = dataProvider._get(rowIndex)

/**
 * Returns all items in given data provider.
 * @return the list of items.
 */
fun <T : Any> Grid<T>._findAll(): List<T> = dataProvider._findAll()

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
 * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
 * and converts the result to string (even if the result is a [Component]).
 * @param rowIndex the row index, 0 or higher.
 * @param columnId the column ID.
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> Grid<T>._getFormatted(rowIndex: Int, columnId: String): String {
    val rowObject: T = dataProvider._get(rowIndex)
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
    val rowObject: T = dataProvider._get(rowIndex)
    return columns.filter { it.isVisible } .map { it._getFormatted(rowObject) }
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> Grid.Column<T>.getPresentationValue(rowObject: T): Any? {
    val json = Json.createObject()
    // the valueprovider is wrapped in TemplateRenderer which is wrapped in DataGenerator
    (grid as Grid<T>).dataGenerator2.generateData(rowObject, json)
    val value: JsonValue? = json.get(internalId2)
    return value?.asString()
}

@Suppress("UNCHECKED_CAST")
private val <T> Grid<T>.dataGenerator2: DataGenerator<T> get() = javaClass.getDeclaredMethod("getDataGenerator").run {
    isAccessible = true
    invoke(this@dataGenerator2) as DataGenerator<T>
}

@Suppress("UNCHECKED_CAST")
private val <T> Grid.Column<T>.internalId2: String get() = javaClass.getDeclaredMethod("getInternalId").run {
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

/**
 * Dumps the first [maxRows] rows of the Grid, formatting the values using the [_getFormatted] function. The output example:
 * ```
 * --[Name]--[Age]--[Occupation]--
 * 0: John, 25, Service Worker
 * 1: Fred, 40, Supervisor
 * --and 198 more
 * ```
 */
fun <T: Any> Grid<T>._dump(rows: IntRange = 0..10): String = kotlin.text.buildString {
    val visibleColumns: List<Grid.Column<T>> = columns.filter { it.isVisible }
    visibleColumns.map { "[${it.header2}]" }.joinTo(this, prefix = "--", separator = "-", postfix = "--\n")
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

fun Grid<*>.expectRows(count: Int) {
    if (dataProvider._size() != count) {
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
