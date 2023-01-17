package com.github.mvysny.kaributesting.mockhttp

import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.Serializable
import java.lang.Exception
import java.net.URL
import java.net.URLConnection
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import jakarta.servlet.*
import jakarta.servlet.descriptor.JspConfigDescriptor
import kotlin.collections.HashMap

public open class MockContext : ServletContext, Serializable {
    @Deprecated("Deprecated in Java")
    override fun getServlet(name: String): Servlet? {
        // this method is deprecated since servlet spec 2.1 and should always return null.
        // see javadoc for more details.
        return null
    }

    override fun <T : Servlet?> createServlet(clazz: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
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
            @Suppress("NAME_SHADOWING")
            var path = path
            if (path.contains("..")) {
                // to be able to resolve ThemeResource("../othertheme/img/foo.png") which work from the browser.
                path = Paths.get(path).normalize().toString()
                // convert Windows path separators to Linux ones, so that the follow-up code works
                path = path.replace('\\', '/')
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

    override fun addListener(className: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : EventListener?> addListener(t: T) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addListener(listenerClass: Class<out EventListener>) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getClassLoader(): ClassLoader = Thread.currentThread().contextClassLoader

    override fun getAttributeNames(): Enumeration<String> = attributes.keys()

    override fun getMajorVersion(): Int = 3

    override fun log(msg: String) {
        log.error(msg)
    }

    @Deprecated("Deprecated in Java")
    override fun log(exception: Exception, msg: String) {
        log.error(msg, exception)
    }

    override fun log(message: String, throwable: Throwable) {
        log.error(message, throwable)
    }

    override fun getFilterRegistration(filterName: String?): FilterRegistration {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setSessionTrackingModes(sessionTrackingModes: MutableSet<SessionTrackingMode>) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setInitParameter(name: String, value: String): Boolean = initParameters.putIfAbsent(name, value) == null

    override fun getResourceAsStream(path: String): InputStream? = getResource(path)?.openStream()

    override fun getNamedDispatcher(name: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getFilterRegistrations(): MutableMap<String, out FilterRegistration> {
        throw UnsupportedOperationException("not implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getServletNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getDefaultSessionTrackingModes(): Set<SessionTrackingMode> = setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)

    override fun getMimeType(file: String): String = URLConnection.guessContentTypeFromName(file) ?: "application/octet-stream"

    override fun declareRoles(vararg roleNames: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <T : Filter?> createFilter(clazz: Class<T>): T {
        throw UnsupportedOperationException("not implemented")
    }

    /**
     * [getRealPath] will only resolve `path` in these folders.
     */
    public var realPathRoots: List<String> = listOf("src/main/webapp/frontend", "src/main/webapp")

    override fun getRealPath(path: String): String? {
        for (realPathRoot in realPathRoots) {
            val realPath: File = File(moduleDir, "$realPathRoot/$path").canonicalFile.absoluteFile
            if (realPath.absolutePath.startsWith(File(realPathRoot).absolutePath) && realPath.exists()) {
                return realPath.absolutePath
            }
        }
        return null
    }

    public val initParameters: MutableMap<String, String> = mutableMapOf()

    override fun getInitParameter(name: String): String? = initParameters[name]

    override fun getMinorVersion(): Int = 0

    override fun getJspConfigDescriptor(): JspConfigDescriptor {
        throw UnsupportedOperationException("not implemented")
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getServletContextName(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addFilter(filterName: String?, className: String?): FilterRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addFilter(filterName: String?, filter: Filter?): FilterRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addFilter(filterName: String?, filterClass: Class<out Filter>?): FilterRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getContextPath(): String = ""

    override fun getSessionCookieConfig(): SessionCookieConfig {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getVirtualServerName(): String = "mock/localhost" // Tomcat returns "Catalina/localhost"

    private var sessionTimeout: Int = 30

    override fun getSessionTimeout(): Int = sessionTimeout

    override fun setSessionTimeout(sessionTimeout: Int) {
        this.sessionTimeout = sessionTimeout
    }

    private var requestCharacterEncoding: String? = null

    override fun getRequestCharacterEncoding(): String? = requestCharacterEncoding

    override fun setRequestCharacterEncoding(encoding: String?) {
        requestCharacterEncoding = encoding
    }

    private var responseCharacterEncoding: String? = null

    override fun getResponseCharacterEncoding(): String? = responseCharacterEncoding

    override fun setResponseCharacterEncoding(encoding: String?) {
        responseCharacterEncoding = encoding
    }

    override fun getContext(uripath: String): ServletContext {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw UnsupportedOperationException("not implemented")
    }

    private val attributes = ConcurrentHashMap<String, Any>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun setAttribute(name: String, value: Any?) {
        attributes.putOrRemove(name, value)
    }

    override fun getServletRegistration(servletName: String): ServletRegistration? = null

    override fun <T : EventListener?> createListener(clazz: Class<T>?): T {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addServlet(servletName: String?, className: String?): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addServlet(servletName: String?, servlet: Servlet?): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addServlet(servletName: String?, servletClass: Class<out Servlet>?): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    override fun addJspFile(servletName: String, jspFile: String): ServletRegistration.Dynamic {
        throw UnsupportedOperationException("not implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getServlets(): Enumeration<Servlet> = Collections.emptyEnumeration()

    override fun getEffectiveMinorVersion(): Int = 0

    override fun getServletRegistrations(): MutableMap<String, out ServletRegistration> = HashMap()

    override fun getResourcePaths(path: String?): MutableSet<String> = mutableSetOf()

    override fun getInitParameterNames(): Enumeration<String> = Collections.enumeration(initParameters.keys)

    override fun getServerInfo(): String = "Mock"

    override fun getEffectiveSessionTrackingModes(): Set<SessionTrackingMode> =
        setOf(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)

    public companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(MockContext::class.java)
    }
}

internal val moduleDir: File get() {
    var dir = File("").absoluteFile
    // Workaround for https://youtrack.jetbrains.com/issue/IDEA-188466
    // When using $MODULE_DIR$, IDEA will set CWD to, say, karibu-testing/.idea/modules/karibu-testing-v8
    // We need to revert that back to karibu-testing/karibu-testing-v8
    if (dir.absolutePath.contains("/.idea/modules")) {
        dir = File(dir.absolutePath.replace("/.idea/modules", ""))
    }
    return dir
}
