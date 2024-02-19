package com.github.mvysny.kaributesting.mockhttp

import java.util.*
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletResponse

public open class MockServletConfig(public val context: ServletContext) : ServletConfig {

    /**
     * Per-servlet init parameters.
     */
    public var servletInitParams: MutableMap<String, String> = mutableMapOf<String, String>()

    override fun getInitParameter(name: String): String? = servletInitParams[name]

    override fun getInitParameterNames(): Enumeration<String> = Collections.enumeration(servletInitParams.keys)

    override fun getServletName(): String = "Vaadin Servlet"

    override fun getServletContext(): ServletContext = context
}

internal fun <K, V> MutableMap<K, V>.putOrRemove(key: K, value: V?) {
    if (value == null) remove(key) else set(key, value)
}

public object MockHttpEnvironment {
    /**
     * [MockRequest.getLocalPort]
     */
    public var localPort: Int = 8080

    /**
     * [MockRequest.getServerPort]
     */
    public var serverPort: Int = 8080

    /**
     * [MockRequest.getRemotePort]
     */
    public var remotePort: Int = 8080

    /**
     * [MockRequest.getRemoteAddr]
     */
    public var remoteAddr: String = "127.0.0.1"

    /**
     * [MockRequest.getAuthType]
     */
    public var authType: String? = null

    /**
     * [MockRequest.isSecure]
     */
    public var isSecure: Boolean = false

    /**
     * [MockRequest.authenticate]
     */
    public var authenticator: (HttpServletResponse) -> Boolean = { throw UnsupportedOperationException("not implemented. Set MockHttpEnvironment.authenticator to override") }

    /**
     * According to the servlet spec, HttpSession functions should fail on an invalidated session.
     * However, apparently the servlet containers do not take this check too seriously since existing Spring Security servlet-related
     * code apparently works and is happily reading and writing http session attributes on an invalidated session.
     *
     * Therefore this setting defaults to false which disables all session invalidity checks. Turn this
     * to true, to make [MockHttpSession] behave according to the spec.
     *
     * See [MockHttpSession.getAttribute] implementation for more details.
     */
    public var strictSessionValidityChecks: Boolean = false
}
