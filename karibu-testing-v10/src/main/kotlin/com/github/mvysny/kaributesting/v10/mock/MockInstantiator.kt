package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.di.Instantiator
import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
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
        // we're extending a non-public class; we need to use special hacks.
        // workaround 1: place the new class into the same package
        // workaround 2: use ClassLoadingStrategy.Default.INJECTION
        // remove when https://github.com/vaadin/flow/issues/9169 is fixed

        return ByteBuddy().subclass(baseClass)
                .name("com.vaadin.flow.component.littemplate.MyLitTemplateParserImpl")
                .method(ElementMatchers.named(methodName))
                .intercept(MethodCall.invoke(delegate)
                        .withAllArguments()
                        .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .make()
                .load(ByteBuddyUtils::class.java.classLoader, ClassLoadingStrategy.Default.INJECTION)
                .loaded
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
            classMockLitTemplateParserFactory.newInstance() as T
        classNpmTemplateParserFactory ->
            classMockNpmTemplateParserFactory.newInstance() as T
        else -> super.getOrCreate(type)
    }

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
                Class.forName("com.vaadin.flow.component.littemplate.LitTemplateParserImpl")

        /**
         * The `MockLitTemplateParserImpl` class loading templates via
         * [MockNpmTemplateParser.mockGetSourcesFromTemplate].
         */
        private val classMockLitTemplateParserImpl: Class<*> =
                ByteBuddyUtils.overrideMethod(classLitTemplateParserImpl, "getSourcesFromTemplate",
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
                    classMockLitTemplateParserImpl.newInstance()
                }
    }
}
