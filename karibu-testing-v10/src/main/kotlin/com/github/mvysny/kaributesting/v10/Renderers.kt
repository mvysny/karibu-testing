package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.data.renderer.BasicRenderer
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.data.renderer.TemplateRenderer
import com.vaadin.flow.function.ValueProvider
import org.jsoup.Jsoup
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Formats the value produced by this Renderer in a nice way.
 */
fun <T: Any> Renderer<T>._getPresentationValue(rowObject: T): Any? = when (this) {
    is TemplateRenderer<T> -> {
        val renderedTemplateHtml: String = renderTemplate(rowObject)
        Jsoup.parse(renderedTemplateHtml).textRecursively
    }
    is BasicRenderer<T, *> -> {
        val value: Any? = valueProvider.apply(rowObject)
        val getFormattedValueM: Method = BasicRenderer::class.java.declaredMethods
                .first { it.name == "getFormattedValue" }
        getFormattedValueM.isAccessible = true
        getFormattedValueM.invoke(this, value)
    }
    is ComponentRenderer<*, T> -> {
        val component: Component = createComponent(rowObject)
        component.toPrettyString()
    }
    else -> null
}

@Suppress("UNCHECKED_CAST", "ConflictingExtensionProperty") // conflicting property is "protected"
val <T, V> BasicRenderer<T, V>.valueProvider: ValueProvider<T, V>
    get() {
    val javaField: Field = BasicRenderer::class.java.getDeclaredField("valueProvider").apply {
        isAccessible = true
    }
    return javaField.get(this) as ValueProvider<T, V>
}

/**
 * Renders the template for given [item]
 */
fun <T> TemplateRenderer<T>.renderTemplate(item: T): String {
    var template: String = this.template
    this.valueProviders.forEach { (k: String, v: ValueProvider<T, *>) ->
        if (template.contains("[[item.$k]]")) {
            template = template.replace("[[item.$k]]", v.apply(item).toString())
        }
    }
    return template
}

val Renderer<*>.template: String
    get() {
        val template: String? = Renderer::class.java.getDeclaredField("template").run {
            isAccessible = true
            get(this@template) as String?
        }
        return template ?: ""
    }