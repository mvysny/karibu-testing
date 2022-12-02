package com.github.mvysny.kaributesting.v10.mock

import com.vaadin.flow.component.littemplate.LitTemplateParser
import com.vaadin.flow.component.littemplate.internal.LitTemplateParserImpl
import com.vaadin.flow.di.Instantiator
import com.vaadin.flow.i18n.I18NProvider
import com.vaadin.flow.server.VaadinService

/**
 * Makes sure to load [MockNpmTemplateParser].
 */
public open class MockInstantiator(public val delegate: Instantiator) : Instantiator by delegate {
    public companion object {
        @JvmStatic
        public fun create(delegate: Instantiator): Instantiator {
            checkVaadinSupportedByKaribuTesting()

            return MockInstantiatorV18(delegate)
        }
    }
}

private object MockLitTemplateParserImpl : LitTemplateParserImpl() {
    override fun getSourcesFromTemplate(service: VaadinService, tag: String, url: String): String =
        MockNpmTemplateParser.mockGetSourcesFromTemplate(tag, url)
}

private object MockLitTemplateParserFactory : LitTemplateParser.LitTemplateParserFactory() {
    override fun createParser() = MockLitTemplateParserImpl
}

/**
 * Used for Vaadin 18+. In order to load [MockNpmTemplateParser] and also hook into the
 * LitTemplateParser, we need to provide custom implementations of the `TemplateParserFactory`
 * class and the `LitTemplateParserFactory` class.
 *
 * Nasty class manipulation ahead, simply because this project compiles against Vaadin 14,
 * in order to keep Vaadin 14 compatibility.
 */
public class MockInstantiatorV18(delegate: Instantiator): MockInstantiator(delegate) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getOrCreate(type: Class<T>): T = when (type) {
        LitTemplateParser.LitTemplateParserFactory::class.java ->
            MockLitTemplateParserFactory as T
        else -> super.getOrCreate(type)
    }

    override fun getI18NProvider(): I18NProvider? = delegate.i18NProvider
}
