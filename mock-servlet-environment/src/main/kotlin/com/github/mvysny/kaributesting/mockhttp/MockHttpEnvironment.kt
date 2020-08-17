package com.github.mvysny.kaributesting.mockhttp

import java.security.Principal
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

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
     * [HttpServletRequest.getLocalPort]
     */
    public var localPort: Int = 8080

    /**
     * [HttpServletRequest.getServerPort]
     */
    public var serverPort: Int = 8080

    /**
     * [HttpServletRequest.getUserPrincipal]
     */
    public var userPrincipal: Principal? = null

    /**
     * [HttpServletRequest.getRemotePort]
     */
    public var remotePort: Int = 8080
}
