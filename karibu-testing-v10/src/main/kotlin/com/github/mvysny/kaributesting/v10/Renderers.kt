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
public fun <T> Renderer<T>._getPresentationValue(rowObject: T): String? = when (this) {
    is TemplateRenderer<T> -> {
        val renderedTemplateHtml: String = this.renderTemplate(rowObject)
        Jsoup.parse(renderedTemplateHtml).textRecursively
    }
    is BasicRenderer<T, *> -> {
        val value: Any? = this.valueProvider.apply(rowObject)
        _BasicRenderer_getFormattedValue.invoke(this, value) as String?
    }
    is TextRenderer<T> -> {
        renderText(rowObject)
    }
    is ComponentRenderer<*, T> -> {
        val component: Component = createComponent(rowObject)
        component.toPrettyString()
    }
    else -> if (this::class.simpleName == "LitRenderer") {
        // LitRenderer re-declares private members
        val templateProperty = this::class.java.getDeclaredField("templateExpression")
        templateProperty.isAccessible = true
        val templateExpression = templateProperty.get(this) as String

        val valueProvidersProperty = this::class.java.getDeclaredField("valueProviders")
        valueProvidersProperty.isAccessible = true
        val valueProviders = valueProvidersProperty.get(this) as Map<String, ValueProvider<T, *>>

        val renderedLitTemplateHtml: String = renderLitTemplate(templateExpression, valueProviders, rowObject)
        Jsoup.parse(renderedLitTemplateHtml).textRecursively
    } else {
        null
    }
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

public fun <T> renderLitTemplate(template: String, valueProviders: Map<String, ValueProvider<T, *>>, item: T): String {
    var renderedTemplate = template;
    valueProviders.forEach { (k: String, v: ValueProvider<T, *>) ->
        if (renderedTemplate.contains("\${item.$k}")) {
            renderedTemplate = renderedTemplate.replace("\${item.$k}", v.apply(item).toString())
        }
    }
    return renderedTemplate
}

/**
 * Returns the text rendered for given [item].
 */
@Suppress("UNCHECKED_CAST")
public fun <T> TextRenderer<T>.renderText(item: T): String =
    createComponent(item).element.text
