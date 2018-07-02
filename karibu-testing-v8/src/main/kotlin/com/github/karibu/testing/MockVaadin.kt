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
     * @param uiFactory called once from this method, to provide instance of your app's UI. By default it returns [MockUI].
     * A basic Vaadin environment is prepared before calling this factory, in order to be safe to instantiate your UI.
     * To instantiate your UI just call your UI constructor, for example `YourUI()`
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

    private fun createSession(httpSession: MockHttpSession, uiFactory: () -> UI) {
        val service = checkNotNull(VaadinService.getCurrent()) as VaadinServletService
        val session = object : VaadinSession(service) {
            override fun close() {
                super.close()
                closeCurrentUI()
                VaadinSession.setCurrent(null)
                strongRefSession = null
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
