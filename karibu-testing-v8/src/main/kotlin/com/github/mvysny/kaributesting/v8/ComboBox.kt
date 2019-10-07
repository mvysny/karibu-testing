package com.github.mvysny.kaributesting.v8

import com.vaadin.server.SerializableConsumer
import com.vaadin.ui.ComboBox
import java.lang.reflect.Field


private val comboBoxFilterSlot: Field = ComboBox::class.java.getDeclaredField("filterSlot").apply { isAccessible = true }

/**
 * Emulates an user inputting something into the combo box, filtering items.  You can use [getSuggestionItems]
 * to retrieve those items and to verify that the filter on your data provider works properly.
 */
fun <T> ComboBox<T>.setUserInput(userInput: String?) {
    checkEditableByUser()
    @Suppress("UNCHECKED_CAST")
    (comboBoxFilterSlot.get(this) as SerializableConsumer<String?>).accept(userInput)
}

/**
 * Simulates the user creating a custom item. Only works if the field is editable by the user
 * and allows custom values ([ComboBox.isAllowCustomValue] is true).
 */
fun <T> ComboBox<T>._newItem(userInput: String) {
    checkEditableByUser()
    check(newItemProvider != null) { "${toPrettyString()} has no NewItemProvider set" }
    newItemProvider!!.apply(userInput)
}

/**
 * Fetches items currently displayed in the suggestion box.
 */
fun <T> ComboBox<T>.getSuggestionItems(): List<T> =
        dataCommunicator.fetchItemsWithRange(0, Int.MAX_VALUE)

/**
 * Fetches captions of items currently displayed in the suggestion box.
 */
fun <T> ComboBox<T>.getSuggestions(): List<String> {
    val items: List<T> = getSuggestionItems()
    return items.map { itemCaptionGenerator.apply(it) }
}
