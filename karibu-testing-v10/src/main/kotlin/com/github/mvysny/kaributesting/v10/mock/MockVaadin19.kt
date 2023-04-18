package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.di.Lookup
import com.vaadin.flow.di.LookupInitializer
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinServletContext
import com.vaadin.flow.server.startup.ApplicationConfigurationFactory
import com.vaadin.flow.server.startup.DefaultApplicationConfigurationFactory
import com.vaadin.flow.server.startup.LookupServletContainerInitializer
import elemental.json.Json
import elemental.json.JsonObject
import jakarta.servlet.ServletContext

/**
 * Makes sure to call LookupInitializer (present in Vaadin 19+).
 * @author Martin Vysny <mavi@vaadin.com>
 */
public object MockVaadin19 {

    public fun init(ctx: ServletContext) {
        checkVaadinSupportedByKaribuTesting()

        val loaderInitializer = LookupServletContainerInitializer()

        val loaders = mutableSetOf<Class<*>>(
            LookupInitializer::class.java,
            Class.forName("com.vaadin.flow.di.LookupInitializer${'$'}ResourceProviderImpl")
        )

        fun tryLoad(clazz: String) {
            // sometimes customers don't include entire vaadin-core and exclude stuff like fusion on purpose.
            // load the class only if it exists.
            try {
                loaders.add(Class.forName(clazz))
            } catch (ex: ClassNotFoundException) {}
        }

        tryLoad("com.vaadin.fusion.frontend.EndpointGeneratorTaskFactoryImpl")
        loaderInitializer.onStartup(loaders, ctx)

        // verify that the Lookup has been set
        verifyHasLookup(ctx)
    }

    /**
     * Verifies that the ctx has an instance of `com.vaadin.flow.di.Lookup` set, and returns it.
     * @return the instance of `com.vaadin.flow.di.Lookup`.
     */
    public fun verifyHasLookup(ctx: ServletContext): Lookup {
        val lookup: Any? = ctx.getAttribute("com.vaadin.flow.di.Lookup")
        checkNotNull(lookup) {
            "The context doesn't contain the Vaadin 19 Lookup class. Available attributes: " + ctx.attributeNames.toList()
        }
        return lookup as Lookup
    }
    public fun verifyHasLookup(ctx: VaadinContext): Lookup =
        verifyHasLookup((ctx as VaadinServletContext).context)

    /**
     * Calls `Lookup.lookup(Class)`.
     */
    public fun lookup(ctx: VaadinContext, clazz: Class<*>): Any? {
        val lookup: Lookup = verifyHasLookup(ctx)
        return lookup.lookup(clazz)
    }

    public fun getTokenFileFromClassloader(): JsonObject? {
        // Use DefaultApplicationConfigurationFactory.getTokenFileFromClassloader() to make sure to read
        // the same flow-build-info.json that Vaadin reads.

        // this thing only works with Vaadin 19+
        val ctx: VaadinContext = MockVaadinHelper.createMockVaadinContext()
        val acf = lookup(ctx, ApplicationConfigurationFactory::class.java)
        checkNotNull(acf) { "ApplicationConfigurationFactory is null" }
        if (acf is DefaultApplicationConfigurationFactory) {
            val m = DefaultApplicationConfigurationFactory::class.java.getDeclaredMethod("getTokenFileFromClassloader", VaadinContext::class.java)
            m.isAccessible = true
            val json = m.invoke(acf, ctx) as String? ?: return null
            return Json.parse(json)
        }
        return null
    }
}

internal fun checkVaadinSupportedByKaribuTesting() {
    if (!VaadinVersion.get.isAtLeast(24)) {
        // this Vaadin has no Lookup support => unsupported
        throw RuntimeException("Karibu-Testing 2+ only support Vaadin 24+ but the project uses Vaadin ${VaadinVersion.get}. Please try Karibu-Testing 1.3.x instead.")
    }
}
