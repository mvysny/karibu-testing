package com.github.mvysny.kaributesting.v23

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.ClickNotifier
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.virtuallist.VirtualList
import com.vaadin.flow.data.renderer.ClickableRenderer
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.NativeButtonRenderer
import com.vaadin.flow.data.renderer.Renderer
import java.lang.reflect.Field
import kotlin.test.fail

/**
 * Returns the item on given row. Fails if the row index is invalid.
 * @param rowIndex the row, 0..size - 1
 * @return the item at given row, not null.
 */
public fun <T : Any> VirtualList<T>._get(rowIndex: Int): T {
    require(rowIndex >= 0) { "rowIndex must be 0 or greater: $rowIndex" }
    if (_dataProviderSupportsSizeOp) {
        val size: Int = _size()
        if (rowIndex >= size) {
            throw AssertionError("Requested to get row $rowIndex but the data provider only has ${_size()} rows\n${_dump()}")
        }
    }
    return _getOrNull(rowIndex)
        ?: throw AssertionError("Requested to get row $rowIndex but the data provider returned 0 rows\n${_dump()}")
}

/**
 * Returns the item on given row, or null if the [rowIndex] is larger than the number
 * of items the data provider can provide.
 * @param rowIndex the row, 0 or larger.
 * @return the item at given row or null if the data provider provides less rows.
 */
public fun <T : Any> VirtualList<T>._getOrNull(rowIndex: Int): T? {
    require(rowIndex >= 0) { "rowIndex must be 0 or greater: $rowIndex" }
    if (_dataProviderSupportsSizeOp) {
        val size: Int = _size()
        if (rowIndex >= size) {
            return null
        }
    }
    val fetched: List<T> = _fetch(rowIndex, 1)
    return fetched.firstOrNull()
}

/**
 * @return true if the current data provider supports [_size] retrieval, false
 * if not.
 */
public val VirtualList<*>._dataProviderSupportsSizeOp: Boolean get() = dataCommunicator.isDefinedSize

/**
 * Returns items in given range from VirtualList's data provider.
 */
public fun <T> VirtualList<T>._fetch(offset: Int, limit: Int): List<T> = dataCommunicator.fetch(offset, limit)

public val VirtualList<*>._saneFetchLimit: Int get() = dataCommunicator._saneFetchLimit

/**
 * Returns all items in given data provider.
 * @return the list of items.
 */
public fun <T> VirtualList<T>._findAll(): List<T> = _fetch(0, _saneFetchLimit)

/**
 * Returns the number of items in this VirtualList.
 *
 * If [_dataProviderSupportsSizeOp] is false, this function will fetch all the data
 * and count the result returned, which is very slow.
 */
public fun VirtualList<*>._size(): Int {
    if (!_dataProviderSupportsSizeOp) {
        return _findAll().size
    }
    return dataCommunicator._size()
}

private val _VirtualList_renderer: Field = VirtualList::class.java.getDeclaredField("renderer").apply { isAccessible = true }

/**
 * Returns the renderer for this VirtualList.
 */
public val <T> VirtualList<T>._renderer: Renderer<T> get() = _VirtualList_renderer.get(this) as Renderer<T>

/**
 * Performs a click on a [ClickableRenderer] in given [VirtualList] cell. Only supports the following scenarios:
 * * [ClickableRenderer]
 * * [ComponentRenderer] which renders a [Button] or a [ClickNotifier].
 * @param rowIndex the row index, 0 or higher.
 * @throws AssertionError if the renderer is not [ClickableRenderer] nor [ComponentRenderer].
 */
public fun <T : Any> VirtualList<T>._clickRenderer(rowIndex: Int) {
    checkEditableByUser()
    val renderer: Renderer<T> = _renderer
    val item: T = _get(rowIndex)
    if (renderer is ClickableRenderer<*>) {
        @Suppress("UNCHECKED_CAST")
        (renderer as ClickableRenderer<T>).onClick(item)
    } else if (renderer is ComponentRenderer<*, *>) {
        val component: Component = (renderer as ComponentRenderer<*, T>).createComponent(item)
        if (component is Button) {
            component._click()
        } else if (component is ClickNotifier<*>) {
            component._click()
        } else {
            // don't try to do anything smart here since things will break silently for the customer as they upgrade Vaadin version
            // https://github.com/mvysny/karibu-testing/issues/67
            fail("${this.toPrettyString()}: ComponentRenderer produced ${component.toPrettyString()} which is not a button nor a ClickNotifier - please use _getCellComponent() instead")
        }
    } else {
        fail("${this.toPrettyString()} has renderer $renderer which is not supported by this method")
    }
}

