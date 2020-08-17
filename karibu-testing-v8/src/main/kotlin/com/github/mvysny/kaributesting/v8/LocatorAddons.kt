package com.github.mvysny.kaributesting.v8

import com.vaadin.ui.Component
import java.util.function.Predicate

/**
 * Makes sure that the component's [Component.getCaption] contains given [substring].
 */
public fun <T : Component> SearchSpec<T>.captionContains(substring: String) {
    predicates.add(CaptionContainsPredicate(substring))
}

private data class CaptionContainsPredicate<T : Component>(val substring: String) : Predicate<T> {
    override fun test(t: T): Boolean = (t.caption ?: "").contains(substring)
    override fun toString() = "captionContains('$substring')"
}
