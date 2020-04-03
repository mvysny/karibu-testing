package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.mockhttp.*
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.component.polymertemplate.TemplateParser
import com.vaadin.flow.di.Instantiator
import com.vaadin.flow.function.DeploymentConfiguration
import com.vaadin.flow.internal.CurrentInstance
import com.vaadin.flow.internal.StateTree
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.server.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

private class MockPage(ui: UI, private val uiFactory: () -> UI, private val session: VaadinSession) : Page(ui) {
    override fun reload() {
        // recreate the UI on reload(), to simulate browser's F5
        super.reload()
        MockVaadin.closeCurrentUI()
        MockVaadin.createUI(uiFactory, session)
    }
}

private class MockVaadinSession(service: VaadinService,
                                val httpSession: MockHttpSession,
                                val uiFactory: () -> UI) : VaadinSession(service) {
    /**
     * We need to pretend that we have the UI lock during the duration of the test method, otherwise
     * Vaadin would complain that there is no session lock.
     * The easiest way is to simply always provide a locked lock :)
     */
    private val lock: ReentrantLock = ReentrantLock().apply { lock() }

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

        MockVaadin.clearVaadinInstances()
        httpSession.destroy()
        MockVaadin.createSession(httpSession.servletContext, uiFactory)
    }
}

private class MockVaadinServlet(val routes: Routes,
                          val serviceFactory: (VaadinServlet, DeploymentConfiguration) -> VaadinServletService) : VaadinServlet() {

    override fun createDeploymentConfiguration(): DeploymentConfiguration {
        // we need to skip the test at DeploymentConfigurationFactory.verifyMode otherwise
        // testing a Vaadin 15 component module in npm mode without webpack.config.js nor flow-build-info.json would fail.
        if (VaadinMeta.flowBuildInfo == null) {
            // probably inside a Vaadin 15 component module. create a dummy token file so that
            // DeploymentConfigurationFactory.verifyMode() is happy.
            val tokenFile: File = File.createTempFile("flow-build-info", "json")
            tokenFile.writeText("{}")
            servletContext.setInitParameter("vaadin.frontend.token.file", tokenFile.absolutePath)
        }
        return super.createDeploymentConfiguration()
    }

    override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
        val service: VaadinServletService = serviceFactory(this, deploymentConfiguration)
        service.init()
        routes.register(service.context as VaadinServletContext)
        return service
    }
}

