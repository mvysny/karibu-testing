package com.github.mvysny.kaributesting.mockhttp

import java.io.BufferedReader
import java.security.Principal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.*
import javax.servlet.http.*

public open class MockRequest(private var session: HttpSession) : HttpServletRequest {

    override fun getInputStream(): ServletInputStream {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startAsync(): AsyncContext {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun startAsync(servletRequest: ServletRequest?, servletResponse: ServletResponse?): AsyncContext {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun getProtocol(): String = "HTTP/1.1"

    override fun getRequestURL(): StringBuffer = StringBuffer("http://localhost:8080/")

    public var characterEncodingInt: String? = null

    override fun setCharacterEncoding(env: String) {
        characterEncodingInt = env
    }

    public val parameters: MutableMap<String, Array<String>> = mutableMapOf<String, Array<String>>()

    override fun getParameterValues(name: String): Array<String>? = parameters[name]

    override fun isAsyncStarted(): Boolean = false

    override fun getContentLengthLong(): Long = -1

    @Deprecated("Deprecated in Java")
    override fun getRealPath(path: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun login(username: String?, password: String?) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isRequestedSessionIdValid(): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    /**
     * Returns [MockHttpEnvironment.serverPort].
     */
    override fun getServerPort(): Int = MockHttpEnvironment.serverPort

    override fun getRequestedSessionId(): String = session.id

    override fun getServletPath(): String = ""

    override fun getSession(create: Boolean): HttpSession {
        val isValid = (session as? MockHttpSession)?.isValid ?: true
        if (create && !isValid) {
            session = MockHttpSession.create(session.servletContext)
        }
        return session
    }

    override fun getSession(): HttpSession = getSession(true)

    override fun getServerName(): String = "127.0.0.1"

    override fun getLocalAddr(): String = "127.0.0.1"

    override fun <T : HttpUpgradeHandler?> upgrade(handlerClass: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
    }

    override fun isRequestedSessionIdFromCookie(): Boolean = false

    public var partsInt: MutableList<Part>? = null

    override fun getPart(name: String): Part? {
        if (partsInt == null) throw IllegalStateException("Unable to process parts as no multi-part configuration has been provided")
        return partsInt!!.firstOrNull { it.name == name }
    }

    override fun isRequestedSessionIdFromURL(): Boolean = false

    /**
     * Returns [MockHttpEnvironment.localPort].
     */
    override fun getLocalPort(): Int = MockHttpEnvironment.localPort

    @Deprecated("Deprecated in Java")
    override fun isRequestedSessionIdFromUrl(): Boolean = false

    override fun getServletContext(): ServletContext = session.servletContext

    override fun getQueryString(): String? = null

    override fun getDispatcherType(): DispatcherType = DispatcherType.REQUEST

    override fun getParts(): MutableCollection<Part> {
        return partsInt ?: throw IllegalStateException("Unable to process parts as no multi-part configuration has been provided")
    }

    override fun getScheme(): String = "http"

    override fun logout() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLocalName(): String = "localhost"

    override fun isAsyncSupported(): Boolean = false

    override fun getParameterNames(): Enumeration<String> = Collections.enumeration(parameters.keys)

    override fun authenticate(response: HttpServletResponse?): Boolean {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPathTranslated(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getIntHeader(name: String): Int = getHeader(name)?.toInt() ?: -1

    override fun changeSessionId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getAsyncContext(): AsyncContext {
        throw IllegalStateException("async not supported in mock environment")
    }

    override fun getRequestURI(): String = "/"

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    public var isUserInRole: (Principal, role: String) -> Boolean = { _, _ ->  false }

    /**
     * Set [isUserInRole] to modify the outcome of this function.
     */
    override fun isUserInRole(role: String): Boolean {
        val p = userPrincipal ?: return false
        return isUserInRole(p, role)
    }

    override fun getPathInfo(): String? = null

    override fun getRemoteUser(): String? = null

    public var cookiesInt: Array<Cookie>? = null

    public fun addCookie(cookie: Cookie) {
        if (cookiesInt == null) {
            cookiesInt = arrayOf()
        }
        cookiesInt = cookiesInt!! + cookie
    }

    override fun getCookies(): Array<Cookie>? = cookiesInt

    public var localeInt: Locale = Locale.US

    override fun getLocale(): Locale = localeInt

    override fun getMethod(): String = "GET"

    override fun getParameterMap(): Map<String, Array<String>> = parameters

    override fun getAttributeNames(): Enumeration<String> = attributes.keys()

    override fun getRemoteAddr(): String = MockHttpEnvironment.remoteAddr

    override fun getHeaders(name: String): Enumeration<String> {
        val h = headers[name]
        return if (h == null) Collections.emptyEnumeration() else Collections.enumeration(h)
    }

    public var userPrincipalInt: Principal? = null

    /**
     * Set via [userPrincipalInt].
     */
    override fun getUserPrincipal(): Principal? = userPrincipalInt

    override fun getReader(): BufferedReader {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getLocales(): Enumeration<Locale> = Collections.enumeration(listOf(locale))

    /**
     * Returns [MockHttpEnvironment.authType]
     */
    override fun getAuthType(): String? = MockHttpEnvironment.authType

    override fun getCharacterEncoding(): String? = null

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getContentLength(): Int = -1

    public val headers: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>()
    init {
        headers["user-agent"] = listOf("IntelliJ IDEA/182.4892.20")
    }

    override fun getHeader(headerName: String): String? = headers[headerName]?.get(0)

    override fun getContextPath(): String = ""

    override fun getContentType(): String? = null

    override fun getHeaderNames(): Enumeration<String> = headers.keys()

    private val attributes = ConcurrentHashMap<String, Any>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun setAttribute(name: String, value: Any?) {
        attributes.putOrRemove(name, value)
    }

    override fun getParameter(parameter: String): String? = parameters[parameter]?.get(0)

    /**
     * Returns [MockHttpEnvironment.remotePort].
     */
    override fun getRemotePort(): Int = MockHttpEnvironment.remotePort

    override fun getDateHeader(name: String?): Long = -1

    override fun getRemoteHost(): String = "127.0.0.1"

    /**
     * Returns [MockHttpEnvironment.isSecure]
     */
    override fun isSecure(): Boolean = MockHttpEnvironment.isSecure

    public fun setParameter(name: String, vararg values: String) {
        parameters[name] = arrayOf(*values)
    }
}
