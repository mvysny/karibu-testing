package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.IconName
import com.github.mvysny.kaributools.caption
import com.github.mvysny.kaributools.iconName
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import java.util.function.Predicate

/**
 * Makes sure that the component's [Component.caption] contains given [substring].
 */
public fun <T : Component> SearchSpec<T>.captionContains(substring: String) {
    predicates.add(CaptionContainsPredicate(substring))
}

private data class CaptionContainsPredicate<T : Component>(val substring: String) : Predicate<T> {
    override fun test(t: T): Boolean = t.caption.contains(substring)
    override fun toString() = "captionContains('$substring')"
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

/**
 * Makes sure that [Button.getIcon] is of given [collection] and matches the [iconName].
 */
public fun <T: Button> SearchSpec<T>.buttonIconIs(collection: String, iconName: String) {
    predicates.add(ButtonIconIsPredicate(IconName(collection, iconName)))
}

/**
 * Makes sure that [Button.getIcon] is given [vaadinIcon].
 */
public fun <T: Button> SearchSpec<T>.buttonIconIs(vaadinIcon: VaadinIcon) {
    predicates.add(ButtonIconIsPredicate(IconName.of(vaadinIcon)))
}

private data class ButtonIconIsPredicate<T: Button>(val iconName: IconName) : Predicate<T> {
    override fun test(t: T): Boolean {
        val icon: Icon = t.icon as? Icon ?: return false
        return icon.iconName == iconName

    }
    override fun toString() = "buttonIconIs($iconName)"
}
