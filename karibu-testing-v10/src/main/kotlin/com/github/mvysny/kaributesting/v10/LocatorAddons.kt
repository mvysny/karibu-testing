package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.caption
import com.github.mvysny.kaributools.label
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.VaadinIcon
import java.util.function.Predicate

/**
 * Makes sure that the component's [Component.caption] contains given [substring].
 */
@Suppress("DEPRECATION")
@Deprecated("use 'labelContains()' or 'textContains()'")
public fun <T : Component> SearchSpec<T>.captionContains(substring: String) {
    predicates.add(CaptionContainsPredicate(substring))
}

@Suppress("DEPRECATION")
@Deprecated("use 'label' or 'text'")
private data class CaptionContainsPredicate<T : Component>(val substring: String) : Predicate<T> {
    override fun test(t: T): Boolean = t.caption.contains(substring)
    override fun toString() = "captionContains('$substring')"
}

/**
 * Makes sure that the component's [Component.caption] contains given [substring].
 */
public fun <T : Component> SearchSpec<T>.labelContains(substring: String) {
    predicates.add(LabelContainsPredicate(substring))
}

private data class LabelContainsPredicate<T : Component>(val substring: String) : Predicate<T> {
    override fun test(t: T): Boolean = t.label.contains(substring)
    override fun toString() = "labelContains('$substring')"
}

/**
 * Makes sure that the component's [HasText.getText] contains given [substring].
 */
public fun <T> SearchSpec<T>.textContains(substring: String) where T : HasText, T : Component {
    predicates.add(TextContainsPredicate(substring))
}

private data class TextContainsPredicate<T : HasText>(val substring: String) : Predicate<T> {
    override fun test(t: T): Boolean = t.text.contains(substring)
    override fun toString() = "textContains('$substring')"
}

@Deprecated("replaced by iconIs()")
public fun <T: Button> SearchSpec<T>.buttonIconIs(collection: String, iconName: String) {
    iconIs(collection, iconName)
}

@Deprecated("replaced by iconIs()")
public fun <T: Button> SearchSpec<T>.buttonIconIs(vaadinIcon: VaadinIcon) {
    iconIs(vaadinIcon)
}
