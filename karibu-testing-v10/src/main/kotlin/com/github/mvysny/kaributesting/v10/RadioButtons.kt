package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import java.lang.reflect.Method

/**
 * Returns all item's captions.
 */
public fun <T: Any> RadioButtonGroup<T>.getItemLabels(): List<String> {
    val labelGenerator = _itemLabelGenerator
    return getItems().map { labelGenerator.apply(it) }
}

private val __RadioButtonGroup_getDataProvider: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val m = RadioButtonGroup::class.java.getDeclaredMethod("getDataProvider")
    m.isAccessible = true
    m
}

/**
 * `RadioButtonGroup.getDataProvider()` is private since Vaadin 24.0.0.beta1
 */
@Suppress("UNCHECKED_CAST")
public val <T> RadioButtonGroup<T>.dataProvider: DataProvider<T, *>
    get() = __RadioButtonGroup_getDataProvider.invoke(this) as DataProvider<T, *>

/**
 * Returns all items in this [RadioButtonGroup].
 */
public fun <T: Any> RadioButtonGroup<T>.getItems(): List<T> = dataProvider._findAll()

internal val <T> RadioButtonGroup<T>._itemLabelGenerator: ItemLabelGenerator<T> get() {
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
    selectByLabel(label, dataProvider, _itemLabelGenerator)
}
