package com.github.karibu.testing.v10

import com.github.karibu.mockhttp.MockContext
import com.github.karibu.mockhttp.MockHttpSession
import com.github.karibu.mockhttp.MockRequest
import com.github.karibu.mockhttp.MockServletConfig
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
import java.util.concurrent.ExecutionException
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
     *
     * The UI factory *must* provide a new, fresh instance of the UI, so that the
     * tests start from a pre-known state. If you're using Spring and you're getting UI
     * from the injector, you must reconfigure Spring to use prototype scope,
     * otherwise an old UI from the UI scope or Session Scope will be provided.
     * @param routes all classes annotated with [com.vaadin.flow.router.Route]; use [Routes.autoDiscoverViews] to auto-discover all such classes.
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class. If you decide to
     * provide a different value, override [UI.beforeClientResponse] so that your dialogs are opened properly with this mocked testing - see
     * [MockedUI.beforeClientResponse] for details.
     * @param serviceFactory allows you to provide your own implementation of [VaadinServletService] which allows you to e.g. override
     * [VaadinServletService.loadInstantiators] and provide your own way of instantiating Views, e.g. via Spring or Guice.
     */
    @JvmStatic @JvmOverloads
    fun setup(routes: Routes = Routes(),
              uiFactory: () -> UI = { MockedUI() },
              serviceFactory: (VaadinServlet, DeploymentConfiguration, RouteRegistry) -> VaadinServletService =
                                               { servlet, dc, reg -> MockService(servlet, dc, reg) }) {
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
    @JvmStatic
    fun setup(routes: Routes = Routes(),
                         serviceFactory: (VaadinServlet, DeploymentConfiguration, RouteRegistry) -> VaadinServletService = defaultServiceFactory()) =
            setup(routes = routes, uiFactory = { MockedUI() }, serviceFactory = serviceFactory)

    private fun defaultServiceFactory() = { servlet: VaadinServlet, dc: DeploymentConfiguration, reg: RouteRegistry ->
        MockService(servlet, dc, reg) }

    private fun closeCurrentUI() {
        val ui: UI = UI.getCurrent() ?: return
        lastNavigation = ui.internals.activeViewLocation
        ui.close()
        ui._fireEvent(DetachEvent(ui))
        UI.setCurrent(null)
        strongRefUI = null
    }

    /**
     * Cleans up and removes the Vaadin UI and Vaadin Session. You can call this function in `afterEach{}` block,
     * to clean up after the test. This comes handy when you want to be extra-sure that the next test won't accidentally reuse old UI,
     * should you forget to call [setup] properly.
     *
     * You don't have to call this function though; [setup] will overwrite any current UI/Session instances with a fresh ones.
     */
    @JvmStatic
    fun tearDown() {
        clearVaadinInstances()
        lastNavigation = null
    }

    private fun clearVaadinInstances() {
        closeCurrentUI()
        closeCurrentSession()
        CurrentInstance.set(VaadinRequest::class.java, null)
        strongRefReq = null
        VaadinService.setCurrent(null)
    }

    private fun closeCurrentSession() {
        VaadinSession.setCurrent(null)
        strongRefSession = null
    }

    private fun createSession(ctx: MockContext, servlet: VaadinServlet, uiFactory: ()->UI) {
        val httpSession = MockHttpSession.create(ctx)

        val session = object : VaadinSession(servlet.service) {
            /**
             * We need to pretend that we have the UI lock during the duration of the test method, otherwise
             * Vaadin would complain that there is no session lock.
             * The easiest way is to simply always provide a locked lock :)
             */
            private val lock = ReentrantLock().apply { lock() }
            override fun getLockInstance(): Lock = lock
            override fun close() {
                super.close()

                // We need to simulate the actual browser + servlet container behavior here.
                // Imagine that we want a test scenario where the user logs out, and we want to check that a login prompt appears.

                // To log out the user, the code typically closes the session and tells the browser to reload
                // the page (Page.getCurrent().reload() or similar).
                // Thus the page is reloaded by the browser, and since the session is gone, the servlet container
                // will create a new, fresh session.

                // That's exactly what we need to do here. We need to close the current UI and eradicate it,
                // then we need to close the current session and eradicate it, and then we need to create a completely fresh
                // new UI and Session.

                // A problem appears when the uiFactory accidentally doesn't create a new, fresh instance of UI. Say that
                // we call Spring injector to provide us an instance of the UI, but we accidentally scoped the UI to Session.
                // Spring doesn't know that (since we haven't told Spring that the Session scope is gone) and provides
                // the previous UI instance which is still attached to the session. And it blows.

                clearVaadinInstances()
                httpSession.destroy()
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
        require(ui.session == null) { "uiFactory produced UI $ui which is already attached to a Session, " +
                "yet we expect the UI to be a fresh new instance, not yet attached to a Session, so that the tests" +
                " are able to always start with a fresh UI with a pre-known state. Perhaps you're " +
                "using Spring which reuses a scoped instance of the UI?" }

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

    /**
     * Runs all tasks scheduled by [UI.access].
     *
     * If [VaadinSession.errorHandler] is not set or [propagateExceptionToHandler] is false, any exceptions thrown from Commands taken by [UI.access] will make this function fail.
     * The exceptions will be wrapped in [ExecutionException].
     */
    fun runUIQueue(propagateExceptionToHandler: Boolean = false) {
        VaadinSession.getCurrent()!!.apply {
            // we need to set up UI error handler which will be notified for every exception thrown out of the acccess{} block
            // otherwise the exceptions would simply be logged but unlock() wouldn't fail.
            val errors = mutableListOf<Throwable>()
            val oldErrorHandler = errorHandler
            if (oldErrorHandler == null || oldErrorHandler is DefaultErrorHandler || !propagateExceptionToHandler) {
                errorHandler = ErrorHandler { errors.add(it.throwable) }
            }

            try {
                unlock()  // this will process all Runnables registered via ui.access()
                // lock the session back, so that the test can continue running as-if in the UI thread.
                lock()
            } finally {
                errorHandler = oldErrorHandler
            }

            if (!errors.isEmpty()) {
                errors.drop(1).forEach { errors[0].addSuppressed(it) }
                throw errors[0]
            }
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