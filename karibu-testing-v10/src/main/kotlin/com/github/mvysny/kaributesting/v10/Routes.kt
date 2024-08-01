package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.mock.MockVaadin19
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.router.*
import com.vaadin.flow.server.VaadinContext
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import com.vaadin.flow.server.startup.RouteRegistryInitializer
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.expect

/**
 * A configuration object of all routes and error routes in the application. Simply use [autoDiscoverViews] to discover everything.
 *
 * To speed up the tests, you can create one instance of this class only, then reuse that instance in every
 * call to [MockVaadin.setup].
 * @property routes a list of all route views in your application. Vaadin will ignore any routes not present here.
 * @property errorRoutes a list of all route views in your application. Vaadin will ignore any routes not present here.
 * @property skipPwaInit if true, the PWA initialization code is skipped in Vaadin, which dramatically speeds up
 * the [MockVaadin.setup] from 2 seconds to 50ms. Since that's usually what you want to do, this defaults to true.
 */
public data class Routes(
        val routes: MutableSet<Class<out Component>> = mutableSetOf(),
        val errorRoutes: MutableSet<Class<out HasErrorParameter<*>>> = mutableSetOf(MockRouteNotFoundError::class.java, MockRouteAccessDeniedError::class.java),
        var skipPwaInit: Boolean = true
) : Serializable {

    /**
     * Manually register error routes. No longer needed since [autoDiscoverViews] can now detect error routes.
     */
    @Deprecated("No longer needed, error routes are now auto-detected with autoDiscoverViews()", ReplaceWith(""))
    public fun addErrorRoutes(vararg routes: Class<out HasErrorParameter<*>>): Routes = apply {
        errorRoutes.addAll(routes.toSet())
    }

    /**
     * Registers all routes to Vaadin 15 registry. Automatically called from [MockVaadin.setup].
     */
    @Suppress("UNCHECKED_CAST")
    public fun register(sc: VaadinContext) {
        // otherwise RouteRegistryInitializer will not perform the initialization but
        // will defer it until the Lookup is available.
        MockVaadin19.verifyHasLookup(sc)
        RouteRegistryInitializer().onStartup(routes.toSet(), sc.context)
        checkNotNull(sc.context.getAttribute("com.vaadin.flow.server.startup.ApplicationRouteRegistry${'$'}ApplicationRouteRegistryWrapper")) {
            "RouteRegistryInitializer did not register the ApplicationRouteRegistry!"
        }
        val registry: ApplicationRouteRegistry = ApplicationRouteRegistry.getInstance(sc)
        registry.setErrorNavigationTargets(errorRoutes.map { it as Class<out Component> }.toSet())
        if (skipPwaInit) {
            registry.clearPwaClass()
        }
    }

    /**
     * Auto-discovers everything, registers it into `this` and returns `this`.
     * * [Route]-annotated views go into [routes]
     * * [HasErrorParameter] error views go into [errorRoutes]
     * After this function finishes, you can still modify the [routes] and [errorRoutes] sets,
     * for example you can clear the [errorRoutes] if there is some kind of misdetection.
     * @param packageName set the package name for the detector to be faster; or provide null to scan the whole classpath, but this is quite slow.
     * @return this
     */
    @JvmOverloads
    public fun autoDiscoverViews(packageName: String? = null): Routes = apply {
        val classGraph: ClassGraph = ClassGraph().enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(*(if (packageName == null) arrayOf() else arrayOf(packageName)))
        classGraph.scan().use { scanResult: ScanResult ->
            scanResult.getClassesWithAnnotation(Route::class.java.name).mapTo(routes) { info: ClassInfo ->
                Class.forName(info.name).asSubclass(Component::class.java)
            }
            scanResult.getClassesImplementing(HasErrorParameter::class.java.name).mapTo(errorRoutes) { info: ClassInfo ->
                Class.forName(info.name).asSubclass(HasErrorParameter::class.java)
            }
        }

        // remove @DefaultErrorHandler RouteAccessDeniedError and RouteNotFoundError so that they're replaced with Karibu's Mock counterparts
        // which perform better logging
        errorRoutes.remove(RouteNotFoundError::class.java)
        if (VaadinVersion.get.isAtLeast(24, 3)) {
            errorRoutes.remove(Class.forName("com.vaadin.flow.router.RouteAccessDeniedError"))
        }

        // https://github.com/mvysny/karibu-testing/issues/50
        // if the app defines its own NotFoundException handler, remove MockRouteNotFoundError
        if (errorRoutes.any { it != MockRouteNotFoundError::class.java && it.isRouteNotFound }) {
            errorRoutes.remove(MockRouteNotFoundError::class.java)
        }
        if (errorRoutes.any { it != MockRouteAccessDeniedError::class.java && it.isAccessDenied }) {
            errorRoutes.remove(MockRouteAccessDeniedError::class.java)
        }

        println("Auto-discovered views: $this")
    }

    override fun toString(): String =
            "Routes(routes=${routes.joinToString { it.simpleName }}, errorRoutes=${errorRoutes.joinToString { it.simpleName }})"
}

