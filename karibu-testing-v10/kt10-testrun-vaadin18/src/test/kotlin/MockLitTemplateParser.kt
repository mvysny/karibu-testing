package com.vaadin.flow.component.littemplate;

import com.github.mvysny.kaributesting.v10.mock.MockInstantiator
import com.github.mvysny.kaributesting.v10.mock.MockNpmTemplateParser
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.di.Instantiator

private class MockLitTemplateParser : LitTemplateParserImpl() {
    /**
     * @param tag the value of the [com.vaadin.flow.component.Tag] annotation, e.g. `my-component`
     * @param url the URL resolved according to the [com.vaadin.flow.component.dependency.JsModule] spec, for example `./view/my-view.js` or `@vaadin/vaadin-button.js`.
     */
    override fun getSourcesFromTemplate(tag: String, url: String): String? {
        return MockNpmTemplateParser.mockGetSourcesFromTemplate(tag, url)
    }

    companion object Factory : LitTemplateParser.LitTemplateParserFactory() {
        override fun createParser(): LitTemplateParser = MockLitTemplateParser()
    }
}

private object MockNpmTemplateParserFactory : TemplateParser.TemplateParserFactory() {
    override fun createParser(): TemplateParser = MockNpmTemplateParser()
}

class MockInstantiatorV18(delegate: Instantiator): MockInstantiator(delegate) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getOrCreate(type: Class<T>): T = when (type) {
        LitTemplateParser.LitTemplateParserFactory::class.java ->
            MockLitTemplateParser.Factory as T
        TemplateParser.TemplateParserFactory::class.java ->
            MockNpmTemplateParserFactory as T
        else -> super.getOrCreate(type)
    }
}
