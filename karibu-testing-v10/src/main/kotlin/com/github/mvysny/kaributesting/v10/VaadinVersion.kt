package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.server.Version
import elemental.json.JsonObject
import java.net.URL

data class SemanticVersion(val major: Int, val minor: Int, val bugfix: Int) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int =
            compareValuesBy(this, other, { it.major }, { it.minor }, { it.bugfix })

    override fun toString() = "$major.$minor.$bugfix"
}

object VaadinMeta {
    /**
     * Vaadin Flow `flow-server.jar` version: for example 1.2.0 for Vaadin 12
     */
    val flowVersion: SemanticVersion get() = SemanticVersion(Version.getMajorVersion(), Version.getMinorVersion(), Version.getRevision())

    /**
     * Guesses Vaadin version from [flowVersion]. Returns one of 11, 12, 13, 14 or 15.
     */
    val version: Int by lazy {
        try {
            // for Vaadin 14+ the version can be detected from the VaadinCoreShrinkWrap class.
            // This is more accurate but doesn't work for Vaadin 13 or lower.
            val shrinkWrapClazz: Class<*> = Class.forName("com.vaadin.shrinkwrap.VaadinCoreShrinkWrap")
            val version: String = shrinkWrapClazz.getAnnotation(NpmPackage::class.java).version
            return@lazy version.takeWhile { it != '.' } .toInt()
        } catch (ex: ClassNotFoundException) {
            // Vaadin 13 or lower. Fall back and detect the version from flowVersion
        }
        when {
            flowVersion < SemanticVersion(1, 2, 0) -> 11
            flowVersion < SemanticVersion(1, 3, 0) -> 12
            flowVersion < SemanticVersion(2, 0, 0) -> 13
            else -> 14
        }
    }

    val flowBuildInfo: JsonObject? get() = Thread.currentThread().contextClassLoader
            .getResource("META-INF/VAADIN/config/flow-build-info.json")
            ?.readJson()

    /**
     * Tells Karibu-Testing whether we run the tests in the compatibility mode or not.
     * If we run in npm mode, Karibu-Testing needs to hook into PolymerTemplate loading mechanism.
     *
     * You can force a particular mode by calling `System.setProperty("vaadin.compatibilityMode", "true")`.
     *
     * This value is also applied to Vaadin's DeploymentConfiguration.
     */
    val isCompatibilityMode: Boolean get() {
        // allow overriding this via a system property
        if (System.getProperty("vaadin.compatibilityMode") != null) {
            return System.getProperty("vaadin.compatibilityMode").toBoolean()
        }

        if (version <= 13) {
            // Vaadin 13 and lower always uses Bower mode
            return true
        }
        if (version >= 15) {
            // Vaadin 15 and higher always uses npm mode
            return false
        }

        // Vaadin 14.

        // The WAR project should package the flow-build-info.json config file which
        // clearly states the Vaadin configuration including the compatibility mode setting
        val fbi: JsonObject? = flowBuildInfo
        if (fbi != null) {
            return fbi.getBoolean("compatibilityMode")
        }
        // The `flow-build-info.json` may be missing - that happens when we're in a Bower mode,
        // but that also happens when we're not testing a WAR
        // project but a module jar with additional components.
        //
        // The compat mode is pretty much a configuration of the Vaadin Maven Plugin
        // and it's impossible to figure that out. Instead, let's simply check
        // whether the polymer.jar is on the classpath. If it is, then we're using
        // Bower mode and thus the compat mode.
        val polymerHtml: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/resources/webjars/polymer/polymer.html")
        return polymerHtml != null
    }
}
