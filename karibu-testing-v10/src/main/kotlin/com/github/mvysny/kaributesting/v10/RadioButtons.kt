package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer

/**
 * Returns all item's captions.
 */
public fun <T: Any> RadioButtonGroup<T>.getItemLabels(): List<String> {
    val labelGenerator = itemLabelGenerator
    return getItems().map { labelGenerator.apply(it) }
}

/**
 * Returns all items in this [RadioButtonGroup].
 */
public fun <T: Any> RadioButtonGroup<T>.getItems(): List<T> = dataProvider._findAll()

internal val <T> RadioButtonGroup<T>.itemLabelGenerator: ItemLabelGenerator<T> get() {
    val r = itemRenderer
    return ItemLabelGenerator { r._getPresentationValue(it).toString() }
}

/**
 * Retrieves a component produced by [RadioButtonGroup.itemRenderer]. Fails if the
 * renderer is not a [ComponentRenderer].
 * @param item the item
 * @throws IllegalStateException if the renderer is not [ComponentRenderer].
 */
public fun <T : Any> RadioButtonGroup<T>._getRenderedComponent(item: T): Component =
    itemRenderer.createComponent(item)

/**
 * Select an item in the radio button group by [label].
 *
 * Fails if the item is not found, or multiple items are found. Fails if the radio button group is not editable.
 */
public fun <T> RadioButtonGroup<T>.selectByLabel(label: String) {
    selectByLabel(label, dataProvider, itemLabelGenerator)
}
