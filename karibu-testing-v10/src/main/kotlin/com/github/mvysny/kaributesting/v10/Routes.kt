package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.startup.ApplicationRouteRegistry
import com.vaadin.flow.server.startup.RouteRegistryInitializer
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import java.io.Serializable
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicReference
import javax.servlet.ServletContext
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
data class Routes(
        val routes: MutableSet<Class<out Component>> = mutableSetOf(),
        val errorRoutes: MutableSet<Class<out HasErrorParameter<*>>> = mutableSetOf(),
        var skipPwaInit: Boolean = true
) : Serializable {

    /**
     * Manually register error routes. No longer needed since [autoDiscoverViews] can now detect error routes.
     */
    @Deprecated("No longer needed, error routes are now auto-detected with autoDiscoverViews()", ReplaceWith(""))
    fun addErrorRoutes(vararg routes: Class<out HasErrorParameter<*>>): Routes = apply {
        errorRoutes.addAll(routes.toSet())
    }

    /**
     * Registers all routes to Vaadin 13 registry. Automatically called from [MockVaadin.setup].
     */
    @Suppress("UNCHECKED_CAST")
    fun register(sc: ServletContext) {
        RouteRegistryInitializer().onStartup(routes.toSet(), sc)
        val registry: ApplicationRouteRegistry = ApplicationRouteRegistry.getInstance(sc)
        registry.setErrorNavigationTargets(errorRoutes.map { it as Class<out Component> }.toSet())
        if (skipPwaInit) {
            registry.clearPwaClass()
        }
    }

    /**
     * Auto-discovers everything, registers it into `this` and returns `this`.
     * * [Route]-annotated views
     * * [HasErrorParameter] error views
     * @param packageName set the package name for the detector to be faster; or provide null to scan the whole classpath, but this is quite slow.
     * @param autoDetectErrorRoutes if false then [HasErrorParameter] error views are not auto-detected. This emulates
     * the old behavior of this method.
     * @return this
     */
    @JvmOverloads
    fun autoDiscoverViews(packageName: String? = null, autoDetectErrorRoutes: Boolean = true): Routes = apply {
        val scan: ScanResult = ClassGraph().enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages(*(if (packageName == null) arrayOf() else arrayOf(packageName))).scan()
        scan.use { scanResult ->
            scanResult.getClassesWithAnnotation(Route::class.java.name).mapTo(routes) { info ->
                Class.forName(info.name).asSubclass(Component::class.java)
            }
            if (autoDetectErrorRoutes) {
                scanResult.getClassesImplementing(HasErrorParameter::class.java.name).mapTo(errorRoutes) { info ->
                    Class.forName(info.name).asSubclass(HasErrorParameter::class.java)
                }
            }
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
fun ApplicationRouteRegistry.clearPwaClass() {
    val pwaClassField: Field = ApplicationRouteRegistry::class.java.getDeclaredField("pwaConfigurationClass").apply { isAccessible = true }
    val ref: AtomicReference<Class<*>> = pwaClassField.get(this) as AtomicReference<Class<*>>
    ref.set(null)
    expect(null) { pwaConfigurationClass }
}
