package com.github.karibu.testing

import com.github.karibu.mockhttp.MockContext
import com.github.karibu.mockhttp.MockHttpSession
import com.github.karibu.mockhttp.MockRequest
import com.github.karibu.mockhttp.MockServletConfig
import com.vaadin.server.*
import com.vaadin.shared.Version
import com.vaadin.shared.ui.ui.PageClientRpc
import com.vaadin.ui.UI
import com.vaadin.util.CurrentInstance
import java.util.concurrent.locks.ReentrantLock

object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    private var strongRefSession: VaadinSession? = null
    private var strongRefUI: UI? = null
    private var lastLocation: String? = null
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
     */
    @JvmStatic @JvmOverloads
    fun setup(uiFactory: ()->UI = { MockUI() }) {
        // prepare mocking servlet environment
        val servletContext = MockContext()
        val servlet = object : VaadinServlet() {
            override fun createServletService(deploymentConfiguration: DeploymentConfiguration): VaadinServletService {
                val service = object : VaadinServletService(this, deploymentConfiguration) {
                    override fun isAtmosphereAvailable() = false
                }
                service.init()
                return service
            }
        }
        servlet.init(MockServletConfig(servletContext))
        val httpSession = MockHttpSession.create(servletContext)

        // mock Vaadin environment: Service
        val service = VaadinServlet::class.java.getDeclaredMethod("getService").run {
            isAccessible = true
            invoke(servlet) as VaadinServletService
        }
        VaadinService.setCurrent(service)

        // Session
        createSession(httpSession, uiFactory)
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
        closeCurrentUI()
        closeCurrentSession()
        lastLocation = null
    }

    private fun closeCurrentSession() {
        VaadinSession.setCurrent(null)
        strongRefSession = null
    }

    private fun createSession(httpSession: MockHttpSession, uiFactory: () -> UI) {
        val service = checkNotNull(VaadinService.getCurrent()) as VaadinServletService
        val session = object : VaadinSession(service) {
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

                closeCurrentUI()
                closeCurrentSession()
                httpSession.destroy()
                createSession(httpSession, uiFactory)
            }
        }

        httpSession.setAttribute(service.serviceName + ".lock", ReentrantLock().apply { lock() })
        session.refreshTransients(WrappedHttpSession(httpSession), service)
        VaadinSession.setCurrent(session)
        strongRefSession = session

        // request
        val httpRequest = MockRequest(httpSession)
        val request = VaadinServletRequest(httpRequest, service)
        httpRequest.setParameter("v-loc", "http://localhost:8080")
        CurrentInstance.set(VaadinRequest::class.java, request)

        // UI
        createUI(uiFactory)
    }

    private fun closeCurrentUI() {
        val ui: UI = UI.getCurrent() ?: return
        lastLocation = Page.getCurrent().location.path.trim('/')
        ui.close()
        ui.detach()
        UI.setCurrent(null)
        strongRefUI = null
    }

    private fun createUI(uiFactory: ()->UI) {
        val ui = uiFactory()
        require(ui.session == null) { "uiFactory produced UI $ui which is already attached to a Session, " +
                "yet we expect the UI to be a fresh new instance, not yet attached to a Session, so that the" +
                " tests are able to always start with a fresh UI with a pre-known state. Perhaps you're " +
                "using Spring which reuses a scoped instance of the UI?" }
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
}

/**
 * An empty mock UI.
 */
internal class MockUI : UI() {
    override fun init(request: VaadinRequest?) {
    }
}
