package com.github.mvysny.kaributesting.v10.spring

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.UI
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.server.ServiceException
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinServletService
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.SpringServlet
import com.vaadin.flow.spring.SpringVaadinServletService
import com.vaadin.flow.spring.SpringVaadinSession
import org.springframework.context.ApplicationContext
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.ServletConfig
import javax.servlet.ServletException

/**
 * @author mavi
 */
internal class MockSpringServlet(val routes: Routes, val ctx: ApplicationContext, val uiFactory: () -> UI) : SpringServlet(ctx, false) {
    @Throws(ServletException::class)
    override fun init(servletConfig: ServletConfig) {
        routes.register(servletConfig.servletContext)
        super.init(servletConfig)
    }

    @Throws(ServiceException::class)
    override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
        val service: VaadinServletService = object : SpringVaadinServletService(this, deploymentConfiguration, ctx) {
            override fun isAtmosphereAvailable(): Boolean {
                return false
            }

            override fun getMainDivId(session: VaadinSession, request: VaadinRequest): String {
                return "ROOT-1"
            }

            override fun createVaadinSession(request: VaadinRequest): VaadinSession {
                return MockSpringVaadinSession(this, uiFactory)
            }
        }
        service.init()
        return service
    }
}

open class MockSpringVaadinSession(service: SpringVaadinServletService, public val uiFactory: () -> UI) : SpringVaadinSession(service) {
    /**
     * We need to pretend that we have the UI lock during the duration of the test method, otherwise
     * Vaadin would complain that there is no session lock.
     * The easiest way is to simply always provide a locked lock :)
     */
    private val lock: ReentrantLock = ReentrantLock().apply { lock() }

    override fun getLockInstance(): Lock = lock
    override fun close() {
        super.close()
        MockVaadin.afterSessionClose(this, uiFactory)
    }
}