/**
 * Clears the PWA class config from this registry.
 */
@Suppress("UNCHECKED_CAST")
public fun ApplicationRouteRegistry.clearPwaClass() {
    val pwaClassField: Field = ApplicationRouteRegistry::class.java.getDeclaredField("pwaConfigurationClass").apply { isAccessible = true }
    val ref: AtomicReference<Class<*>> = pwaClassField.get(this) as AtomicReference<Class<*>>
    ref.set(null)
    expect(null) { pwaConfigurationClass }
}

/**
 * This route gets registered by default in [Routes], so that Karibu-Testing can catch
 * any navigation to a missing route and can respond with an informative exception.
 */
@Tag(Tag.DIV)
@AnonymousAllowed
public open class MockRouteNotFoundError: Component(), HasErrorParameter<NotFoundException> {
    override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<NotFoundException>): Int {
        val message: String = buildString {
            val path: String = event.location.path
            append("No route found for '").append(path).append("'")
            if (parameter.hasCustomMessage()) {
                append(": ").append(parameter.customMessage)
            }
            append("\nAvailable routes: ")
            val routes: List<RouteData> = event.source.registry.registeredRoutes
            append(routes.map { it.toPrettyString() })
            append("\nIf you'd like to revert back to the original Vaadin RouteNotFoundError, please remove the ${MockRouteNotFoundError::class.java} from Routes.errorRoutes")
        }
        throw NotFoundException(message).apply { initCause(parameter.caughtException) }
    }
}

/**
 * This route gets registered by default in [Routes], so that Karibu-Testing can catch
 * any [AccessDeniedException] and throw it immediately, instead of redirecting to [NotFoundException] as the original [RouteAccessDeniedError]
 * handler does. Fixes [#161](https://github.com/mvysny/karibu-testing/issues/161).
 */
@Tag(Tag.DIV)
@AnonymousAllowed
public open class MockRouteAccessDeniedError: Component(), HasErrorParameter<AccessDeniedException> {
    override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<AccessDeniedException>): Int {
        // don't re-throw caughtException - the stacktrace won't point here.
        // try our best to preserve the stacktrace, but bail out for custom exceptions
        if (parameter.exception.javaClass == AccessDeniedException::class.java || parameter.exception.javaClass == MockAccessDeniedException::class.java) {
            throw MockAccessDeniedException(parameter)
        }
        log.error("!!!! Karibu-Testing: MockRouteAccessDeniedError caught an exception ${parameter.caughtException}: ${parameter.customMessage}")
        throw parameter.caughtException!!
    }
    public companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(MockRouteAccessDeniedError::class.java)
    }
}

public class MockAccessDeniedException(override val message: String, cause: Throwable?) : AccessDeniedException() {
    public constructor(param: ErrorParameter<AccessDeniedException>) : this(param.customMessage, param.caughtException)
    init {
        initCause(cause)
    }
}

internal fun RouteData.toPrettyString(): String {
    val template = template
    val path: String = if (template.isNullOrBlank()) "<root>" else "/$template"
    return "${navigationTarget.simpleName} at '$path'"
}
