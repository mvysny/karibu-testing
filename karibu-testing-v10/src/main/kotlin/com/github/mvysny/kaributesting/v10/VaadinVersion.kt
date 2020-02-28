package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.server.Version
import com.vaadin.shrinkwrap.VaadinCoreShrinkWrap
import elemental.json.JsonObject

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
     * Guesses Vaadin version from [flowVersion]. Returns one of 14 or 15.
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

    val isCompatibilityMode: Boolean get() = flowBuildInfo?.getBoolean("compatibilityMode") ?: true
}
