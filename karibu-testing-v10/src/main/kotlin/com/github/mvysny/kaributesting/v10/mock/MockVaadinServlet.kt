package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.server.*
import com.vaadin.flow.server.frontend.FrontendUtils
import jakarta.servlet.ServletException
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * Makes sure that [routes] are properly registered, and that [MockService]
 * is used instead of vanilla [VaadinServletService].
 *
 * To use a custom servlet instead of this one, just pass it to [MockVaadin.setup].
 */
public open class MockVaadinServlet @JvmOverloads constructor(
        public val routes: Routes = Routes(),
        public val uiFactory: () -> UI = @JvmSerializableLambda { MockedUI() }
) : VaadinServlet() {

    @Throws(ServletException::class)
    override fun createDeploymentConfiguration(): DeploymentConfiguration {
        MockVaadinHelper.mockFlowBuildInfo(this)
        // workaround for https://github.com/vaadin/flow/issues/18682
        System.setProperty(Constants.VAADIN_PREFIX + FrontendUtils.PARAM_FRONTEND_DIR, File(FrontendUtils.DEFAULT_FRONTEND_DIR).absolutePath)
        System.setProperty(Constants.VAADIN_PREFIX + FrontendUtils.PROJECT_BASEDIR, File(".").absolutePath)
        return super.createDeploymentConfiguration()
    }

    @Throws(ServiceException::class)
    override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
        val service: VaadinServletService = MockService(this, deploymentConfiguration, uiFactory)
        service.init()
        routes.register(service.context as VaadinServletContext)
        return service
    }
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
