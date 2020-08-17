package com.github.mvysny.kaributesting.v8

import com.vaadin.data.HasValue
import com.vaadin.server.*
import com.vaadin.ui.*
import java.lang.reflect.Field
import java.lang.reflect.Method

import java.util.ArrayList

/**
 * If true, [PrettyPrintTree] will use `\--` instead of `└──` which tend to render on some terminals as `???`.
 */
public var prettyPrintUseAscii: Boolean = false

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
            if (root is HasComponents) {
                for (child in root) {
                    result.children.add(ofVaadin(child))
                }
            }
            return result
        }
    }
}

public fun Component.toPrettyTree(): String = PrettyPrintTree.ofVaadin(this).print()

public fun Component.toPrettyString(): String {
    val list = ArrayList<String>()
    if (id != null) {
        list.add("#$id")
    }
    if (!isVisible) {
        list.add("INVIS")
    }
    if (this is HasValue<*> && this.isReadOnly) {
        list.add("RO")
    }
    if (!isEnabled) {
        list.add("DISABLED")
    }
    if (!styleName.isNullOrBlank()) {
        list.add(this.styleName.split("\\s+".toRegex())
                .map { it.trim() }
                .filter { it.isNotBlank() }.joinToString(" ") { ".$it" }
        )
    }
    if (caption != null) {
        list.add("caption='$caption'")
    }
    if (this is HasValue<*> || this is Label) {
        list.add("value='$value'")
    }
    if (this is AbstractComponent) {
        if (componentError != null) {
            list.add("componentError='${componentError.message}'")
        }
        if (errorMessage != null && errorMessage != componentError) {
            list.add("errorMessage='${errorMessage.message}'")
        }
    }
    if (this is Grid<*>) {
        list.add("columns=[${this.columns.filter { !it.isHidden }.joinToString { "'${it.caption}'" }}]")
    }
    if (this is Link) {
        list.add("href='${resource?.toPrettyString() ?: ""}'")
    }
    if (this is Image) {
        list.add("src='${source?.toPrettyString() ?: ""}'")
    }
    var name = javaClass.simpleName
    if (name.isEmpty()) {
        // anonymous classes
        name = javaClass.name
    }
    return name + list
}

public fun Resource.toPrettyString(): String = when(this) {
    is ExternalResource -> this.url
    is ClassResource -> this.toPrettyString()
    is GenericFontIcon -> "${javaClass.simpleName}[${this.fontFamily}/0x${this.codepoint.toString(16)}]"
    is StreamResource -> "${javaClass.simpleName}[$filename]"
    is FileResource -> "${javaClass.simpleName}[$sourceFile]"
    else -> "${javaClass.simpleName}[$this]"
}

public fun ClassResource.toPrettyString(): String {
    val getAssociatedClassMethod: Method = ClassResource::class.java.getDeclaredMethod("getAssociatedClass").apply { isAccessible = true }
    val associatedClass: Class<*> = getAssociatedClassMethod.invoke(this) as Class<*>
    val resourceNameField: Field = ClassResource::class.java.getDeclaredField("resourceName").apply { isAccessible = true }
    val resourceName: String = resourceNameField.get(this) as String
    return "ClassResource[${associatedClass.name}/$resourceName]"
}