object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    private var strongRefSession: VaadinSession? = null
    private var strongRefUI: UI? = null
    private var strongRefReq: VaadinRequest? = null
    private var strongRefRes: VaadinResponse? = null
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
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class.
     * @param serviceFactory allows you to provide your own implementation of [VaadinServletService] which allows you to e.g. override
     * [VaadinServletService.loadInstantiators] and provide your own way of instantiating Views, e.g. via Spring or Guice.
     * Please consult [MockService] on what methods you must override in your custom service.
     */
    @JvmStatic
    @JvmOverloads
    fun setup(routes: Routes = Routes(),
              uiFactory: () -> UI = { MockedUI() },
              serviceFactory: (VaadinServlet, DeploymentConfiguration) -> VaadinServletService =
                      { servlet, dc -> MockService(servlet, dc) }) {
        // init servlet
        val servlet = MockVaadinServlet(routes, serviceFactory)
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
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class.
     * @param servlet allows you to provide your own implementation of [VaadinServlet]. You MUST override [VaadinServlet.createServletService]
     * and construct a custom service which overrides important methods. Please consult [MockService] on what methods you must override in your custom service.
     */
    @JvmStatic
    fun setup(uiFactory: () -> UI = { MockedUI() }, servlet: VaadinServlet) {
        check(VaadinMeta.version >= 15) { "Karibu-Testing only works with Vaadin 15+ but you're using ${VaadinMeta.version}" }

        val ctx = MockContext()
        servlet.init(MockServletConfig(ctx))
        VaadinService.setCurrent(servlet.service!!)

        // init Vaadin Session
        createSession(ctx, uiFactory)
    }

    /**
     * One more overloaded setup() for use in Java and Groovy
     */
    @JvmStatic
    fun setup(routes: Routes = Routes(),
              serviceFactory: (VaadinServlet, DeploymentConfiguration) -> VaadinServletService = defaultServiceFactory()) =
            setup(routes = routes, uiFactory = { MockedUI() }, serviceFactory = serviceFactory)

    private fun defaultServiceFactory() = { servlet: VaadinServlet, dc: DeploymentConfiguration ->
        MockService(servlet, dc)
    }

    internal fun closeCurrentUI() {
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
        VaadinService.setCurrent(null)
        lastNavigation = null
    }

    internal fun clearVaadinInstances() {
        closeCurrentUI()
        closeCurrentSession()
        CurrentInstance.set(VaadinRequest::class.java, null)
        CurrentInstance.set(VaadinResponse::class.java, null)
        strongRefReq = null
        strongRefRes = null
    }

    private fun closeCurrentSession() {
        VaadinSession.setCurrent(null)
        strongRefSession = null
    }

    /**
     * Change & call [setup] to set a different browser.
     *
     * The default is Firefox 71 on Ubuntu Linux.
     */
    var userAgent: String = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:71.0) Gecko/20100101 Firefox/71.0"

    internal fun createSession(ctx: ServletContext, uiFactory: () -> UI) {
        val service: VaadinServletService = checkNotNull(VaadinService.getCurrent()) as VaadinServletService
        val httpSession: MockHttpSession = MockHttpSession.create(ctx)

        val session = MockVaadinSession(service, httpSession, uiFactory)
        httpSession.setAttribute(service.serviceName + ".lock", session.lockInstance)
        session.configuration = service.deploymentConfiguration
        session.refreshTransients(WrappedHttpSession(httpSession), service)
        VaadinSession.setCurrent(session)
        strongRefSession = session

        // init Vaadin Request
        val mockRequest = MockRequest(httpSession)
        // so that session.browser.updateRequestDetails() also creates browserDetails
        mockRequest.headers["User-Agent"] = listOf(userAgent)
        val request = VaadinServletRequest(mockRequest, service)
        strongRefReq = request
        session.browser.updateRequestDetails(request)
        CurrentInstance.set(VaadinRequest::class.java, request)

        // init Vaadin Response
        val response = VaadinServletResponse(MockResponse(httpSession), service)
        strongRefRes = response
        CurrentInstance.set(VaadinResponse::class.java, response)

        // create UI
        createUI(uiFactory, session)
    }

    internal fun createUI(uiFactory: () -> UI, session: VaadinSession) {
        val request: VaadinRequest = checkNotNull(VaadinRequest.getCurrent())
        val ui = uiFactory()
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
        strongRefUI = ui

        session.addUI(ui)
        session.service.fireUIInitListeners(ui)

        // navigate to the initial page
        if (lastNavigation != null) {
            UI.getCurrent().router.navigate(UI.getCurrent(), lastNavigation!!, NavigationTrigger.PROGRAMMATIC)
            lastNavigation = null
        } else {
            UI.getCurrent().navigate("")
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
    fun clientRoundtrip() {
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
    fun runUIQueue(propagateExceptionToHandler: Boolean = false) {
        checkNotNull(VaadinSession.getCurrent()) { "No VaadinSession" }
        VaadinSession.getCurrent()!!.apply {
            // we need to set up UI error handler which will be notified for every exception thrown out of the acccess{} block
            // otherwise the exceptions would simply be logged but unlock() wouldn't fail.
            val errors = mutableListOf<Throwable>()
            val oldErrorHandler = errorHandler
            if (oldErrorHandler == null || oldErrorHandler is DefaultErrorHandler || !propagateExceptionToHandler) {
                errorHandler = ErrorHandler {
                    var t = it.throwable
                    if (t !is ExecutionException) {
                        // for some weird reason t may not be ExecutionException when it originates from a coroutine :confused:
                        // the stacktrace would point someplace random. Wrap it in ExecutionException whose stacktrace will point to the test
                        t = ExecutionException(t.message, t)
                    }
                    errors.add(t)
                }
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
 * A simple no-op UI used by default by [MockVaadin.setup]. The class is open, in order to be extensible in user's library
 */
open class MockedUI : UI()

/**
 * A mocking service that performs three very important tasks:
 * * Overrides [isAtmosphereAvailable] to tell Vaadin that we don't have Atmosphere (otherwise Vaadin will crash)
 * * Provides some dummy value as a root ID via [getMainDivId] (otherwise the mocked servlet env will crash).
 * The class is intentionally opened, to be extensible in user's library.
 */
open class MockService(servlet: VaadinServlet, deploymentConfiguration: DeploymentConfiguration) : VaadinServletService(servlet, deploymentConfiguration) {
    override fun isAtmosphereAvailable(): Boolean = false
    override fun getMainDivId(session: VaadinSession?, request: VaadinRequest?): String = "ROOT-1"
    override fun getInstantiator(): Instantiator = MockInstantiator(super.getInstantiator())
}

/**
 * Makes sure to load [MockNpmTemplateParser].
 */
open class MockInstantiator(val delegate: Instantiator) : Instantiator by delegate {
    override fun getTemplateParser(): TemplateParser = MockNpmTemplateParser()
}

val currentRequest: VaadinRequest
    get() = VaadinService.getCurrentRequest()
            ?: throw IllegalStateException("No current request")
val currentResponse: VaadinResponse
    get() = VaadinService.getCurrentResponse()
            ?: throw IllegalStateException("No current response")

/**
 * Retrieves the mock request which backs up [VaadinRequest].
 * ```
 * currentRequest.mock.addCookie(Cookie("foo", "bar"))
 * ```
 */
val VaadinRequest.mock: MockRequest get() = (this as VaadinServletRequest).request as MockRequest

/**
 * Retrieves the mock request which backs up [VaadinResponse].
 * ```
 * currentResponse.mock.getCookie("foo").value
 * ```
 */
val VaadinResponse.mock: MockResponse get() = (this as VaadinServletResponse).response as MockResponse

/**
 * Retrieves the mock session which backs up [VaadinSession].
 * ```
 * VaadinSession.getCurrent().mock
 * ```
 */
val VaadinSession.mock: MockHttpSession get() = (session as WrappedHttpSession).httpSession as MockHttpSession
