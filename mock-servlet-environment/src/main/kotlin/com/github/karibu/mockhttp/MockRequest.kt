package com.github.karibu.mockhttp

import java.io.BufferedReader
import java.security.Principal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.*
import javax.servlet.http.*

open class MockRequest(private val session: HttpSession) : HttpServletRequest {

    override fun getInputStream(): ServletInputStream {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startAsync(): AsyncContext {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun startAsync(servletRequest: ServletRequest?, servletResponse: ServletResponse?): AsyncContext {
        throw UnsupportedOperationException("Unsupported")
    }

    override fun getProtocol(): String = "HTTP/1.1"

    override fun getRequestURL(): StringBuffer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setCharacterEncoding(env: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParameterValues(name: String?): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isAsyncStarted(): Boolean = false

    override fun getContentLengthLong(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRealPath(path: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(username: String?, password: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isRequestedSessionIdValid(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServerPort(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRequestedSessionId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServletPath(): String = ""

    override fun getSession(create: Boolean): HttpSession = session

    override fun getSession(): HttpSession = session

    override fun getServerName(): String = "127.0.0.1"

    override fun getLocalAddr(): String = "127.0.0.1"

    override fun <T : HttpUpgradeHandler?> upgrade(handlerClass: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isRequestedSessionIdFromCookie(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPart(name: String?): Part {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isRequestedSessionIdFromURL(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocalPort(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isRequestedSessionIdFromUrl(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServletContext(): ServletContext {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getQueryString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDispatcherType(): DispatcherType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParts(): MutableCollection<Part> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getScheme(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun logout() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocalName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isAsyncSupported(): Boolean = false

    override fun getParameterNames(): Enumeration<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun authenticate(response: HttpServletResponse?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPathTranslated(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIntHeader(name: String?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeSessionId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAsyncContext(): AsyncContext {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRequestURI(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isUserInRole(role: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPathInfo(): String? = null

    override fun getRemoteUser(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCookies(): Array<Cookie> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocale(): Locale = Locale.ENGLISH

    override fun getMethod(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParameterMap(): Map<String, Array<String>> = mapOf()

    override fun getAttributeNames(): Enumeration<String> = attributes.keys()

    override fun getRemoteAddr(): String = "127.0.0.1"

    override fun getHeaders(name: String?): Enumeration<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUserPrincipal(): Principal {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getReader(): BufferedReader {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocales(): Enumeration<Locale> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuthType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCharacterEncoding(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getContentLength(): Int = -1

    val headers = ConcurrentHashMap<String, String>()

    override fun getHeader(headerName: String): String? = headers[headerName]

    override fun getContextPath(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContentType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHeaderNames(): Enumeration<String> = headers.keys()

    private val attributes = ConcurrentHashMap<String, Any>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun setAttribute(name: String, value: Any?) {
        attributes.putOrRemove(name, value)
    }

    private val parameters = mutableMapOf<String, String>()

    override fun getParameter(parameter: String): String? = parameters.get(parameter)
    fun setParameter(parameter: String, value: String?) {
        parameters.putOrRemove(parameter, value)
    }

    override fun getRemotePort(): Int = 8080

    override fun getDateHeader(name: String?): Long = -1

    override fun getRemoteHost(): String = "127.0.0.1"

    override fun isSecure(): Boolean = false
}