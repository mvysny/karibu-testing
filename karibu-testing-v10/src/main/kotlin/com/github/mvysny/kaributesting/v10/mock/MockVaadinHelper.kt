package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.mockhttp.MockContext
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.server.DeploymentConfigurationFactory
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinServlet
import com.vaadin.flow.server.VaadinServletContext
import elemental.json.Json
import elemental.json.JsonObject
import java.io.File
import java.lang.reflect.Method
import javax.servlet.ServletContext

public object MockVaadinHelper {
    @JvmStatic
    public fun mockFlowBuildInfo(servlet: VaadinServlet) {
        // we need to skip the test at DeploymentConfigurationFactory.verifyMode otherwise
        // testing a Vaadin 15 component module in npm mode without webpack.config.js nor flow-build-info.json would fail.
        if (VaadinMeta.flowBuildInfo == null) {
            // probably inside a Vaadin 15 component module. create a dummy token file so that
            // DeploymentConfigurationFactory.verifyMode() is happy.
            val tokenFile: File = File.createTempFile("flow-build-info", "json")
            tokenFile.writeText("{}")
            servlet.servletContext.setInitParameter("vaadin.frontend.token.file", tokenFile.absolutePath)
        }

        if (VaadinMeta.fullVersion.isAtLeast(14, 6) && VaadinMeta.fullVersion.isExactly(14)) {
            // otherwise DeploymentConfigurationFactory.verifyMode() will fail for Vaadin 14.6+
            servlet.servletContext.setInitParameter("compatibilityMode", "false")
        }
    }

    public fun createMockContext(): ServletContext {
        val ctx = MockContext()
        MockVaadin19.init(ctx)
        return ctx
    }

    public fun createMockVaadinContext(): VaadinContext =
        VaadinServletContext(createMockContext())

    public fun getTokenFileFromClassloader(): JsonObject? {
        checkVaadinSupportedByKaribuTesting()

        if (VaadinMeta.fullVersion.isAtLeast(19)) {
            return MockVaadin19.getTokenFileFromClassloader()
        }

        // Vaadin 14.6+
        // Use DeploymentConfigurationFactory.getTokenFileFromClassloader(Class, VaadinContext)
        val m: Method = DeploymentConfigurationFactory::class.java.getDeclaredMethod("getTokenFileFromClassloader", Class::class.java, VaadinContext::class.java)
        m.isAccessible = true
        val ctx: VaadinContext = createMockVaadinContext()
        val json: String = m.invoke(null, null, ctx) as String? ?: return null
        return Json.parse(json)
    }
}
