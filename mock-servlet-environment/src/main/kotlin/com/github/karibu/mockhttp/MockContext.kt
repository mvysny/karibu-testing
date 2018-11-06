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
        TODO("not implemented")
    }

    override fun getEffectiveMajorVersion(): Int = 3

    override fun getResource(path: String): URL? {
        val realPath = getRealPath(path) ?: return null
        return File(realPath).toURI().toURL()
    }

    override fun addListener(className: String?) {
        TODO("not implemented")
    }

    override fun <T : EventListener?> addListener(t: T) {
        TODO("not implemented")
    }

    override fun addListener(listenerClass: Class<out EventListener>?) {
        TODO("not implemented")
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
        TODO("not implemented")
    }

    override fun setSessionTrackingModes(sessionTrackingModes: MutableSet<SessionTrackingMode>) {
        TODO("not implemented")
    }

    override fun setInitParameter(name: String, value: String): Boolean = initParameters.putIfAbsent(name, value) == null

    override fun getResourceAsStream(path: String): InputStream? = getResource(path)?.openStream()

    override fun getNamedDispatcher(name: String?): RequestDispatcher {
        TODO("not implemented")
    }

    override fun getFilterRegistrations(): MutableMap<String, out FilterRegistration> {
        TODO("not implemented")
    }

    override fun getServletNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getDefaultSessionTrackingModes(): Set<SessionTrackingMode> = setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)

    override fun getMimeType(file: String?): String {
        TODO("not implemented")
    }

    override fun declareRoles(vararg roleNames: String?) {
        TODO("not implemented")
    }

    override fun <T : Filter?> createFilter(clazz: Class<T>?): T {
        TODO("not implemented")
    }

    override fun getRealPath(path: String): String? = listOf("src/main/webapp/frontend/$path", "src/main/webapp/$path").asSequence()
        .map { File(moduleDir, it).absolutePath }
        .filter { File(it).exists() }
        .firstOrNull()

    val initParameters = mutableMapOf<String, String>()

    override fun getInitParameter(name: String): String? = initParameters[name]

    override fun getMinorVersion(): Int = 0

    override fun getJspConfigDescriptor(): JspConfigDescriptor {
        TODO("not implemented")
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getServletContextName(): String {
        TODO("not implemented")
    }

    override fun addFilter(filterName: String?, className: String?): FilterRegistration.Dynamic {
        TODO("not implemented")
    }

    override fun addFilter(filterName: String?, filter: Filter?): FilterRegistration.Dynamic {
        TODO("not implemented")
    }

    override fun addFilter(filterName: String?, filterClass: Class<out Filter>?): FilterRegistration.Dynamic {
        TODO("not implemented")
    }

    override fun getContextPath(): String = ""

    override fun getSessionCookieConfig(): SessionCookieConfig {
        TODO("not implemented")
    }

    override fun getVirtualServerName(): String = "mock/localhost" // Tomcat returns "Catalina/localhost"

    override fun getContext(uripath: String?): ServletContext {
        TODO("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        TODO("not implemented")
    }

    private val attributes = ConcurrentHashMap<String, Any>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun setAttribute(name: String, value: Any?) {
        attributes.putOrRemove(name, value)
    }

    override fun getServletRegistration(servletName: String?): ServletRegistration {
        TODO("not implemented")
    }

    override fun <T : EventListener?> createListener(clazz: Class<T>?): T {
        TODO("not implemented")
    }

    override fun addServlet(servletName: String?, className: String?): ServletRegistration.Dynamic {
        TODO("not implemented")
    }

    override fun addServlet(servletName: String?, servlet: Servlet?): ServletRegistration.Dynamic {
        TODO("not implemented")
    }

    override fun addServlet(servletName: String?, servletClass: Class<out Servlet>?): ServletRegistration.Dynamic {
        TODO("not implemented")
    }

    override fun getServlets(): Enumeration<Servlet> = Collections.emptyEnumeration()

    override fun getEffectiveMinorVersion(): Int = 0

    override fun getServletRegistrations(): MutableMap<String, out ServletRegistration> {
        TODO("not implemented")
    }

    override fun getResourcePaths(path: String?): MutableSet<String> = mutableSetOf()

    override fun getInitParameterNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getServerInfo(): String = "Mock"

    override fun getEffectiveSessionTrackingModes(): Set<SessionTrackingMode> = setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(MockContext::class.java)
    }
}

internal val moduleDir: File get() {
    var dir = File("").absoluteFile
    // workaround for https://youtrack.jetbrains.com/issue/IDEA-188466
    // the thing is that when using $MODULE_DIR$, IDEA will set CWD to, say, karibu-testing/.idea/modules/karibu-testing-v8
    // we need to revert that back to karibu-testing/karibu-testing-v8
    if (dir.absolutePath.contains("/.idea/modules")) {
        dir = File(dir.absolutePath.replace("/.idea/modules", ""))
    }
    return dir
}
