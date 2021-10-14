package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.template
import com.github.mvysny.kaributools.textRecursively
import com.github.mvysny.kaributools.valueProvider
import com.vaadin.flow.component.Component
import com.vaadin.flow.data.renderer.*
import com.vaadin.flow.function.ValueProvider
import org.jsoup.Jsoup
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
    is TextRenderer<T> -> {
        renderText(rowObject)
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

/**
 * Returns the text rendered for given [item].
 */
@Suppress("UNCHECKED_CAST")
public fun <T> TextRenderer<T>.renderText(item: T): String =
    createComponent(item).element.text
