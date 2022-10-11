@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.VaadinVersion
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
import java.lang.reflect.Method
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
public fun <T> ComboBox<T>.setUserInput(userInput: String?) {
    _expectEditableByUser()
    if (VaadinVersion.get.isAtLeast(23, 2)) {
        getComboBoxBaseFilterSlot(this).accept(userInput)
    } else {
        val comboBoxFilterSlot: Field =
            ComboBox::class.java.getDeclaredField("filterSlot")
        comboBoxFilterSlot.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val filterSlot =
            comboBoxFilterSlot.get(this) as SerializableConsumer<String?>
        filterSlot.accept(userInput)
    }
}

private val _ComboBoxBase_getDataController: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val comboBoxBaseClass =
        Class.forName("com.vaadin.flow.component.combobox.ComboBoxBase")
    val m = comboBoxBaseClass.getDeclaredMethod("getDataController")
    m.isAccessible = true
    m
}
private val _ComboBoxDataController_filterSlot: Field by lazy(
    LazyThreadSafetyMode.PUBLICATION
) {
    val comboBoxFilterSlot: Field =
        Class.forName("com.vaadin.flow.component.combobox.ComboBoxDataController")
            .getDeclaredField("filterSlot")
    comboBoxFilterSlot.isAccessible = true
    comboBoxFilterSlot
}

/**
 * INTERNAL, DON'T USE. Only for Vaadin 23.2+. Calls `(this as ComboBoxBase).getDataController().filterSlot`.
 */
public fun getComboBoxBaseFilterSlot(comboBoxBase: /*com.vaadin.flow.component.combobox.ComboBoxBase*/Any): SerializableConsumer<String?> {
    require(VaadinVersion.get.isAtLeast(23, 2)) { "This only works for Vaadin 23.2 and higher but got ${VaadinVersion.get}" }
    val dataController: /*ComboBoxDataController*/Any = _ComboBoxBase_getDataController.invoke(comboBoxBase)

    @Suppress("UNCHECKED_CAST")
    val filterSlot =
        _ComboBoxDataController_filterSlot.get(dataController) as SerializableConsumer<String?>
    return filterSlot
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
public fun <T> ComboBox<T>.selectByLabel(
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
        else -> _value = items[0]
    }
}

/**
 * Simulates the user creating a custom item. Only works if the field is editable by the user
 * and allows custom values ([ComboBox.isAllowCustomValue] is true).
 */
public fun <T> ComboBox<T>._fireCustomValueSet(userInput: String) {
    _expectEditableByUser()
    check(isAllowCustomValue) { "${toPrettyString()} doesn't allow custom values" }
    _fireEvent(GeneratedVaadinComboBox.CustomValueSetEvent<ComboBox<T>>(this, true, userInput))
}

private val _ComboBox_dataCommunicator: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val field: Field = ComboBox::class.java.getDeclaredField("dataCommunicator")
    field.isAccessible = true
    field
}

private val _ComboBox_23_2_dataCommunicator: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val comboBoxBaseClass =
        Class.forName("com.vaadin.flow.component.combobox.ComboBoxBase")
    val m = comboBoxBaseClass.getDeclaredMethod("getDataCommunicator")
    m.isAccessible = true
    m
}

@Suppress("UNCHECKED_CAST")
private fun <T> getDataCommunicator(cb: ComboBox<T>): DataCommunicator<T>? =
    when {
        VaadinVersion.get.isAtLeast(23, 2) ->
            _ComboBox_23_2_dataCommunicator.invoke(cb) as DataCommunicator<T>?

        else ->
            _ComboBox_dataCommunicator.get(cb) as DataCommunicator<T>?
    }

@Suppress("UNCHECKED_CAST")
internal val <T> ComboBox<T>._dataCommunicator: DataCommunicator<T>
    get() = getDataCommunicator(this)
        ?: fail("${toPrettyString()}: items/dataprovider has not been set")

/**
 * Fetches items currently displayed in the suggestion box. This list is filtered
 * by any user input set via [setUserInput].
 */
public fun <T> ComboBox<T>.getSuggestionItems(): List<T> =
    _dataCommunicator.fetchAll()

/**
 * Fetches captions of items currently displayed in the suggestion box. This list is filtered
 * by any user input set via [setUserInput].
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
    val g: ItemLabelGenerator<T> =
        itemLabelGenerator ?: ItemLabelGenerator { it.toString() }
    return items.map { g.apply(it) }
}

/**
 * Select an item in the combo box by [label]. Calls [getSuggestionItems] to obtain filtered items,
 * then selects the sole item that matches [label].
 *
 * Fails if the item is not found, or multiple items are found. Fails if the combo box is not editable.
 */
public fun <T> Select<T>.selectByLabel(label: String) {
    // it's OK to use selectByLabel(): Select's dataprovider is expected to hold small amount of data
    // since Select doesn't offer any filtering capabilities.
    selectByLabel(
        label,
        dataProvider,
        itemLabelGenerator ?: ItemLabelGenerator { it.toString() })
}

/**
 * Beware: the function will poll all items from the [dataProvider]. Use cautiously and only for small data providers.
 */
internal fun <T> HasValue<*, T>.selectByLabel(
    label: String,
    dataProvider: DataProvider<T, *>,
    itemLabelGenerator: ItemLabelGenerator<T>
) {
    val items = dataProvider._findAll()
    val itemsWithLabel: List<T> =
        items.filter { itemLabelGenerator.apply(it) == label }

    when {
        itemsWithLabel.isEmpty() ->
            fail("${(this as Component).toPrettyString()}: No item found with label '$label'. Available items: ${items.map { "'${itemLabelGenerator.apply(it)}'=>$it" }}")
        itemsWithLabel.size > 1 ->
            fail("${(this as Component).toPrettyString()}: Multiple items found with label '$label': $itemsWithLabel")

        else -> _value = itemsWithLabel[0]
    }
}
