package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.server.*
import java.lang.reflect.Constructor
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

/**
 * Workaround for https://github.com/mvysny/karibu-testing/issues/66
 */
internal val VaadinServlet.serviceSafe: VaadinServletService? get() {
    // for some reason a direct invocation of VaadinServlet::getService() FAILS with
    // Vaadin 20.0.0.alpha7 even though the method is definitely public.
    val m = VaadinServlet::class.java.getDeclaredMethod("getService")
    return m.invoke(this) as VaadinServletService?
}

/**
 * Workaround for https://github.com/mvysny/karibu-testing/issues/66
 */
internal fun createVaadinServletRequest(request: HttpServletRequest, service: VaadinService): VaadinServletRequest {
    // for some reason a direct constructor invocation FAILS with
    // Vaadin 20.0.0.alpha7 even though the constructor is definitely public.
    val constructor: Constructor<*> =
        VaadinServletRequest::class.java.declaredConstructors.first { it.parameterCount == 2 }
    return constructor.newInstance(request, service) as VaadinServletRequest
}

/**
 * Workaround for https://github.com/mvysny/karibu-testing/issues/66
 */
internal fun createVaadinServletResponse(response: HttpServletResponse, service: VaadinService): VaadinServletResponse {
    // for some reason a direct constructor invocation FAILS with
    // Vaadin 20.0.0.alpha7 even though the constructor is definitely public.
    val constructor: Constructor<*> =
        VaadinServletResponse::class.java.declaredConstructors.first { it.parameterCount == 2 }
    return constructor.newInstance(response, service) as VaadinServletResponse
}

internal fun createVaadinBrowser(request: VaadinRequest): WebBrowser {
    val constructor =
        WebBrowser::class.java.getDeclaredConstructor(VaadinRequest::class.java)
    constructor.isAccessible = true
    return constructor.newInstance(request)
}
