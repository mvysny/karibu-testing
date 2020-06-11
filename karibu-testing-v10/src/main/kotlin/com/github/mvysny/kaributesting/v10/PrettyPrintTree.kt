package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.data.binder.HasItems
import java.util.*
import kotlin.streams.toList

/**
 * If true, [PrettyPrintTree] will use `\--` instead of `└──` which tend to render on some terminals as `???`.
 */
var prettyPrintUseAscii: Boolean = false

/**
 * Utility class to create a pretty-printed ASCII tree of arbitrary nodes that can be printed to the console.
 * You can build the tree out of any tree structure, just fill in this node [name] and its [children].
 *
 * To create a pretty tree dump of a Vaadin component, just use [ofVaadin].
 */
class PrettyPrintTree(val name: String, val children: MutableList<PrettyPrintTree>) {

    private val pipe = if (!prettyPrintUseAscii) '│' else '|'
    private val branchTail = if (!prettyPrintUseAscii) "└── " else "\\-- "
    private val branch = if (!prettyPrintUseAscii) "├── " else "|-- "

    fun print(): String {
        val sb = StringBuilder()
        print(sb, "", true)
        return sb.toString()
    }

    private fun print(sb: StringBuilder, prefix: String, isTail: Boolean) {
        sb.append(prefix + (if (isTail) branchTail else branch) + name + "\n")
        for (i in 0 until children.size - 1) {
            children[i].print(sb, prefix + if (isTail) "    " else "$pipe   ", false)
        }
        if (children.size > 0) {
            children[children.size - 1]
                    .print(sb, prefix + if (isTail) "    " else "$pipe   ", true)
        }
    }

    companion object {

        fun ofVaadin(root: Component): PrettyPrintTree {
            val result = PrettyPrintTree(root.toPrettyString(), mutableListOf())
            for (child: Component in testingLifecycleHook.getAllChildren(root)) {
                result.children.add(ofVaadin(child))
            }
            return result
        }
    }
}

fun Component.toPrettyTree(): String = PrettyPrintTree.ofVaadin(this).print()

/**
 * Returns the most basic properties of the component, formatted as a concise string:
 * * The component class
 * * The [Component.getId]
 * * Whether the component is [Component.isVisible]
 * * Whether it is a [HasValue] that is read-only
 * * the styles
 * * The [Component.label] and text
 * * The [HasValue.getValue]
 */
@Suppress("UNCHECKED_CAST")
fun Component.toPrettyString(): String {
    val list = LinkedList<String>()
    val slot: String? = element.getAttribute("slot")
    if (!slot.isNullOrBlank()) {
        list.add("slot='$slot'")
    }
    if (id.isPresent) {
        list.add("#${id.get()}")
    }
    if (!_isVisible) {
        list.add("INVIS")
    }
    if (this is HasValue<*, *> && (this as HasValue<HasValue.ValueChangeEvent<Any?>, Any?>).isReadOnly) {
        list.add("RO")
    }
    if (!isEnabled) {
        list.add("DISABLED")
    }
    if (element.style.names.toList().isNotEmpty()) {
        list.add(element.style.names.toList().joinToString(" ") { "$it:${element.style[it]}" })
    }
    if (label.isNotBlank()) {
        list.add("label='$label'")
    }
    if (label != caption && caption.isNotBlank()) {
        list.add("caption='$caption'")
    }
    if (!_text.isNullOrBlank() && this !is Button) {
        list.add("text='$_text'")
    }
    if (this is HasValue<*, *>) {
        list.add("value='${(this as HasValue<HasValue.ValueChangeEvent<Any?>, Any?>).value}'")
    }
    if (this is HasValidation) {
        if (this.isInvalid) {
            list.add("INVALID")
        }
        if (!this.errorMessage.isNullOrBlank()) {
            list.add("errorMessage='$errorMessage'")
        }
    }
    if (this is Grid.Column<*> && this.header2.isNotBlank()) {
        list.add("header='${this.header2}'")
    }
    if (!placeholder.isNullOrBlank()) {
        list.add("placeholder='$placeholder'")
    }
    if (this is Anchor) {
        list.add("href='$href'")
    }
    if (this is Image) {
        list.add("src='$src'")
    }
    if (this is Icon) {
        list.add("icon='$iconName'")
    }
    if (this is Button && icon is Icon) {
        list.add("icon='${(icon as Icon).iconName}'")
    }
    if (this is Grid<*> && this.beanType != null) {
        list.add("<${this.beanType.simpleName}>")
    }
    if (this is HasItems<*>) {
        list.add("dataprovider='${this.dataProvider}'")
    }
    prettyStringHook(this, list)
    var name: String = javaClass.simpleName
    if (name.isEmpty()) {
        // anonymous classes
        name = javaClass.name
    }
    return name + list
}

/**
 * Invoked by [toPrettyString] to add additional properties for your custom component.
 * Add additional properties to the `list` provided, e.g. `list.add("icon='$icon'")`.
 *
 * By default does nothing.
 */
var prettyStringHook: (component: Component, list: LinkedList<String>) -> Unit = { _, _ -> }
