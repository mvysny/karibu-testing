package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.server.DeploymentConfigurationFactory
import com.vaadin.flow.server.VaadinServlet
import com.vaadin.flow.server.VaadinServletContext
import com.vaadin.flow.server.VaadinServletService
import java.util.*

/**
 * Makes sure that [routes] are properly registered, and that [MockService]
 * is used instead of vanilla [VaadinServletService].
 *
 * To use a custom servlet instead of this one, just pass it to [MockVaadin.setup].
 */
public open class MockVaadinServlet @JvmOverloads constructor(
        public val routes: Routes = Routes(),
        public val uiFactory: () -> UI = { MockedUI() }
) : VaadinServlet() {

    override fun createDeploymentConfiguration(): DeploymentConfiguration {
        MockVaadinHelper.mockFlowBuildInfo(this)
        return super.createDeploymentConfiguration()
    }

    override fun createDeploymentConfiguration(initParameters: Properties): DeploymentConfiguration {
        // make sure that Vaadin 14+ starts in npm mode even with `frontend/` and `flow-build-info.json` missing.
        // this check is required for testing a jar module with Vaadin 14 components.
        if (VaadinMeta.version == 14) {
            initParameters.remove(DeploymentConfigurationFactory::class.java.getDeclaredField("DEV_MODE_ENABLE_STRATEGY").get(null))
        }
        return super.createDeploymentConfiguration(initParameters)
    }

    override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
        val service: VaadinServletService = MockService(this, deploymentConfiguration, uiFactory)
        service.init()
        routes.register(service.context as VaadinServletContext)
        return service
    }
}