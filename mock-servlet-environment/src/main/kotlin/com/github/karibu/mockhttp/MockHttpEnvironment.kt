package com.github.karibu.mockhttp

import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.*
import javax.servlet.*
import javax.servlet.descriptor.JspConfigDescriptor

open class MockContext : ServletContext {
    override fun getServlet(name: String?): Servlet? = null

    override fun <T : Servlet?> createServlet(clazz: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEffectiveMajorVersion(): Int = 3

    override fun getResource(path: String): URL = File(getRealPath(path)).toURI().toURL()

    override fun addListener(className: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : EventListener?> addListener(t: T) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(listenerClass: Class<out EventListener>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getClassLoader(): ClassLoader = Thread.currentThread().contextClassLoader

    override fun getAttributeNames(): Enumeration<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMajorVersion(): Int = 3

    override fun log(msg: String) {
        log.error(msg)
    }

    override fun log(exception: Exception, msg: String) {
        log.error(msg, exception)
    }

    override fun log(message: String, throwable: Throwable) {
        log.error(message, throwable)
    }

    override fun getFilterRegistration(filterName: String?): FilterRegistration {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setSessionTrackingModes(sessionTrackingModes: MutableSet<SessionTrackingMode>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setInitParameter(name: String?, value: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getResourceAsStream(path: String): InputStream = getResource(path).openStream()

    override fun getNamedDispatcher(name: String?): RequestDispatcher {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFilterRegistrations(): MutableMap<String, out FilterRegistration> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServletNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getDefaultSessionTrackingModes(): MutableSet<SessionTrackingMode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMimeType(file: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun declareRoles(vararg roleNames: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Filter?> createFilter(clazz: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRealPath(path: String): String = File("src/main/webapp/frontend/$path").absolutePath

    override fun getInitParameter(name: String): String? = null

    override fun getMinorVersion(): Int = 0

    override fun getJspConfigDescriptor(): JspConfigDescriptor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAttribute(name: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServletContextName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addFilter(filterName: String?, className: String?): FilterRegistration.Dynamic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addFilter(filterName: String?, filter: Filter?): FilterRegistration.Dynamic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addFilter(filterName: String?, filterClass: Class<out Filter>?): FilterRegistration.Dynamic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContextPath(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionCookieConfig(): SessionCookieConfig {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVirtualServerName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContext(uripath: String?): ServletContext {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val attributes = mutableMapOf<String, Any>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun setAttribute(name: String, `object`: Any?) {
        if (`object` == null) attributes.remove(name) else attributes[name] = `object`
    }

    override fun getServletRegistration(servletName: String?): ServletRegistration {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : EventListener?> createListener(clazz: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addServlet(servletName: String?, className: String?): ServletRegistration.Dynamic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addServlet(servletName: String?, servlet: Servlet?): ServletRegistration.Dynamic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addServlet(servletName: String?, servletClass: Class<out Servlet>?): ServletRegistration.Dynamic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServlets(): Enumeration<Servlet> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEffectiveMinorVersion(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getServletRegistrations(): MutableMap<String, out ServletRegistration> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getResourcePaths(path: String?): MutableSet<String> = mutableSetOf()

    override fun getInitParameterNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getServerInfo(): String = "Mock"

    override fun getEffectiveSessionTrackingModes(): MutableSet<SessionTrackingMode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(MockContext::class.java)
    }
}

open class MockServletConfig(val context: ServletContext) : ServletConfig {
    override fun getInitParameter(name: String): String? = null

    override fun getInitParameterNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getServletName(): String = "Vaadin Servlet"

    override fun getServletContext(): ServletContext = context
}

internal fun <K, V> MutableMap<K, V>.putOrRemove(key: K, value: V?) {
    if (value == null) remove(key) else set(key, value)
}
