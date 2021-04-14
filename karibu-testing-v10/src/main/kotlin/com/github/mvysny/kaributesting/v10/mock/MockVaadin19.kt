package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.di.Lookup
import com.vaadin.flow.di.LookupInitializer
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinServletContext
import com.vaadin.flow.server.startup.LookupServletContainerInitializer
import elemental.json.Json
import elemental.json.JsonObject
import javax.servlet.ServletContext

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
    public fun verifyHasLookup(ctx: ServletContext): Lookup {
        check(VaadinMeta.hasLookup) { "Lookup is only available in Vaadin 19+ and 14.6+" }
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

internal fun checkVaadinSupportedByKaribuTesting() {
    if (!VaadinMeta.hasLookup) {
        // this Vaadin has no Lookup support => unsupported
        throw RuntimeException("Karibu-Testing 1.3+ only support Vaadin 19+ and Vaadin 14.6+ but the project uses Vaadin ${VaadinMeta.fullVersion}")
    }
}
