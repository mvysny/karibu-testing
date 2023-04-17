package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.data.provider.DataProvider
import java.lang.reflect.Method
import kotlin.test.fail

/**
 * Fetches renderings of items currently displayed in the checkbox group component.
 */
public fun <T> CheckboxGroup<T>.getItemLabels(): List<String?> {
    val items: List<T> = dataProvider._findAll()
    return items.map { itemLabelGenerator.apply(it) }
}

private val __CheckboxGroup_getDataProvider: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val m = CheckboxGroup::class.java.getDeclaredMethod("getDataProvider")
    m.isAccessible = true
    m
}

/**
 * `CheckboxGroup.getDataProvider()` is private since Vaadin 24.0.0.beta1
 */
@Suppress("UNCHECKED_CAST")
public val <T> CheckboxGroup<T>.dataProvider: DataProvider<T, *>
    get() = __CheckboxGroup_getDataProvider.invoke(this) as DataProvider<T, *>

/**
 * Select all items in this [CheckboxGroup] by [labels]. Fails if the item is not found. Fails if this component is not editable.
 */
public fun <T> CheckboxGroup<T>.selectByLabel(vararg labels: String) {
    selectByLabel(labels.toSet(), itemLabelGenerator)
}

/**
 * Beware: the function will poll all items from the [dataProvider]. Use cautiously and only for small data providers.
 */
internal fun <T> HasValue<*, Set<T>>.selectByLabel(labels: Set<String>, itemLabelGenerator: ItemLabelGenerator<T>) {
    // maps label to items that have the label
    @Suppress("UNCHECKED_CAST")
    val items: Map<String, List<T>> = ((this as Component).dataProvider as DataProvider<T, *>)
        ._findAll()
        .groupBy { itemLabelGenerator.apply(it) }
    // select all items that have the label
    val itemsWithLabels = items.filterKeys { key -> labels.contains(key) }
    val e = itemsWithLabels.entries.firstOrNull { it.value.size > 1 }
    if (e != null) {
        fail("${toPrettyString()}: Multiple items found with label '${e.key}': ${e.value}")
    }
    if (itemsWithLabels.keys.size < labels.size) {
        fail("${toPrettyString()}: No item found with label(s) '${labels.minus(items.keys)}'. Available items: ${items.keys}")
    }

    _value = itemsWithLabels.values.flatten().toSet()
}