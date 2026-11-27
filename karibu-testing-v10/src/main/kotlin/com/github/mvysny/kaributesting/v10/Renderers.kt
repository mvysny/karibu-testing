package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.template
import com.github.mvysny.kaributools.textRecursively
import com.github.mvysny.kaributools.valueProvider
import com.vaadin.flow.component.Component
import com.vaadin.flow.data.renderer.*
import com.vaadin.flow.function.ValueProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
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
public fun <T> Renderer<T>._getPresentationValue(rowObject: T): String? = when {
    this is BasicRenderer<T, *> -> {
        val value: Any? = this.valueProvider.apply(rowObject)
        _BasicRenderer_getFormattedValue.invoke(this, value) as String?
    }
    this is TextRenderer<T> -> {
        renderText(rowObject)
    }
    this is ComponentRenderer<*, T> -> {
        val component: Component? = createComponent(rowObject)
        component?.toPrettyString() ?: ""
    }
    this is LitRenderer<*> -> {
        @Suppress("UNCHECKED_CAST")
        (this as LitRenderer<T>)._getPresentationJsoup(rowObject).textRecursively
    }
    else -> null
}

/**
 * Renders this Lit renderer's template for given [rowObject] and returns the resulting HTML
 * as-is, before any text extraction.
 *
 * Whereas [_getPresentationValue] strips everything down to text (`FOO Bar`), this keeps the
 * markup (`<a href='...'>FOO</a> <span>Bar</span>`), so you can assert on the presence of
 * particular tags or attributes. See [_getPresentationJsoup] for a directly queryable form.
 */
public fun <T> LitRenderer<T>._getPresentationHtml(rowObject: T): String =
    renderLitTemplate(litTemplate, valueProviders, rowObject)

private val _LitRenderer_getTemplate: Method? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    LitRenderer::class.java.declaredMethods
        .firstOrNull { it.name == "getTemplate" && it.parameterCount == 0 }
        ?.apply { isAccessible = true }
}

private val _JsDefinitionProxy_findDeclaredCall: Method by lazy(LazyThreadSafetyMode.PUBLICATION) {
    Class.forName("com.vaadin.flow.js.JsDefinitionProxy")
        .getMethod("findDeclaredCall", Class.forName("com.vaadin.flow.js.JsSnippet"))
}

private val compiledLitTemplateBody = Regex("""^\s*return\s+html`(.*)`;?\s*$""", RegexOption.DOT_MATCHES_ALL)

/**
 * The Lit markup this renderer renders, for both the string form
 * (`LitRenderer.of("<b>${item.name}</b>")`) and the `CompiledLitTemplate` form of Vaadin 25.4+,
 * whose `return html`<b>${item.name}</b>`;` expression is unwrapped to the same markup, with
 * the declaring call's arguments substituted for `${$0}`, `${$1}`…
 *
 * TODO delete once karibu-tools 0.28 is out, together with the three `private val`s above it,
 * and call karibu-tools' `template` instead: 0.28 reads `CompiledLitTemplate` itself.
 */
private val LitRenderer<*>.litTemplate: String
    get() {
        val expression = template
        if (expression.isNotEmpty()) return expression
        val compiled: Any = _LitRenderer_getTemplate?.invoke(this) ?: return ""
        val call: Any = _JsDefinitionProxy_findDeclaredCall.invoke(null, compiled) ?: return ""
        val js = call.javaClass.getMethod("getExpression").invoke(call) as String
        val arguments = call.javaClass.getMethod("arguments").invoke(call) as List<*>
        var markup = compiledLitTemplateBody.matchEntire(js)?.groupValues?.get(1) ?: js
        arguments.forEachIndexed { index, arg -> markup = markup.replace("\${\$$index}", arg.toString()) }
        return markup
    }

/**
 * Renders this Lit renderer's template for given [rowObject] and returns it parsed by JSoup,
 * so that you can query the rendered markup further, for example:
 * ```
 * expect(1) { litRenderer._getPresentationJsoup(row).select("a[href]").size }
 * ```
 * The returned [Element] is the `<body>` of the parsed fragment; [Element.select] queries
 * its descendants recursively.
 *
 * See [_getPresentationHtml] for the raw HTML string and [_getPresentationValue] for the
 * text-only counterpart.
 */
public fun <T> LitRenderer<T>._getPresentationJsoup(rowObject: T): Element =
    Jsoup.parseBodyFragment(_getPresentationHtml(rowObject)).body()

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
