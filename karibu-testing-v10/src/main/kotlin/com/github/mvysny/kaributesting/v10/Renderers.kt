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

private val _BasicRenderer_getFormattedValue: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val getFormattedValueM: Method = BasicRenderer::class.java.declaredMethods
        .first { it.name == "getFormattedValue" }
    getFormattedValueM.isAccessible = true
    getFormattedValueM
}

/**
 * Returns the output of this renderer for given [rowObject] formatted as close as possible
 * to the client-side output.
 */
public fun <T : Any> Renderer<T>._getPresentationValue(rowObject: T): Any? = when (this) {
    is TemplateRenderer<T> -> {
        val renderedTemplateHtml: String = this.renderTemplate(rowObject)
        Jsoup.parse(renderedTemplateHtml).textRecursively
    }
    is BasicRenderer<T, *> -> {
        val value: Any? = this.valueProvider.apply(rowObject)
        _BasicRenderer_getFormattedValue.invoke(this, value)
    }
    is ComponentRenderer<*, T> -> {
        val component: Component = createComponent(rowObject)
        component.toPrettyString()
    }
    else -> null
}

/**
 * Renders the template for given [item]
 */
public fun <T> TemplateRenderer<T>.renderTemplate(item: T): String {
    var template: String = this.template
    this.valueProviders.forEach { (k: String, v: ValueProvider<T, *>) ->
        if (template.contains("[[item.$k]]")) {
            template = template.replace("[[item.$k]]", v.apply(item).toString())
        }
    }
    return template
}

private val _BasicRenderer_valueProvider: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val javaField: Field = BasicRenderer::class.java.getDeclaredField("valueProvider")
    javaField.isAccessible = true
    javaField
}

/**
 * Returns the [ValueProvider] set to [BasicRenderer].
 */
@Suppress("UNCHECKED_CAST", "ConflictingExtensionProperty")
public val <T, V> BasicRenderer<T, V>.valueProvider: ValueProvider<T, V>
    get() = _BasicRenderer_valueProvider.get(this) as ValueProvider<T, V>

private val _Renderer_template: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val templateF: Field = Renderer::class.java.getDeclaredField("template")
    templateF.isAccessible = true
    templateF
}

/**
 * Returns the Polymer Template set to the [Renderer].
 */
public val Renderer<*>.template: String
    get() {
        val template: String? = _Renderer_template.get(this) as String?
        return template ?: ""
    }
