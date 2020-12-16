package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.mockhttp.MockContext
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinServletContext
import javax.servlet.ServletContainerInitializer
import javax.servlet.ServletContext

/**
 * Makes sure to call LookupInitializer (present in Vaadin 19+).
 * @author Martin Vysny <mavi@vaadin.com>
 */
public object MockVaadin19 {

    public fun init(ctx: ServletContext) {
        if (VaadinMeta.version < 19) {
            // Vaadin 18 and lower has no Loader, do nothing
            return
        }

        val loaderInitializerClass =
            Class.forName("com.vaadin.flow.server.startup.LookupInitializer")
                .asSubclass(ServletContainerInitializer::class.java)
        val loaderInitializer: ServletContainerInitializer =
            loaderInitializerClass.getConstructor().newInstance()

        loaderInitializer.onStartup(
            setOf(
                Class.forName("com.vaadin.flow.component.polymertemplate.rpc.PolymerPublishedEventRpcHandler"),
                Class.forName("com.vaadin.flow.server.startup.LookupInitializer${'$'}ResourceProviderImpl"),
                Class.forName("com.vaadin.flow.server.frontend.fusion.EndpointGeneratorTaskFactoryImpl")
            ),
            ctx
        )

        // verify that the Lookup has been set
        val lookup: Any? = ctx.getAttribute("com.vaadin.flow.di.Lookup")
        checkNotNull(lookup)
    }

    internal fun createContextWithLookup(): VaadinContext {
        val ctx = MockContext()
        init(ctx)
        return VaadinServletContext(ctx)
    }
}
