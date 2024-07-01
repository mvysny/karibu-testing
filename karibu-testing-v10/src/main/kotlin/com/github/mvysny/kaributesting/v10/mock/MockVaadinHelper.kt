package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.fakeservlet.FakeContext
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.VaadinServlet
import com.vaadin.flow.server.VaadinServletContext
import elemental.json.JsonObject
import java.io.File
import jakarta.servlet.ServletContext

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
    }

    public fun createMockContext(): ServletContext {
        val ctx = FakeContext()
        MockVaadin19.init(ctx)
        return ctx
    }

    public fun createMockVaadinContext(): VaadinContext =
        VaadinServletContext(createMockContext())

    public fun getTokenFileFromClassloader(): JsonObject? {
        // no need to optimize this method; the output is cached in VaadinMeta.flowBuildInfo
        checkVaadinSupportedByKaribuTesting()

        return MockVaadin19.getTokenFileFromClassloader()
    }
}
