package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.radiobutton.RadioButtonGroup

/**
 * Returns all item's captions.
 */
public fun <T: Any> RadioButtonGroup<T>.getItemLabels(): List<String> {
    val r = itemRenderer
    return dataProvider._findAll().map {
        r._getPresentationValue(it).toString()
    }
}
