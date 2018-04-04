package com.github.karibu.testing

import com.github.karibu.mockhttp.MockContext
import com.github.karibu.mockhttp.MockHttpSession
import com.github.karibu.mockhttp.MockRequest
import com.github.karibu.mockhttp.MockServletConfig
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.function.SerializableConsumer
import com.vaadin.flow.internal.CurrentInstance
import com.vaadin.flow.internal.ExecutionContext
import com.vaadin.flow.internal.StateTree
import com.vaadin.flow.server.*
import com.vaadin.flow.server.startup.RouteRegistry
import com.vaadin.flow.shared.VaadinUriResolver
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    private val strongRefSession = ThreadLocal<VaadinSession>()
    private val strongRefUI = ThreadLocal<UI>()
    private val strongRefReq = ThreadLocal<VaadinRequest>()

    /**
     * Mocks Vaadin for the current test method.
     * @param routes all classes annotated with [com.vaadin.flow.router.Route]; use [autoDiscoverViews] to auto-discover all such classes.
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class. If you decide to
     * provide a different value, override [UI.beforeClientResponse] so that your dialogs are opened properly with this mocked testing.
     */
    fun setup(routes: Set<Class<out Component>> = setOf(), uiFactory: ()->UI = { MockedUI() }) {
        // init servlet
        val servlet = VaadinServlet()
        val ctx = MockContext()
        servlet.init(MockServletConfig(ctx))
        val httpSession = MockHttpSession.create(ctx)

        // init VaadinService
        val service = object : VaadinServletService(servlet, DefaultDeploymentConfiguration(MockVaadin::class.java, Properties(), { _, _ -> })) {
            private val registry = object : RouteRegistry() {
                init {
                    setNavigationTargets(routes)
                }
            }
            override fun isAtmosphereAvailable(): Boolean = false
            override fun getRouteRegistry(): RouteRegistry = registry
            override fun getMainDivId(session: VaadinSession?, request: VaadinRequest?): String = "ROOT-1"
        }
        service.init()
        VaadinService.setCurrent(service)

        // init Vaadin Session
        val session = object : VaadinSession(service) {
            private val lock = ReentrantLock().apply { lock() }
            override fun getLockInstance(): Lock = lock
        }
        VaadinSession.setCurrent(session)
        strongRefSession.set(session)
        session.setAttribute(VaadinUriResolverFactory::class.java, MockResolverFactory)

        // init Vaadin Request
        val request = VaadinServletRequest(MockRequest(httpSession), service)
        strongRefReq.set(request)
        CurrentInstance.set(VaadinRequest::class.java, request)

        val ui = uiFactory()
        ui.internals.session = session
        UI.setCurrent(ui)
        ui.doInit(request, -1)
        strongRefUI.set(ui)
    }
}

/**
 * We need to use a MockedUI, with [beforeClientResponse] overridden, otherwise opened dialogs will never appear in the UI.
 */
class MockedUI : UI() {
    override fun beforeClientResponse(component: Component, execution: SerializableConsumer<ExecutionContext>): StateTree.ExecutionRegistration {
        if (component is Dialog && component.isOpened) {
            component.addOpenedChangeListener {
                // not currently fired by Flow.
                if (!component.isOpened) {
                    component.element.removeFromParent()  // cleanup closed dialogs from the UI
                }
            }
        }
        execution.accept(ExecutionContext(this, false))
        return object : StateTree.ExecutionRegistration {
            override fun remove() {
                // no-op, cannot be canceled since it already ran
            }
        }
    }
}

object MockResolverFactory : VaadinUriResolverFactory {
    override fun getUriResolver(request: VaadinRequest): VaadinUriResolver = MockUriResolver
}

object MockUriResolver : VaadinUriResolver() {
    override fun getFrontendRootUrl(): String = ""
    override fun getContextRootUrl(): String = ""
}
