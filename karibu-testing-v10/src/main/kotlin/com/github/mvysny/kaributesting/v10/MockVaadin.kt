package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.mockhttp.*
import com.github.mvysny.kaributesting.v10.mock.*
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.internal.CurrentInstance
import com.vaadin.flow.internal.StateTree
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.server.*
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.ServletContext
import kotlin.test.expect

public object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    // use ThreadLocals so that multiple threads may initialize fresh Vaadin instances at the same time.
    private val strongRefSession = ThreadLocal<VaadinSession>()
    private val strongRefUI = ThreadLocal<UI>()
    private val strongRefReq = ThreadLocal<VaadinRequest>()
    private val strongRefRes = ThreadLocal<VaadinResponse>()
    private val lastNavigation = ThreadLocal<Location>()

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
     *
     * Sometimes you wish to provide a specific [VaadinServletService],
     * e.g. to override
     * [VaadinServletService.loadInstantiators] and provide your own way of instantiating Views, e.g. via Spring or Guice.
     * Please do that by extending [MockVaadinServlet] and overriding [MockVaadinServlet.createServletService]
     * `createServletService(DeploymentConfiguration)`.
     * Please consult [MockService] on what methods you must override in your custom service.
     * @param routes all classes annotated with [com.vaadin.flow.router.Route]; use [Routes.autoDiscoverViews] to auto-discover all such classes.
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class.
     */
    @JvmStatic
    @JvmOverloads
    public fun setup(routes: Routes = Routes(),
              uiFactory: () -> UI = { MockedUI() }) {
        // init servlet
        val servlet = MockVaadinServlet(routes)
        setup(uiFactory, servlet)
    }

    /**
     * Use this method when you need to provide a completely custom servlet (e.g. `SpringServlet`). Do not forget to create a specialized service
     * which works in mocked environment. See below for details on how to do this.
     *
     * The UI factory *must* provide a new, fresh instance of the UI, so that the
     * tests start from a pre-known state. If you're using Spring and you're getting UI
     * from the injector, you must reconfigure Spring to use prototype scope,
     * otherwise an old UI from the UI scope or Session Scope will be provided.
     * @param uiFactory produces [UI] instances and sets them as current, by default
     * simply instantiates [MockedUI] class.
     * @param servlet allows you to provide your own implementation of [VaadinServlet].
     * You MUST override [VaadinServlet.createServletService]
     * and construct a custom service which overrides important methods.
     * Please consult [com.github.mvysny.kaributesting.v10.mock.MockService]
     * on what methods you must override in your custom service.
     */
    @JvmStatic
    public fun setup(uiFactory: () -> UI = { MockedUI() }, servlet: VaadinServlet) {
        check(VaadinMeta.fullVersion >= SemanticVersion.VAADIN_14_3_0) {
            "Karibu-Testing only works with Vaadin ${SemanticVersion.VAADIN_14_3_0} but you're using ${VaadinMeta.fullVersion}"
        }
        check(!VaadinMeta.isCompatibilityMode)

        if (!servlet.isInitialized) {
            val ctx: ServletContext = MockVaadinHelper.createMockContext()
            servlet.init(MockServletConfig(ctx))
        }
        val service: VaadinServletService = checkNotNull(servlet.service)
        expect(true, "$servlet failed to call VaadinServletService.init() in createServletService()") {
            service.router != null
        }
        VaadinService.setCurrent(service)

        // init Vaadin Session
        createSession(servlet.servletContext, uiFactory)
    }

    /**
     * Properly closes the current UI and fire the detach event on it.
     * Does nothing if there is no current UI.
     */
    public fun closeCurrentUI() {
        val ui: UI = UI.getCurrent() ?: return
        lastNavigation.set(ui.internals.activeViewLocation)
        if (ui.isClosing && ui.internals.session != null) {
            ui._close()
        }
        ComponentUtil.onComponentDetach(ui)
        UI.setCurrent(null)
        strongRefUI.remove()
    }

    /**
     * Cleans up and removes the Vaadin UI and Vaadin Session. You can call this function in `afterEach{}` block,
     * to clean up after the test. This comes handy when you want to be extra-sure that the next test won't accidentally reuse old UI,
     * should you forget to call [setup] properly.
     *
     * You don't have to call this function though; [setup] will overwrite any current UI/Session instances with a fresh ones.
     */
    @JvmStatic
    public fun tearDown() {
        clearVaadinInstances()
        val service: VaadinService? = VaadinService.getCurrent()
        if (service != null) {
            service.fireServiceDestroyListeners(ServiceDestroyEvent(service))
            VaadinService.setCurrent(null)
        }
        lastNavigation.remove()
    }

    private fun clearVaadinInstances() {
        closeCurrentUI()
        closeCurrentSession()
        CurrentInstance.set(VaadinRequest::class.java, null)
        CurrentInstance.set(VaadinResponse::class.java, null)
        strongRefReq.remove()
        strongRefRes.remove()
    }

    private fun closeCurrentSession() {
        val session: VaadinSession? = VaadinSession.getCurrent()
        if (session != null) {
            val service: VaadinService = VaadinService.getCurrent()
            service.fireSessionDestroy(session)
            VaadinSession.setCurrent(null)
            // service destroys session via session.access(); we need to run that action now.
            currentlyClosingSession.set(true)
            runUIQueue(session = session)
            currentlyClosingSession.set(false)
        }
        strongRefSession.remove()
    }

    private val currentlyClosingSession: ThreadLocal<Boolean> = object : ThreadLocal<Boolean>() {
        override fun initialValue(): Boolean = false
    }

    /**
     * Change & call [setup] to set a different browser.
     *
     * The default is Firefox 71 on Ubuntu Linux.
     */
    public var userAgent: String = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:71.0) Gecko/20100101 Firefox/71.0"

    private fun createSession(ctx: ServletContext, uiFactory: () -> UI) {
        val service: VaadinServletService = checkNotNull(VaadinService.getCurrent()) as VaadinServletService
        val httpSession: MockHttpSession = MockHttpSession.create(ctx)

        // init Vaadin Request
        val mockRequest = MockRequest(httpSession)
        // so that session.browser.updateRequestDetails() also creates browserDetails
        mockRequest.headers["User-Agent"] = listOf(userAgent)
        val request = VaadinServletRequest(mockRequest, service)
        strongRefReq.set(request)
        CurrentInstance.set(VaadinRequest::class.java, request)

        // init Session.
        // Use the underlying Service to create the Vaadin Session; however
        // you MUST mock certain things in order for Karibu to work.
        // See MockSession for more details. By default the service is a MockService
        // which creates MockSession.
        val mcreateVaadinSession: Method = VaadinService::class.java.getDeclaredMethod("createVaadinSession", VaadinRequest::class.java)
        mcreateVaadinSession.isAccessible = true
        val session: VaadinSession = mcreateVaadinSession.invoke(service, checkNotNull(VaadinRequest.getCurrent())) as VaadinSession
        httpSession.setAttribute(service.serviceName + ".lock", ReentrantLock().apply { lock() })
        session.refreshTransients(WrappedHttpSession(httpSession), service)
        expect(true, "$session created from $service has null lock. See the MockSession class on how to mock locks properly") { session.lockInstance != null }
        expect(true, "$session created from $service: lock must be locked!") { (session.lockInstance as ReentrantLock).isLocked }
        session.configuration = service.deploymentConfiguration

        VaadinSession.setCurrent(session)
        strongRefSession.set(session)
        if (VaadinMeta.version < 19) {
            session.browser.updateRequestDetails(request)
        } else {
            val m = VaadinSession::class.java.getDeclaredMethod("setBrowser", WebBrowser::class.java)
            val b = WebBrowser::class.java.getDeclaredConstructor(VaadinRequest::class.java).apply { isAccessible = true } .newInstance(request)
            m.invoke(session, b)
        }
        checkNotNull(session.browser.browserApplication) { "The WebBrowser has not been mocked properly" }

        // init Vaadin Response
        val response = VaadinServletResponse(MockResponse(), service)
        strongRefRes.set(response)
        CurrentInstance.set(VaadinResponse::class.java, response)

        // fire session init listeners
        service.fireSessionInitListeners(SessionInitEvent(service, session, request))

        // create UI
        createUI(uiFactory, session)
    }

    internal fun createUI(uiFactory: () -> UI, session: VaadinSession) {
        val request: VaadinRequest = checkNotNull(VaadinRequest.getCurrent())
        val ui: UI = uiFactory()
        require(ui.session == null) {
            "uiFactory produced UI $ui which is already attached to a Session, " +
                    "yet we expect the UI to be a fresh new instance, not yet attached to a Session, so that the tests" +
                    " are able to always start with a fresh UI with a pre-known state. Perhaps you're " +
                    "using Spring which reuses a scoped instance of the UI?"
        }

        // hook into Page.reload() and recreate the UI
        UI::class.java.getDeclaredField("page").apply {
            isAccessible = true
            set(ui, MockPage(ui, uiFactory, session))
        }
        ui.internals.session = session
        UI.setCurrent(ui)
        ui.doInit(request, 1)
        strongRefUI.set(ui)

        session.addUI(ui)
        session.service.fireUIInitListeners(ui)

        // navigate to the initial page
        if (lastNavigation.get() != null) {
            UI.getCurrent().router.navigate(UI.getCurrent(), lastNavigation.get()!!, NavigationTrigger.PROGRAMMATIC)
            lastNavigation.remove()
        } else {
            if (UI.getCurrent().router.registry.getNavigationTarget("").isPresent) {
                UI.getCurrent().navigate("")
            }
        }
    }

    /**
     * Since Karibu-Testing runs in the same JVM as the server and there is no browser, the boundaries between the client and
     * the server become unclear. When looking into sources of any test method, it's really hard to tell where exactly the server request ends, and
     * where another request starts.
     *
     * You can establish an explicit client boundary in your test, by explicitly calling this method. However, since that
     * would be both laborous and error-prone, the default operation is that Karibu Testing pretends as if there was a client-server
     * roundtrip before every component lookup
     * via the [_get]/[_find]/[_expectNone]/[_expectOne] call. See [TestingLifecycleHook] for more details.
     *
     * Calls the following:
     * * [runUIQueue]
     * * [StateTree.runExecutionsBeforeClientResponse] which runs all blocks scheduled via [UI.beforeClientResponse]
     * * [cleanupDialogs]
     * @throws IllegalStateException if the environment is not mocked
     */
    public fun clientRoundtrip() {
        checkNotNull(VaadinSession.getCurrent()) { "No VaadinSession" }
        runUIQueue()
        UI.getCurrent().internals.stateTree.runExecutionsBeforeClientResponse()
        cleanupDialogs()
    }

    /**
     * Runs all tasks scheduled by [UI.access].
     *
     * If [VaadinSession.errorHandler] is not set or [propagateExceptionToHandler]
     * is false, any exceptions thrown from [Command]s scheduled via the [UI.access] will make this function fail.
     * The exceptions will be wrapped in [ExecutionException]. Generally
     * it's best to keep [propagateExceptionToHandler] set to false to
     * make any exceptions fail the test; however if you're testing
     * how your own custom [VaadinSession.errorHandler] responds to exceptions then
     * set this parameter to true.
     *
     * Called automatically by [clientRoundtrip] which is by default called automatically from [TestingLifecycleHook]. You generally
     * don't need to call this method unless you need to test your [ErrorHandler].
     *
     * @param propagateExceptionToHandler defaults to false. If true and [VaadinSession.errorHandler]
     * is set, any exceptions thrown from [Command]s scheduled via the [UI.access] will be
     * redirected to [VaadinSession.errorHandler] and will not be re-thrown from this method.
     * @throws IllegalStateException if the environment is not mocked
     */
    @JvmOverloads
    @JvmStatic
    public fun runUIQueue(propagateExceptionToHandler: Boolean = false, session: VaadinSession = VaadinSession.getCurrent()) {
        // we need to set up UI error handler which will be notified for every exception thrown out of the acccess{} block
        // otherwise the exceptions would simply be logged but unlock() wouldn't fail.
        val errors: MutableList<Throwable> = mutableListOf<Throwable>()
        val oldErrorHandler: ErrorHandler? = session.errorHandler
        if (oldErrorHandler == null || oldErrorHandler is DefaultErrorHandler || !propagateExceptionToHandler) {
            session.errorHandler = ErrorHandler {
                var t: Throwable = it.throwable
                if (t !is ExecutionException) {
                    // for some weird reason t may not be ExecutionException when it originates from a coroutine :confused:
                    // the stacktrace would point someplace random. Wrap it in ExecutionException whose stacktrace will point to the test
                    t = ExecutionException(t.message, t)
                }
                errors.add(t)
            }
        }

        try {
            session.unlock()  // this will process all Runnables registered via ui.access()
            // lock the session back, so that the test can continue running as-if in the UI thread.
            session.lock()
        } finally {
            session.errorHandler = oldErrorHandler
        }

        if (errors.isNotEmpty()) {
            errors.drop(1).forEach { errors[0].addSuppressed(it) }
            throw errors[0]
        }
    }

    /**
     * Internal function, do not call directly.
     *
     * Only usable when you are providing your own implementation of [VaadinSession].
     * See [MockVaadinSession] on how to call this properly.
     */
    @JvmStatic
    public fun afterSessionClose(session: VaadinSession, uiFactory: () -> UI) {
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

        if (!currentlyClosingSession.get()) {
            clearVaadinInstances()
            session.mock.destroy()
            createSession(session.mock.servletContext, uiFactory)
        }
    }
}

private fun VaadinService.fireSessionInitListeners(event: SessionInitEvent) {
    val listenerField: Field = VaadinService::class.java.getDeclaredField("sessionInitListeners")
    listenerField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val sessionInitListeners: Collection<SessionInitListener> = listenerField.get(this) as Collection<SessionInitListener>
    for (sessionInitListener in sessionInitListeners) {
        sessionInitListener.sessionInit(event)
    }
}

private fun VaadinService.fireServiceDestroyListeners(event: ServiceDestroyEvent) {
    val listenerField: Field = VaadinService::class.java.getDeclaredField("serviceDestroyListeners")
    listenerField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val listeners: Collection<ServiceDestroyListener> = listenerField.get(this) as Collection<ServiceDestroyListener>
    for (listener in listeners) {
        listener.serviceDestroy(event)
    }
}

private class MockPage(ui: UI, private val uiFactory: () -> UI, private val session: VaadinSession) : Page(ui) {
    override fun reload() {
        // recreate the UI on reload(), to simulate browser's F5
        super.reload()
        MockVaadin.closeCurrentUI()
        MockVaadin.createUI(uiFactory, session)
    }
}
