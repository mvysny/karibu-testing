package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.formlayout.FormLayout
import java.lang.IllegalStateException
import kotlin.streams.toList

/**
 * Returns the single field nested in this form item. Fails if there are no
 * child components or if there are too many of them. Automatically skips
 * components placed in the `label` slot (see [FormLayout.FormItem.addToLabel]
 * for more details).
 */
val FormLayout.FormItem.field: Component get() {
    val fields: List<Component> = children.toList().filter { it.element.getAttribute("slot") != "label" }
    if (fields.size != 1) {
        throw IllegalStateException("FormItem: Expected 1 field but got ${fields.size}. Component tree:\n${toPrettyTree()}")
    }
    return fields[0]
}

/**
 * Concatenates texts from all elements placed in the `label` slot. This effectively
 * returns whatever was provided in the String label via [FormLayout.addFormItem].
 */
val FormLayout.FormItem.caption: String get() {
    val captions: List<Component> = children.toList().filter { it.element.getAttribute("slot") == "label" }
    return captions.joinToString("") { it._text ?: "" }
}