/**
 * Retrieves a component produced by [ComponentRenderer]. Fails if the
 * renderer is not a [ComponentRenderer].
 * @param rowIndex the row index, 0 or higher.
 * @throws IllegalStateException if the renderer is not [ComponentRenderer].
 */
public fun <T : Any> VirtualList<T>._getRowComponent(rowIndex: Int): Component {
    val renderer: Renderer<T> = _renderer
    if (renderer !is ComponentRenderer<*, *>) {
        fail("${this.toPrettyString()} uses renderer $renderer but we expect a ComponentRenderer here")
    }
    if (renderer is NativeButtonRenderer<*>) {
        fail("${this.toPrettyString()} uses NativeButtonRenderer which is not supported by this function")
    }
    val item: T = _get(rowIndex)
    val component: Component = (renderer as ComponentRenderer<*, T>).createComponent(item)
    return component
}

/**
 * Returns the formatted value of given row as a String. Uses [getPresentationValue]
 * and converts the result to string (even if the result is a [Component]).
 * @param rowIndex the row index, 0 or higher.
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Any> VirtualList<T>._getFormatted(rowIndex: Int): String {
    val rowObject: T = _get(rowIndex)
    return _getFormattedObj(rowObject)
}

/**
 * Returns the formatted row as a list of Strings, one for every visible column.
 * Uses [_getFormatted]. Returns null if the [rowIndex] is not within the limits.
 * @param rowIndex the index of the row, 0-based.
 */
public fun <T : Any> VirtualList<T>._getFormattedOrNull(rowIndex: Int): String? {
    val rowObject: T = _getOrNull(rowIndex) ?: return null
    return _getFormattedObj(rowObject)
}

/**
 * Returns the formatted value as a String. Uses [getPresentationValue]
 * and converts the result to string (even if the result is a [Component]).
 * @param rowObject the bean
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Any> VirtualList<T>._getFormattedObj(rowObject: T): String =
    getPresentationValue(rowObject).toString()

/**
 * Returns the output of renderer for given [rowObject] formatted as close as possible
 * to the client-side output, using [_renderer].
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Any> VirtualList<T>.getPresentationValue(rowObject: T): Any? =
    _renderer._getPresentationValue(rowObject)

/**
 * Dumps given range of [rows] of the Grid, formatting the values using the [_getFormatted] function. The output example:
 * ```
 * --[Name]--[Age]--[Occupation]--
 * 0: John, 25, Service Worker
 * 1: Fred, 40, Supervisor
 * --and 198 more
 * ```
 */
@JvmOverloads
public fun <T : Any> VirtualList<T>._dump(rows: IntRange = 0..9): String = buildString {
    append(toPrettyString()).append('\n')
    val dsIndices: IntRange
    val displayIndices: Set<Int>
    if (_dataProviderSupportsSizeOp) {
        dsIndices = 0 until _size()
        displayIndices = rows.intersect(dsIndices)
        for (i in displayIndices) {
            append("$i: ${_getFormatted(i)}\n")
        }
        val andMore = dsIndices.size - displayIndices.size
        if (andMore > 0) {
            append("--and $andMore more\n")
        }
    } else {
        var rowsOutputted = 0
        for (i in rows) {
            val row = _getFormattedOrNull(i) ?: break
            append("$i: $row\n")
            rowsOutputted++
        }
        if (rowsOutputted == rows.size) {
            append("--and possibly more\n")
        } else {
            append("--\n")
        }
    }
}

/**
 * Asserts that this grid's provider returns given [count] of items. If not,
 * an [AssertionError] is thrown with the Grid [_dump].
 */
public fun VirtualList<*>.expectRows(count: Int) {
    val actual = _size()
    if (actual != count) {
        throw AssertionError("${this.toPrettyString()}: expected $count rows but got $actual\n${_dump()}")
    }
}

/**
 * Asserts that this grid's [rowIndex] row is formatted as expected.
 * @param expected the expected row formatting.
 */
public fun VirtualList<*>.expectRow(rowIndex: Int, expected: String) {
    val actual: String = _getFormatted(rowIndex)
    if (expected != actual) {
        throw AssertionError("${this.toPrettyString()} at $rowIndex: expected $expected but got $actual\n${_dump()}")
    }
}
