package com.github.mvysny.kaributesting.v8

import com.github.mvysny.kaributesting.mockhttp.*
import com.vaadin.server.*
import com.vaadin.shared.Version
import com.vaadin.shared.ui.ui.PageClientRpc
import com.vaadin.ui.UI
import com.vaadin.util.CurrentInstance
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantLock
import javax.servlet.ServletContext

private class MockVaadinService(servlet: VaadinServlet, deploymentConfiguration: DeploymentConfiguration) : VaadinServletService(servlet, deploymentConfiguration) {
    override fun isAtmosphereAvailable() = false
}

private class MockVaadinServlet : VaadinServlet() {
    override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
        val service = MockVaadinService(this, deploymentConfiguration)
        service.init()
        return service
    }

    public override fun getService(): VaadinServletService =
            super.getService()
}

private class MockVaadinSession(val httpSession: MockHttpSession,
                                service: VaadinService,
                                private val uiFactory: () -> UI) : VaadinSession(service) {
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

        MockVaadin.clearVaadinObjects()
        httpSession.destroy()
        MockVaadin.createSession(httpSession, uiFactory)
    }
}

object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    private var strongRefSession: VaadinSession? = null
    private var strongRefUI: UI? = null
    private var strongRefRequest: VaadinRequest? = null
    private var strongRefResponse: VaadinResponse? = null
    private var lastLocation: String? = null
    val log = LoggerFactory.getLogger(MockVaadin::class.java)
    /**
     * Creates new mock session and UI for a test. Just call this before all and every of your UI tests are ran.
     *
     * The UI factory *must* provide a new, fresh instance of the UI, so that the
     * tests start from a pre-known state. If you're using Spring and you're getting UI
     * from the injector, you must reconfigure Spring to use prototype scope,
     * otherwise an old UI from the UI scope or Session Scope will be provided.
     * @param uiFactory called once from this method, to provide instance of your app's UI. By default it returns [MockUI].
     * A basic Vaadin environment is prepared before calling this factory, in order to be safe to instantiate your UI.
     * To instantiate your UI just call your UI constructor, for example `YourUI()`.
     * @param httpSession allow you to use custom http session. Useful for MPR where Vaadin 8 + Vaadin 10 share one session
     * @param servletContext the servlet context to use
     */
    @JvmStatic
    @JvmOverloads
    fun setup(uiFactory: () -> UI = { MockUI() }, httpSession: MockHttpSession? = null, servletContext: ServletContext = MockContext()) {
        // prepare mocking servlet environment
        val servlet = MockVaadinServlet()
        servlet.init(MockServletConfig(servletContext))
        val httpSess: MockHttpSession = httpSession
                ?: MockHttpSession.create(servletContext)

        // mock Vaadin environment: Service
        VaadinService.setCurrent(servlet.service)

        // Session
        createSession(httpSess, uiFactory)
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
        clearVaadinObjects()
        VaadinService.setCurrent(null)
        lastLocation = null
    }

    internal fun clearVaadinObjects() {
        closeCurrentUI()
        closeCurrentSession()
        CurrentInstance.set(VaadinRequest::class.java, null)
        CurrentInstance.set(VaadinResponse::class.java, null)
        strongRefRequest = null
        strongRefResponse = null
    }

    private fun closeCurrentSession() {
        VaadinSession.setCurrent(null)
        strongRefSession = null
    }

    internal fun createSession(httpSession: MockHttpSession, uiFactory: () -> UI) {
        val service: VaadinServletService = checkNotNull(VaadinService.getCurrent()) as VaadinServletService
        val session = MockVaadinSession(httpSession, service, uiFactory)

        httpSession.setAttribute(service.serviceName + ".lock", ReentrantLock().apply { lock() })
        session.refreshTransients(WrappedHttpSession(httpSession), service)
        session.configuration = DefaultDeploymentConfiguration(service.servlet.javaClass, Properties())
        VaadinSession.setCurrent(session)
        strongRefSession = session

        // request
        val httpRequest = MockRequest(httpSession)
        httpRequest.setParameter("v-loc", "http://localhost:8080")
        strongRefRequest = VaadinServletRequest(httpRequest, service)
        CurrentInstance.set(VaadinRequest::class.java, strongRefRequest)

        // response
        strongRefResponse = VaadinServletResponse(MockResponse(httpSession), service)
        CurrentInstance.set(VaadinResponse::class.java, strongRefResponse)

        // UI
        createUI(uiFactory)
    }

    private fun closeCurrentUI() {
        val ui: UI = UI.getCurrent() ?: return
        lastLocation = try {
            Page.getCurrent().location.path.trim('/')
        } catch (t: Exception) {
            // incorrectly mocked Page tends to fail in Page.getLocation(). Just do nothing.
            log.warn("Failed to retrieve current location from Page, probably because of incorrectly mocked Vaadin classes", t)
            null
        }
        ui.close()
        if (ui.isAttached) {
            // incorrectly mocked UI can fail in mysterious ways in detach()
            ui.detach()
        }
        UI.setCurrent(null)
        strongRefUI = null
    }

    private fun createUI(uiFactory: () -> UI) {
        val ui = uiFactory()
        require(ui.session == null) {
            "uiFactory produced UI $ui which is already attached to a Session, " +
                    "yet we expect the UI to be a fresh new instance, not yet attached to a Session, so that the" +
                    " tests are able to always start with a fresh UI with a pre-known state. Perhaps you're " +
                    "using Spring which reuses a scoped instance of the UI?"
        }
        ui.session = checkNotNull(VaadinSession.getCurrent())
        val request = checkNotNull(CurrentInstance.get(VaadinRequest::class.java))
        strongRefUI = ui
        UI.setCurrent(ui)
        ui.page.webBrowser.updateRequestDetails(request)
        if (Version.getMinorVersion() >= 2) {
            // uiRootPath field is only present for Vaadin 8.2.x and higher.
            UI::class.java.getDeclaredField("uiRootPath").apply {
                isAccessible = true
                set(ui, "")
            }
        }
        try {
            ui.doInit(request, 1, "1")
        } catch (e: Exception) {
            if (ui.navigator != null) {
                throw RuntimeException("UI failed to initialize. If you're using autoViewProvider, make sure that views are auto-discovered via autoDiscoverViews()", e)
            } else {
                throw e
            }
        }

        // catch Page.getCurrent().reload() requests
        ui.overrideRpcProxy(PageClientRpc::class.java, object : PageClientRpc {
            override fun reload() {
                closeCurrentUI()
                createUI(uiFactory)
            }

            override fun initializeMobileHtml5DndPolyfill() {
            }
        })

        if (!lastLocation.isNullOrBlank()) {
            UI.getCurrent().navigator.navigateTo(lastLocation)
            lastLocation = null
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
     * Calls [runUIQueue].
     * @throws IllegalStateException if the environment is not mocked
     */
    fun clientRoundtrip() {
        runUIQueue()
    }

    /**
     * Runs all tasks scheduled by [UI.access].
     *
     * If [VaadinSession.errorHandler] is not set or [propagateExceptionToHandler] is false, any exceptions thrown from Commands taken by [UI.access] will make this function fail.
     * The exceptions will be wrapped in [ExecutionException].
     *
     * Called automatically by [clientRoundtrip] which is by default called automatically from [TestingLifecycleHook]. You generally
     * don't need to call this method unless you need to test your [ErrorHandler].
     * @throws IllegalStateException if the environment is not mocked
     */
    fun runUIQueue(propagateExceptionToHandler: Boolean = false) {
        checkNotNull(VaadinSession.getCurrent()) { "No VaadinSession" }
        VaadinSession.getCurrent()!!.apply {
            // we need to set up UI error handler which will be notified for every exception thrown out of the acccess{} block
            // otherwise the exceptions would simply be logged but unlock() wouldn't fail.
            val errors = mutableListOf<Throwable>()
            val oldErrorHandler = UI.getCurrent().errorHandler
            if (oldErrorHandler == null || oldErrorHandler is DefaultErrorHandler || !propagateExceptionToHandler) {
                UI.getCurrent().errorHandler = ErrorHandler {
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
                UI.getCurrent().errorHandler = oldErrorHandler
            }

            if (!errors.isEmpty()) {
                errors.drop(1).forEach { errors[0].addSuppressed(it) }
                throw errors[0]
            }
        }
    }
}

/**
 * An empty mock UI.
 */
internal class MockUI : UI() {
    override fun init(request: VaadinRequest?) {
    }
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
