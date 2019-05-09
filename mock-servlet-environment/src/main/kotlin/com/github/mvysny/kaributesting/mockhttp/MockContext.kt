package com.github.mvysny.kaributesting.mockhttp

import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
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
        // for example @HtmlImport("frontend://reviews-list.html") will expect the resource to be present in the war file,
        // which is typically located in $CWD/src/main/webapp/frontend, so let's search for that first
        val realPath = getRealPath(path)
        if (realPath != null) {
            return File(realPath).toURI().toURL()
        }

        // nope, fall back to class loading.
        //
        // for example @HtmlImport("frontend://bower_components/vaadin-button/src/vaadin-button.html") will try to look up
        // the following resources:
        //
        // 1. /frontend/bower_components/vaadin-button/src/vaadin-button.html
        // 2. /webjars/vaadin-button/src/vaadin-button.html
        //
        // we need to match the latter one to a resource on classpath

        if (path.startsWith("/")) {
            val resource: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/resources$path")
            if (resource != null) {
                return resource
            }
        }

        if (path.startsWith("/VAADIN/")) {
            // Vaadin 8 exposed directory
            var path = path
            if (path.contains("..")) {
                // to be able to resolve ThemeResource("../othertheme/img/foo.png") which work from the browser.
                path = Paths.get(path).normalize().toString()
            }
            // reject to serve "/VAADIN/../" resources
            if (path.startsWith("/VAADIN/")) {
                val resource: URL? = Thread.currentThread().contextClassLoader.getResource(path.trimStart('/'))
                if (resource != null) {
                    return resource
                }
            }
        }
        return null
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

    /**
     * [getRealPath] will only resolve `path` in these folders.
     */
    var realPathRoots: List<String> = listOf("src/main/webapp/frontend", "src/main/webapp")

    override fun getRealPath(path: String): String? {
        for (realPathRoot in realPathRoots) {
            val realPath: File = File(moduleDir, "$realPathRoot/$path").canonicalFile.absoluteFile
            if (realPath.absolutePath.startsWith(File(realPathRoot).absolutePath) && realPath.exists()) {
                return realPath.absolutePath
            }
        }
        return null
    }

    val initParameters: MutableMap<String, String> = mutableMapOf<String, String>()

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

    override fun getInitParameterNames(): Enumeration<String> = Collections.enumeration(initParameters.keys)

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
