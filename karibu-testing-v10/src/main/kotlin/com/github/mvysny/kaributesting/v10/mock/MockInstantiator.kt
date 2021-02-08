package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.SemanticVersion
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.di.Instantiator
import com.vaadin.flow.i18n.I18NProvider
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.bytecode.assign.Assigner
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method

/**
 * Makes sure to load [MockNpmTemplateParser].
 */
public open class MockInstantiator(public val delegate: Instantiator) : Instantiator by delegate {
    override fun getTemplateParser(): TemplateParser = MockNpmTemplateParser()

    public companion object {
        public fun create(delegate: Instantiator): Instantiator {
            // starting from 14.5.0.alpha2 the LitTemplateParser machinery is supported
            if (VaadinMeta.fullVersion >= SemanticVersion(14, 5, 0, "a")
                && VaadinMeta.version == 14) {
                return MockInstantiatorV14_5_0(delegate)
            }
            if (VaadinMeta.version >= 18) {
                return MockInstantiatorV18(delegate)
            }
            return MockInstantiator(delegate)
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

    /**
     * Subclasses [baseClass] and overrides [methodName] which will now return the outcome of [delegate].
     */
    fun overrideMethod(baseClass: Class<*>, methodName: String, delegate: Method): Class<*> {
        return ByteBuddy().subclass(baseClass)
                .method(ElementMatchers.named(methodName))
                .intercept(MethodCall.invoke(delegate)
                        .withAllArguments()
                        .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .make()
                .load(ByteBuddyUtils::class.java.classLoader)
                .loaded
    }
}

/**
 * Used for Vaadin 14.5.0+. In order to load [MockNpmTemplateParser] and also hook into the
 * LitTemplateParser, we need to provide custom implementations of the `TemplateParserFactory`
 * class and the `LitTemplateParserFactory` class.
 *
 * Nasty class manipulation ahead, simply because this project compiles against Vaadin 14,
 * in order to keep Vaadin 14 compatibility.
 */
public class MockInstantiatorV14_5_0(delegate: Instantiator): MockInstantiator(delegate) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getOrCreate(type: Class<T>): T = when (type) {
        classLitTemplateParserFactory ->
            classMockLitTemplateParserFactory.getConstructor().newInstance() as T
        else -> super.getOrCreate(type)
    }

    override fun getI18NProvider(): I18NProvider? = delegate.i18NProvider

    private companion object {
        /**
         * The `LitTemplateParserImpl` class.
         */
        private val classLitTemplateParserImpl: Class<*> =
            Class.forName("com.vaadin.flow.component.littemplate.internal.LitTemplateParserImpl")

        /**
         * The `MockLitTemplateParserImpl` class loading templates via
         * [MockNpmTemplateParser.mockGetSourcesFromTemplate].
         */
        private val classMockLitTemplateParserImpl: Class<*> =
            ByteBuddyUtils.overrideMethod(classLitTemplateParserImpl,
                methodName = "getSourcesFromTemplate",
                MockNpmTemplateParser::class.java.getDeclaredMethod("mockGetSourcesFromTemplate", String::class.java, String::class.java))

        /**
         * The `LitTemplateParser.LitTemplateParserFactory` class.
         */
        private val classLitTemplateParserFactory: Class<*> =
            Class.forName("com.vaadin.flow.component.littemplate.LitTemplateParser${'$'}LitTemplateParserFactory")
        /**
         * The `LitTemplateParser.LitTemplateParserFactory` class returning `MockLitTemplateParser`.
         */
        private val classMockLitTemplateParserFactory: Class<*> =
            ByteBuddyUtils.overrideMethod(classLitTemplateParserFactory, "createParser") {
                classMockLitTemplateParserImpl.getConstructor().newInstance()
            }
    }
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
        classLitTemplateParserFactory ->
            classMockLitTemplateParserFactory.getConstructor().newInstance() as T
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

        /**
         * The `LitTemplateParserImpl` class.
         */
        private val classLitTemplateParserImpl: Class<*> =
                Class.forName("com.vaadin.flow.component.littemplate.internal.LitTemplateParserImpl")

        /**
         * The `MockLitTemplateParserImpl` class loading templates via
         * [MockNpmTemplateParser.mockGetSourcesFromTemplate].
         */
        private val classMockLitTemplateParserImpl: Class<*> =
                ByteBuddyUtils.overrideMethod(classLitTemplateParserImpl,
                        methodName = "getSourcesFromTemplate",
                        MockNpmTemplateParser::class.java.getDeclaredMethod("mockGetSourcesFromTemplate", String::class.java, String::class.java))

        /**
         * The `LitTemplateParser.LitTemplateParserFactory` class.
         */
        private val classLitTemplateParserFactory: Class<*> =
                Class.forName("com.vaadin.flow.component.littemplate.LitTemplateParser${'$'}LitTemplateParserFactory")
        /**
         * The `LitTemplateParser.LitTemplateParserFactory` class returning `MockLitTemplateParser`.
         */
        private val classMockLitTemplateParserFactory: Class<*> =
                ByteBuddyUtils.overrideMethod(classLitTemplateParserFactory, "createParser") {
                    classMockLitTemplateParserImpl.getConstructor().newInstance()
                }
    }
}
