package com.github.mvysny.kaributesting.v23

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.data.provider.DataCommunicator
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.Renderer
import kotlin.test.fail

/**
 * Emulates user inputting something into the combo box, filtering items.
 *
 * In order to verify that the filter on your data provider works properly,
 * use [getSuggestionItems] to retrieve filtered items.
 *
 * Note: this function will not change the value of the combo box.
 * @param userInput emulate user typing something into the ComboBox, thus attempting
 * to filter out items/search for an item. Pass in `null` to clear.
 */
public fun <T> MultiSelectComboBox<T>.setUserInput(userInput: String?) {
    _expectEditableByUser()
    KaribuInternalComboBoxSupport.get().setUserInput(this, userInput)
}

/**
 * Select an item in the combo box by [label]. Calls [setUserInput] to filter the items first, then
 * calls [getSuggestionItems] to obtain filtered items, then selects the sole item that matches [label].
 *
 * Fails if the item is not found, or multiple items are found. Fails if the combo box is not editable.
 * @param bypassSetUserInput if false (default), the [setUserInput] is called to filter the items first.
 * This has much higher performance on a large data set since it will perform the filtering in the
 * data provider itself (in the backend rather than in-memory). However, if this does not work
 * for some reason, set this to `true` to search in all items.
 */
@JvmOverloads
public fun <T> MultiSelectComboBox<T>.selectByLabel(
    label: String,
    bypassSetUserInput: Boolean = false
) {
    val suggestionItems: List<T> = if (!bypassSetUserInput) {
        setUserInput(label)
        getSuggestionItems()
    } else {
        _expectEditableByUser()
        dataProvider._findAll()
    }
    val items: List<T> =
        suggestionItems.filter { itemLabelGenerator.apply(it) == label }
    when {
        items.isEmpty() -> {
            val msg = StringBuilder()
            msg.append("${toPrettyString()}: No item found with label '$label'")
            if (dataProvider.isInMemory) {
                val allItems: List<T> = dataProvider._findAll()
                msg.append(". Available items: ${allItems.map { "'${itemLabelGenerator.apply(it)}'=>$it" }}")
            }
            fail(msg.toString())
        }

        items.size > 1 -> fail("${(this as Component).toPrettyString()}: Multiple items found with label '$label': $items")
        else -> {
            val value = _value!!.toMutableSet()
            value.add(items[0])
            _value = value
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal val <T> MultiSelectComboBox<T>._dataCommunicator: DataCommunicator<T>
    get() = KaribuInternalComboBoxSupport.get().getDataCommunicator(this) as DataCommunicator<T>?
        ?: fail("${toPrettyString()}: items/dataprovider has not been set")

/**
 * Fetches items currently displayed in the suggestion box. This list is filtered
 * by any user input set via [setUserInput].
 */
public fun <T> MultiSelectComboBox<T>.getSuggestionItems(): List<T> =
    _dataCommunicator.fetchAll()

/**
 * Fetches captions of items currently displayed in the suggestion box. This list is filtered
 * by any user input set via [setUserInput].
 */
public fun <T> MultiSelectComboBox<T>.getSuggestions(): List<String> {
    val items: List<T> = getSuggestionItems()
    return items.map { itemLabelGenerator.apply(it) }
}

/**
 * Returns the renderer set via [ComboBox.setRenderer].
 */
@Suppress("UNCHECKED_CAST")
public val <T> MultiSelectComboBox<T>._renderer: Renderer<T> get() = KaribuInternalComboBoxSupport.get().getRenderer(this) as Renderer<T>

/**
 * Returns the component rendered in [ComboBox] dropdown overlay for given [item].
 *
 * Fails if the [ComboBox] renderer is something else than [ComponentRenderer].
 */
public fun <T> MultiSelectComboBox<T>._getRenderedComponentFor(item: T): Component {
    val r = _renderer
    val r2 = r as? ComponentRenderer<*, T> ?: fail("${toPrettyString()}: expected ComponentRenderer but got $r")
    return r2.createComponent(item)
}
