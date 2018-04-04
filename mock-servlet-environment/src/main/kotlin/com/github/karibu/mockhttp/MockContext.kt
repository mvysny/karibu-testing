package com.github.karibu.mockhttp

import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.*
import javax.servlet.descriptor.JspConfigDescriptor

open class MockContext : ServletContext {
    override fun getServlet(name: String?): Servlet? = null

    override fun <T : Servlet?> createServlet(clazz: Class<T>?): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEffectiveMajorVersion(): Int = 3

    override fun getResource(path: String): URL? {
        val realPath = getRealPath(path) ?: return null
        return File(realPath).toURI().toURL()
    }

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

    override fun getAttributeNames(): Enumeration<String> = attributes.keys()

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

    override fun getResourceAsStream(path: String): InputStream? = getResource(path)?.openStream()

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

    override fun getRealPath(path: String): String? = listOf("src/main/webapp/frontend/$path", "src/main/webapp/$path").asSequence()
        .map { File(it).absolutePath }
        .filter { File(it).exists() }
        .firstOrNull()

    override fun getInitParameter(name: String): String? = null

    override fun getMinorVersion(): Int = 0

    override fun getJspConfigDescriptor(): JspConfigDescriptor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
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

    private val attributes = ConcurrentHashMap<String, Any>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun setAttribute(name: String, value: Any?) {
        attributes.putOrRemove(name, value)
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