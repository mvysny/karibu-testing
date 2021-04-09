package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinServletContext
import elemental.json.Json
import elemental.json.JsonObject
import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext

/**
 * Makes sure to call LookupInitializer (present in Vaadin 19+).
 * @author Martin Vysny <mavi@vaadin.com>
 */
public object MockVaadin19 {

    public fun init(ctx: ServletContext) {
        if (!VaadinMeta.hasLookup) {
            // this Vaadin has no Lookup support, do nothing
            return
        }

        val loaderInitializerClass =
            Class.forName("com.vaadin.flow.server.startup.LookupServletContainerInitializer")
                .asSubclass(ServletContainerInitializer::class.java)
        val loaderInitializer: ServletContainerInitializer =
            loaderInitializerClass.getConstructor().newInstance()

        val loaders = mutableSetOf<Class<*>>(
            Class.forName("com.vaadin.flow.di.LookupInitializer"),
            Class.forName("com.vaadin.flow.di.LookupInitializer${'$'}ResourceProviderImpl")
        )
        if (VaadinMeta.fullVersion.isAtLeast(19)) {
            loaders.addAll(listOf(
                Class.forName("com.vaadin.flow.component.polymertemplate.rpc.PolymerPublishedEventRpcHandler"),
                Class.forName("com.vaadin.flow.server.frontend.fusion.EndpointGeneratorTaskFactoryImpl")
            ))
        }
        loaderInitializer.onStartup(loaders, ctx)

        // verify that the Lookup has been set
        verifyHasLookup(ctx)
    }

    /**
     * Verifies that the ctx has an instance of `com.vaadin.flow.di.Lookup` set, and returns it.
     * @return the instance of `com.vaadin.flow.di.Lookup`.
     */
    public fun verifyHasLookup(ctx: ServletContext): Any {
        check(VaadinMeta.hasLookup) { "Lookup is only available in Vaadin 19+ and 14.6+" }
        val lookup: Any? = ctx.getAttribute("com.vaadin.flow.di.Lookup")
        checkNotNull(lookup) {
            "The context doesn't contain the Vaadin 19 Lookup class. Available attributes: " + ctx.attributeNames.toList()
        }
        return lookup
    }
    public fun verifyHasLookup(ctx: VaadinContext): Any =
        verifyHasLookup((ctx as VaadinServletContext).context)

    /**
     * Calls `Lookup.lookup(Class)`.
     */
    public fun lookup(ctx: VaadinContext, clazz: Class<*>): Any? {
        val lookup = verifyHasLookup(ctx)
        val lookupClass = Class.forName("com.vaadin.flow.di.Lookup")
        val m = lookupClass.getDeclaredMethod("lookup", Class::class.java)
        return m.invoke(lookupClass.cast(lookup), clazz)
    }

    public fun getTokenFileFromClassloader(): JsonObject? {
        // Use DefaultApplicationConfigurationFactory.getTokenFileFromClassloader() to make sure to read
        // the same flow-build-info.json that Vaadin reads.
        val ctx: VaadinContext = MockVaadinHelper.createMockVaadinContext()
        val acf = lookup(ctx, Class.forName("com.vaadin.flow.server.startup.ApplicationConfigurationFactory"))
        checkNotNull(acf) { "ApplicationConfigurationFactory is null" }
        val dacfClass = Class.forName("com.vaadin.flow.server.startup.DefaultApplicationConfigurationFactory")
        if (dacfClass.isInstance(acf)) {
            val m = dacfClass.getDeclaredMethod("getTokenFileFromClassloader", VaadinContext::class.java)
            m.isAccessible = true
            val json = m.invoke(acf, ctx) as String? ?: return null
            return Json.parse(json)
        }
        return null
    }
}

// only Vaadin 19+ and Vaadin 14.6+ (but not Vaadin 15-18) has Lookup
internal val VaadinMeta.hasLookup: Boolean
    get() = fullVersion.isAtLeast(19) ||
            (fullVersion.isAtLeast(14,6) && fullVersion.isExactly(14))
