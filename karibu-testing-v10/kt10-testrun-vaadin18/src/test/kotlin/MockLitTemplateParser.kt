import com.github.mvysny.kaributesting.v10.mock.MockInstantiator
import com.github.mvysny.kaributesting.v10.mock.MockNpmTemplateParser
import com.vaadin.flow.di.Instantiator
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.bytecode.assign.Assigner
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method

object ByteBuddyUtils {
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

class MockInstantiatorV18(delegate: Instantiator): MockInstantiator(delegate) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getOrCreate(type: Class<T>): T = when (type) {
        classLitTemplateParserFactory ->
            classMockLitTemplateParserFactory.newInstance() as T
        classNpmTemplateParserFactory ->
            classMockNpmTemplateParserFactory.newInstance() as T
        else -> super.getOrCreate(type)
    }

    companion object {
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
                Class.forName("com.vaadin.flow.component.littemplate.MyLitTemplateParserImpl")

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
