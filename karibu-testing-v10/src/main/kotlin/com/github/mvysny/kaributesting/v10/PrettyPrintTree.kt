package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.data.provider.CallbackDataProvider
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider
import java.util.*

/**
 * If true, [PrettyPrintTree] will use `\--` instead of `└──` which tend to render on some terminals as `???`.
 */
public var prettyPrintUseAscii: Boolean = false

/**
 * Utility class to create a pretty-printed ASCII tree of arbitrary nodes that can be printed to the console.
 * You can build the tree out of any tree structure, just fill in this node [name] and its [children].
 *
 * To create a pretty tree dump of a Vaadin component, just use [ofVaadin].
 */
public class PrettyPrintTree(public val name: String, public val children: MutableList<PrettyPrintTree>) {

    private val pipe = if (!prettyPrintUseAscii) '│' else '|'
    private val branchTail = if (!prettyPrintUseAscii) "└── " else "\\-- "
    private val branch = if (!prettyPrintUseAscii) "├── " else "|-- "

    public fun print(): String {
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

    public companion object {

        public fun ofVaadin(root: Component): PrettyPrintTree {
            val result = PrettyPrintTree(root.toPrettyString(), mutableListOf())
            for (child: Component in testingLifecycleHook.getAllChildren(root)) {
                result.children.add(ofVaadin(child))
            }
            return result
        }
    }
}

/**
 * Pretty-prints the Vaadin component tree and returns it as a string, for example:
 * ```
 * └── MockedUI[]
 *     └── Button[text='Hello!']
 * ```
 */
public fun Component.toPrettyTree(): String = PrettyPrintTree.ofVaadin(this).print()

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
public fun Component.toPrettyString(): String {
    val list = LinkedList<String>()
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
    val label = testingLifecycleHook.getLabel(this)
    if (!label.isNullOrBlank()) {
        list.add("label='$label'")
    }
    if (!_text.isNullOrBlank()) {
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
    if (this is Grid.Column<*>) {
        if (this.header2.isNotBlank()) {
            list.add("header='${this.header2}'")
        }
        if (!this.key.isNullOrBlank()) {
            list.add("key='${this.key}'")
        }
        if (this.isResizable) list.add("resizable")
        if (this.isAutoWidth) list.add("autoWidth")
        if (this.isSortable) list.add("sortable")
        list.add("flexGrow=${this.flexGrow}")
    }
    if (!placeholder.isNullOrBlank()) {
        list.add("placeholder='$placeholder'")
    }
    if (this is Anchor) {
        var href = _href
        if (href.startsWith("VAADIN/dynamic/resource/")) {
            // can't test against UUIDs present in Vaadin resources such as
            // VAADIN/dynamic/resource/1/7c5bbdba-7b08-48f2-8268-4b6e17767bcd/
            href = "VAADIN/dynamic/resource/*"
        }
        list.add("href='$href'")
    }
    if (_iconName != null) {
        list.add("icon='$_iconName'")
    }
    if (this is Html) {
        val outerHtml: String = this.element.outerHTML.trim().replace(Regex("\\s+"), " ")
        list.add(outerHtml.ellipsize(100))
    }
    if (this is Grid<*> && this.beanType != null) {
        list.add("<${this.beanType.simpleName}>")
    }
    if (this.dataProvider != null) {
        list.add("dataprovider='${this.dataProvider!!.toPrettyString()}'")
    }
    element.attributeNames
        .filter { !dontDumpAttributes.contains(it) }
        .sorted() // the attributes may come in arbitrary order; make sure to sort them, in order to have predictable order and repeatable tests.
        .forEach { attributeName ->
            val value = element.getAttribute(attributeName)
            if (!value.isNullOrBlank()) {
                list.add("@$attributeName='$value'")
            }
        }
    if (this !is Html && !element.getProperty("innerHTML").isNullOrBlank()) {
        val innerHTML =
            element.getProperty("innerHTML").trim().replace(Regex("\\s+"), " ")
        list.add("innerHTML='$innerHTML'")
    }
    if (this is Notification) {
        list.add("'${this.getText().ellipsize(20)}'")
        if (!this.isOpened) {
            list.add("CLOSED")
        }
    }
    if (this.javaClass.hasCustomToString()) {
        // by default Vaadin components do not introduce toString() at all;
        // toString() therefore defaults to Object's toString() which is useless. However,
        // if a component does introduce a toString() then use it - it could provide
        // valuable information.
        list.add(this.toString())
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
public var prettyStringHook: (component: Component, list: LinkedList<String>) -> Unit = { _, _ -> }

/**
 * Never dump these attributes in [toPrettyString]. By default these attributes are ignored:
 *
 * * `disabled` - dumped separately as "DISABLED" string.
 * * `id` - dumped as Component.id
 * * `href` - there's special processing for [Anchor._href].
 * * `icon` - shown via [_iconName].
 */
public var dontDumpAttributes: MutableSet<String> = mutableSetOf("disabled", "id", "href", "icon")

/**
 * Pretty-prints a DataProvider.
 */
public fun DataProvider<*, *>.toPrettyString(): String = when {
    this is CallbackDataProvider ->
        // in Vaadin 24.4.0.alpha5 CallbackDataProvider has toString() but it prints
        // CallbackDataProvider(fetchCallback=com.github.mvysny.kaributesting.v10.Grid19TestKt$grid19Testbatch$3$2$2$grid$1$$Lambda/0x00007441dc57ec68@7be9582e, countCallback=com.vaadin.flow.data.provider.HasLazyDataView$$Lambda/0x00007441dc57d610@2297c8bf, idGetter=com.vaadin.flow.data.provider.CallbackDataProvider$$Lambda/0x00007441dc57d828@154fcedd)
        // so it's almost useless. Fall back to just javaClass.simpleName
        this.javaClass.simpleName
    javaClass.hasCustomToString() ->
        this.toString()
    this is ListDataProvider<*> ->
        "${javaClass.simpleName}<${items.firstOrNull()?.javaClass?.simpleName ?: "?"}>(${items.size} items)"
    this is HierarchicalDataProvider<*, *> && isInMemory ->
        // _rowSequence() only lists expanded nodes; calculating size from it is not going to be accurate.
        "${javaClass.simpleName}<${_rowSequence().firstOrNull()?.javaClass?.simpleName ?: "?"}>(? items)"
    this !is HierarchicalDataProvider<*, *> && isInMemory ->
        "${javaClass.simpleName}<${_findAll().firstOrNull()?.javaClass?.simpleName ?: "?"}>(${_findAll().size} items)"
    else ->
        this.javaClass.simpleName
}
