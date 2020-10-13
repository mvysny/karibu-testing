package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.MockNpmTemplateParser
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.di.Instantiator

/**
 * Makes sure to load [MockNpmTemplateParser].
 */
public open class MockInstantiator(public val delegate: Instantiator) : Instantiator by delegate {
    override fun getTemplateParser(): TemplateParser = MockNpmTemplateParser()
}