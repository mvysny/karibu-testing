@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ironlist.IronList
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.renderer.Renderer
import java.lang.reflect.Field
import java.util.stream.Stream
import kotlin.streams.toList

/**
 * Returns the item on given row. Fails if the row index is invalid.
 *
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row, not null.
 */
fun <T : Any> IronList<T>._get(rowIndex: Int): T {
    require(rowIndex >= 0) { "rowIndex must be 0 or greater: $rowIndex" }
        val size: Int = _size()
        if (rowIndex >= size) {
            throw AssertionError("Requested to get row $rowIndex but the data provider only has ${_size()} rows\n${_dump()}")
        }
    val fetched: List<T> = _fetch(rowIndex, 1)
    return fetched.firstOrNull()
            ?: throw AssertionError("Requested to get row $rowIndex but the data provider only has ${_size()} rows\n${_dump()}")
}

/**
 * Fetches an item from [IronList]'s data provider.
 */
fun <T> IronList<T>._fetch(offset: Int, limit: Int): List<T> {
    val stream: Stream<T> = (dataProvider as DataProvider<T, Any?>)
        .fetch(Query<T, Any?>(offset, limit, listOf(), null, null))
    return stream.toList()
}

/**
 * Returns all items in given data provider.
 * @return the list of items.
 */
fun <T> IronList<T>._findAll(): List<T> = _fetch(0, Int.MAX_VALUE)

/**
 * Returns the number of items in this IronList.
 */
fun IronList<*>._size(): Int = dataProvider._size()

val <T> IronList<T>._renderer: Renderer<T> get() {
    val f: Field = IronList::class.java.getDeclaredField("renderer")
    f.isAccessible = true
    return f.get(this) as Renderer<T>
}

/**
 * Returns the formatted value as a String. Does not use renderer to render the value - simply calls value provider and presentation provider
 * and converts the result to string (even if the result is a [Component]).
 * @param rowIndex the row index, 0 or higher.
 * @param columnId the column ID.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> IronList<T>._getFormattedRow(rowIndex: Int): String {
    val rowObject: T = _get(rowIndex)
    return _renderer._getPresentationValue(rowObject).toString()
}

/**
 * Dumps given range of [rows] of the IronList, formatting the values using the [_getFormattedRow] function. The output example:
 * ```
 * ----------------------
 * 0: John
 * 1: Fred
 * --and 198 more
 * ```
 */
@JvmOverloads
fun <T : Any> IronList<T>._dump(rows: IntRange = 0..10): String = buildString {
    appendln("----------------------")
    val dsIndices: IntRange
    val displayIndices: Set<Int>
        dsIndices = 0 until _size()
        displayIndices = rows.intersect(dsIndices)
        for (i in displayIndices) {
            append(i)
            append(": ")
            appendln(_getFormattedRow(i))
        }
    val andMore: Int = dsIndices.size - displayIndices.size
    if (andMore > 0) {
        append("--and $andMore more\n")
    }
}

fun IronList<*>.expectRows(count: Int) {
    if (_size() != count) {
        throw AssertionError("${this.toPrettyString()}: expected $count rows\n${_dump()}")
    }
}

fun IronList<*>.expectRow(rowIndex: Int, expected: String) {
    val actual: String = _getFormattedRow(rowIndex)
    if (expected != actual) {
        throw AssertionError("${this.toPrettyString()} at $rowIndex: expected $expected but got $actual\n${_dump()}")
    }
}
