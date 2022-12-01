package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.UI
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.server.*
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

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
        if (VaadinVersion.get.major == 14) {
            initParameters.remove(
                DeploymentConfigurationFactory::class.java.getDeclaredField(
                    "DEV_MODE_ENABLE_STRATEGY"
                ).get(null)
            )
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

private val _VaadinServlet_getService: Method =
    VaadinServlet::class.java.getDeclaredMethod("getService")

/**
 * Workaround for https://github.com/mvysny/karibu-testing/issues/66
 */
internal val VaadinServlet.serviceSafe: VaadinServletService? get() {
    // we need to use the reflection. The problem is that the signature
    // of the method differs between Vaadin versions:
    //
    // Vaadin 14.6: getService() returns VaadinService
    // Vaadin 20+: getService() returns VaadinServletService
    //
    // calling the method directly will cause MethodNotFoundError on Vaadin 20+
    return _VaadinServlet_getService.invoke(this) as VaadinServletService?
}

/**
 * Workaround for https://github.com/mvysny/karibu-testing/issues/66
 */
internal fun createVaadinServletRequest(request: HttpServletRequest, service: VaadinService): VaadinServletRequest {
    // we need to use the reflection. The problem is that the signature
    // of the constructor differs between Vaadin versions:
    //
    // Vaadin 14.6: VaadinServletRequest(HttpServletRequest, VaadinServletService)
    // Vaadin 20+: VaadinServletRequest(HttpServletRequest, VaadinService)
    //
    // calling the constructor directly will cause MethodNotFoundError.
    val constructor: Constructor<*> =
        VaadinServletRequest::class.java.declaredConstructors.first { it.parameterCount == 2 }
    return constructor.newInstance(request, service) as VaadinServletRequest
}

private val _VaadinServletResponse_constructor: Constructor<*> =
    VaadinServletResponse::class.java.declaredConstructors.first { it.parameterCount == 2 }

/**
 * Workaround for https://github.com/mvysny/karibu-testing/issues/66
 */
internal fun createVaadinServletResponse(response: HttpServletResponse, service: VaadinService): VaadinServletResponse {
    // we need to use the reflection. The problem is that the signature
    // of the constructor differs between Vaadin versions:
    //
    // Vaadin 14.6: VaadinServletResponse(HttpServletResponse, VaadinServletService)
    // Vaadin 20+: VaadinServletResponse(HttpServletResponse, VaadinService)
    //
    // calling the constructor directly will cause MethodNotFoundError.
    return _VaadinServletResponse_constructor.newInstance(response, service) as VaadinServletResponse
}

private val _WebBrowser_constructor: Constructor<WebBrowser> =
    WebBrowser::class.java.getDeclaredConstructor(VaadinRequest::class.java).apply {
        isAccessible = true
    }

internal fun WebBrowser(request: VaadinRequest): WebBrowser =
    _WebBrowser_constructor.newInstance(request)

private val _VaadinService_createVaadinSession: Method =
    VaadinService::class.java.getDeclaredMethod("createVaadinSession", VaadinRequest::class.java).apply {
        isAccessible = true
    }

internal fun VaadinService._createVaadinSession(request: VaadinRequest): VaadinSession {
    return _VaadinService_createVaadinSession.invoke(this, request) as VaadinSession
}
