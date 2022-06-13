@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.provider.DataCommunicator
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.function.SerializableConsumer
import java.lang.reflect.Field
import kotlin.test.fail

/**
 * Emulates user inputting something into the combo box, filtering items. You should use [getSuggestionItems]
 * to retrieve filtered items, in order to verify that the filter on your data provider works properly.
 *
 * Note: this function will not change the value of the combo box.
 */
public fun <T> ComboBox<T>.setUserInput(userInput: String?) {
    checkEditableByUser()
    val comboBoxFilterSlot: Field = ComboBox::class.java.getDeclaredField("filterSlot").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    (comboBoxFilterSlot.get(this) as SerializableConsumer<String?>).accept(userInput)
}

/**
 * Select an item in the combo box by [label]. Calls [setUserInput] to filter the items first, then
 * calls [getSuggestionItems] to obtain filtered items, then selects the sole item that matches [label].
 *
 * Fails if the item is not found, or multiple items are found. Fails if the combo box is not editable.
 */
public fun <T> ComboBox<T>.selectByLabel(label: String) {
    setUserInput(label)
    val items: List<T> = getSuggestionItems().filter { itemLabelGenerator.apply(it) == label }
    when {
        items.isEmpty() -> fail("${(this as Component).toPrettyString()}: No item found with label '$label'")
        items.size > 1 -> fail("${(this as Component).toPrettyString()}: Multiple items found with label '$label': $items")
        else -> _value = items[0]
    }
}

/**
 * Simulates the user creating a custom item. Only works if the field is editable by the user
 * and allows custom values ([ComboBox.isAllowCustomValue] is true).
 */
public fun <T> ComboBox<T>._fireCustomValueSet(userInput: String) {
    checkEditableByUser()
    check(isAllowCustomValue) { "${toPrettyString()} doesn't allow custom values" }
    _fireEvent(GeneratedVaadinComboBox.CustomValueSetEvent<ComboBox<T>>(this, true, userInput))
}

private val _ComboBox_dataCommunicator: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val field: Field = ComboBox::class.java.getDeclaredField("dataCommunicator")
    field.isAccessible = true
    field
}

@Suppress("UNCHECKED_CAST")
internal val <T> ComboBox<T>._dataCommunicator: DataCommunicator<T>
    get() = _ComboBox_dataCommunicator.get(this) as DataCommunicator<T>?
        ?: fail("${toPrettyString()}: items/dataprovider has not been set")

/**
 * Fetches items currently displayed in the suggestion box.
 */
public fun <T> ComboBox<T>.getSuggestionItems(): List<T> =
    _dataCommunicator.fetchAll()

/**
 * Fetches captions of items currently displayed in the suggestion box.
 */
public fun <T> ComboBox<T>.getSuggestions(): List<String> {
    val items: List<T> = getSuggestionItems()
    return items.map { itemLabelGenerator.apply(it) }
}

/**
 * Fetches items currently displayed in the suggestion box.
 */
@Suppress("UNCHECKED_CAST")
public fun <T> Select<T>.getSuggestionItems(): List<T> = dataProvider._findAll()

/**
 * Fetches captions of items currently displayed in the suggestion box.
 */
public fun <T> Select<T>.getSuggestions(): List<String> {
    val items: List<T> = getSuggestionItems()
    val g: ItemLabelGenerator<T> = itemLabelGenerator ?: ItemLabelGenerator { it.toString() }
    return items.map { g.apply(it) }
}

/**
 * Select an item in the combo box by [label]. Calls [getSuggestionItems] to obtain filtered items,
 * then selects the sole item that matches [label].
 *
 * Fails if the item is not found, or multiple items are found. Fails if the combo box is not editable.
 */
public fun <T> Select<T>.selectByLabel(label: String) {
    selectByLabel(label, dataProvider, itemLabelGenerator ?: ItemLabelGenerator { it.toString() })
}

internal fun <T> HasValue<*, T>.selectByLabel(label: String, dataProvider: DataProvider<T, *>, itemLabelGenerator: ItemLabelGenerator<T>) {
    val items = dataProvider._findAll()
    val itemsWithLabel: List<T> = items.filter { itemLabelGenerator.apply(it) == label }
    when {
        itemsWithLabel.isEmpty() -> fail("${(this as Component).toPrettyString()}: No item found with label '$label'. Available labels: ${items.map { itemLabelGenerator.apply(it) }}")
        itemsWithLabel.size > 1 -> fail("${(this as Component).toPrettyString()}: Multiple items found with label '$label': $itemsWithLabel")
        else -> _value = itemsWithLabel[0]
    }
}
