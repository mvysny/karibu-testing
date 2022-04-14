package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.data.renderer.ComponentRenderer

/**
 * Returns all item's captions.
 */
public fun <T: Any> RadioButtonGroup<T>.getItemLabels(): List<String> {
    val r = itemRenderer
    return dataProvider._findAll().map {
        r._getPresentationValue(it).toString()
    }
}

/**
 * Retrieves a component produced by [RadioButtonGroup.itemRenderer]. Fails if the
 * renderer is not a [ComponentRenderer].
 * @param item the item
 * @throws IllegalStateException if the renderer is not [ComponentRenderer].
 */
public fun <T : Any> RadioButtonGroup<T>._getRenderedComponent(item: T): Component =
    itemRenderer.createComponent(item)
