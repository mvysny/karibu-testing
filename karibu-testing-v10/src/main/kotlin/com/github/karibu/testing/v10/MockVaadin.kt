package com.github.karibu.testing.v10

import com.github.karibu.mockhttp.MockContext
import com.github.karibu.mockhttp.MockHttpSession
import com.github.karibu.mockhttp.MockRequest
import com.github.karibu.mockhttp.MockServletConfig
import com.github.karibu.testing.v10.javasupport.ServiceFactory
import com.github.karibu.testing.v10.javasupport.UIFactory
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.function.SerializableConsumer
import com.vaadin.flow.internal.CurrentInstance
import com.vaadin.flow.internal.ExecutionContext
import com.vaadin.flow.internal.StateTree
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.server.*
import com.vaadin.flow.server.startup.RouteRegistry
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    private var strongRefSession: VaadinSession? = null
    private var strongRefUI: UI? = null
    private var strongRefReq: VaadinRequest? = null
    private var lastNavigation: Location? = null

    /**
     * Mocks Vaadin for the current test method:
     * ```
     * MockVaadin.setup(Routes().autoDiscoverViews("com.myapp"))
     * ```
     * @param routes all classes annotated with [com.vaadin.flow.router.Route]; use [Routes.autoDiscoverViews] to auto-discover all such classes.
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class. If you decide to
     * provide a different value, override [UI.beforeClientResponse] so that your dialogs are opened properly with this mocked testing - see
     * [MockedUI.beforeClientResponse] for details.
     * @param serviceFactory allows you to provide your own implementation of [VaadinServletService] which allows you to e.g. override
     * [VaadinServletService.loadInstantiators] and provide your own way of instantiating Views, e.g. via Spring or Guice.
     */
    fun setup(routes: Routes = Routes(), uiFactory: ()->UI = { MockedUI() },
              serviceFactory: (VaadinServlet, DeploymentConfiguration, RouteRegistry) -> VaadinServletService = { servlet, dc, reg -> MockService(servlet, dc, reg) }) {
        // init servlet
        val servlet = object : VaadinServlet() {
            override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
                val service = serviceFactory(this, deploymentConfiguration, routes.createRegistry())
                service.init()
                return service
            }
        }
        val ctx = MockContext()
        servlet.init(MockServletConfig(ctx))
        VaadinService.setCurrent(servlet.service!!)

        // init Vaadin Session
        createSession(ctx, servlet, uiFactory)
    }

    /**
     * One more overloaded setup() for use in Java and Groovy
     */
    @JvmStatic @JvmOverloads fun setup(routes: Routes = defaultRoutes(),
                                       uiFactory: UIFactory = defaultUIFactory(),
                                       serviceFactory: ServiceFactory = defaultServiceFactory()) {
        setup(routes, uiFactory::provide, serviceFactory::provide)
    }

    /**
     * One more overloaded setup() for use in Java and Groovy
     */
    @JvmStatic fun setup(routes: Routes = defaultRoutes(),
                         serviceFactory: ServiceFactory = defaultServiceFactory()) =
            setup(routes = routes, uiFactory = defaultUIFactory(), serviceFactory = serviceFactory)

    private fun defaultRoutes() = Routes()
    private fun defaultServiceFactory() = ServiceFactory { servlet, dc, reg -> MockService(servlet, dc, reg) }
    private fun defaultUIFactory() = UIFactory { MockedUI() }

    private fun closeCurrentUI() {
        val ui: UI = UI.getCurrent() ?: return
        lastNavigation = ui.internals.activeViewLocation
        ui.close()
        ui._fireEvent(DetachEvent(ui))
        UI.setCurrent(null)
        strongRefUI = null
    }

    private fun createSession(ctx: MockContext, servlet: VaadinServlet, uiFactory: ()->UI) {
        val httpSession = MockHttpSession.create(ctx)

        val session = object : VaadinSession(servlet.service) {
            private val lock = ReentrantLock().apply { lock() }
            override fun getLockInstance(): Lock = lock
            override fun close() {
                super.close()
                closeCurrentUI()
                createSession(ctx, servlet, uiFactory)
            }
        }
        session.configuration = servlet.service.deploymentConfiguration
        VaadinSession.setCurrent(session)
        strongRefSession = session

        // init Vaadin Request
        val request = VaadinServletRequest(MockRequest(httpSession), servlet.service)
        strongRefReq = request
        CurrentInstance.set(VaadinRequest::class.java, request)

        // create UI
        createUI(uiFactory, session, request)
    }

    private fun createUI(uiFactory: () -> UI, session: VaadinSession, request: VaadinServletRequest) {
        val ui = uiFactory()
        // hook into Page.reload() and recreate the UI
        UI::class.java.getDeclaredField("page").apply {
            isAccessible = true
            set(ui, object : Page(ui) {
                override fun reload() {
                    super.reload()
                    closeCurrentUI()
                    createUI(uiFactory, session, request)
                }
            })
        }
        ui.internals.session = session
        UI.setCurrent(ui)
        ui.doInit(request, -1)
        strongRefUI = ui

        // navigate to the initial page
        if (lastNavigation != null) {
            UI.getCurrent().router.navigate(UI.getCurrent(), lastNavigation!!, NavigationTrigger.PROGRAMMATIC)
            lastNavigation = null
        } else {
            UI.getCurrent().navigate("")
        }
    }
}

/**
 * We need to use a MockedUI, with [beforeClientResponse] overridden, otherwise opened dialogs will never appear in the UI.
 */
// opened to be extensible in user's library
open class MockedUI : UI() {
    override fun beforeClientResponse(component: Component, execution: SerializableConsumer<ExecutionContext>): StateTree.ExecutionRegistration {
        // run this execution immediately, otherwise the dialog is not really opened. This is because the dialog does not open immediately,
        // instead it slates itself to be opened via this method.
        execution.accept(ExecutionContext(this, false))
        return object : StateTree.ExecutionRegistration {
            override fun remove() {
                // no-op, cannot be canceled since it already ran
            }
        }
    }
}

// opened to be extensible in user's library
open class MockService(servlet: VaadinServlet, deploymentConfiguration: DeploymentConfiguration, private val registry: RouteRegistry) : VaadinServletService(servlet, deploymentConfiguration) {
    override fun isAtmosphereAvailable(): Boolean = false
    override fun getRouteRegistry(): RouteRegistry = registry
    override fun getMainDivId(session: VaadinSession?, request: VaadinRequest?): String = "ROOT-1"
}