package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.littemplate.LitTemplateParser
import com.vaadin.flow.component.littemplate.internal.LitTemplateParserImpl
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.di.Instantiator
import com.vaadin.flow.i18n.I18NProvider
import com.vaadin.flow.server.VaadinService
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.matcher.ElementMatchers

/**
 * Makes sure to load [MockNpmTemplateParser].
 */
public open class MockInstantiator(public val delegate: Instantiator) : Instantiator by delegate {
    override fun getTemplateParser(): TemplateParser = MockNpmTemplateParser()

    public companion object {
        @JvmStatic
        public fun create(delegate: Instantiator): Instantiator {
            checkVaadinSupportedByKaribuTesting()

            if (VaadinVersion.get.major >= 19) {
                return MockInstantiatorV18(delegate)
            }

            // Vaadin 14.6+
            // starting from 14.5.0.alpha2 the LitTemplateParser machinery is supported
            return MockInstantiatorV14_6_0(delegate)
        }
    }
}

private object ByteBuddyUtils {
    /**
     * Subclasses [baseClass] and overrides [methodName] which will now return [withResult].
     */
    fun overrideMethod(baseClass: Class<*>, methodName: String, withResult: ()->Any?): Class<*> {
        return ByteBuddy().subclass(baseClass)
                .method(ElementMatchers.named(methodName))
                .intercept(MethodCall.call(withResult))
                .make()
                .load(ByteBuddyUtils::class.java.classLoader)
                .loaded
    }
}

/**
 * Used for Vaadin 14 (only Vaadin 14.6+). In order to load [MockNpmTemplateParser] and also hook into the
 * LitTemplateParser, we need to provide custom implementations of the `TemplateParserFactory`
 * class and the `LitTemplateParserFactory` class.
 */
public class MockInstantiatorV14_6_0(delegate: Instantiator): MockInstantiator(delegate) {

    init {
        check(
            VaadinVersion.get.major == 14 && VaadinVersion.get.isAtLeast(14, 6)) {
            "Unsupported Vaadin version: ${VaadinVersion.get}"
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getOrCreate(type: Class<T>): T = when (type) {
        LitTemplateParser.LitTemplateParserFactory::class.java ->
            MockLitTemplateParserFactory as T
        else -> super.getOrCreate(type)
    }

    override fun getI18NProvider(): I18NProvider? = delegate.i18NProvider
}

private object MockLitTemplateParserImpl : LitTemplateParserImpl() {
    override fun getSourcesFromTemplate(tag: String, url: String): String =
        MockNpmTemplateParser.mockGetSourcesFromTemplate(tag, url)
    // Vaadin 22.0.0.beta2+ adds a new `service` parameter, need to override that function as well.
    open fun getSourcesFromTemplate(service: VaadinService, tag: String, url: String): String =
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
        classNpmTemplateParserFactory ->
            classMockNpmTemplateParserFactory.getConstructor().newInstance() as T
        else -> super.getOrCreate(type)
    }

    override fun getI18NProvider(): I18NProvider? = delegate.i18NProvider

    private companion object {
        /**
         * The `TemplateParser.TemplateParserFactory` class.
         */
        private val classNpmTemplateParserFactory: Class<*> =
                Class.forName("com.vaadin.flow.component.polymertemplate.TemplateParser${'$'}TemplateParserFactory")

        /**
         * The `TemplateParser.TemplateParserFactory` class returning [MockNpmTemplateParser].
         */
        private val classMockNpmTemplateParserFactory: Class<*> =
                ByteBuddyUtils.overrideMethod(classNpmTemplateParserFactory, "createParser") { MockNpmTemplateParser() }
    }
}
