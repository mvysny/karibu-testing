package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.server.Version
import com.vaadin.shrinkwrap.VaadinCoreShrinkWrap
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
     * Returns Vaadin version. Returns one of 14 or 15.
     */
    val version: Int get() {
        // for Vaadin 14+ the version can be detected from the VaadinCoreShrinkWrap class.
        // This doesn't work for Vaadin 13 or lower, but nevermind - we only support Vaadin 15+ anyway.
        val version: String = VaadinCoreShrinkWrap::class.java.getAnnotation(NpmPackage::class.java).version
        return version.takeWhile { it != '.' }.toInt()
    }

    val flowBuildInfo: JsonObject? get() = Thread.currentThread().contextClassLoader
            .getResource("META-INF/VAADIN/config/flow-build-info.json")
            ?.readJson()

    val isCompatibilityMode: Boolean get() {
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
