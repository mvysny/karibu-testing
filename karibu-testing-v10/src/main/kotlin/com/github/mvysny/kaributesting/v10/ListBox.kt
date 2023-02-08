package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.listbox.ListBoxBase
import com.vaadin.flow.data.provider.DataProvider
import java.lang.reflect.Method

/**
 * Fetches renderings of items currently displayed in the list box component.
 */
public fun <T> ListBoxBase<*, T, *>.getRenderedItems(): List<String?> {
    val items: List<T> = getItems()
    return items.map { itemRenderer._getPresentationValue(it) }
}

/**
 * Fetches all items currently displayed in the list box component.
 */
public fun <T> ListBoxBase<*, T, *>.getItems(): List<T> = dataProvider._findAll()

private val __ListBoxBase_getDataProvider: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val m = ListBoxBase::class.java.getDeclaredMethod("getDataProvider")
    m.isAccessible = true
    m
}

/**
 * `ListBoxBase.getDataProvider()` is private since Vaadin 24.0.0.beta1
 */
@Suppress("UNCHECKED_CAST")
public val <T> ListBoxBase<*, T, *>.dataProvider: DataProvider<T, *>
    get() = __ListBoxBase_getDataProvider.invoke(this) as DataProvider<T, *>
