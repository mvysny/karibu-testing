package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.server.DeploymentConfigurationFactory
import com.vaadin.flow.server.Version
import com.vaadin.shrinkwrap.VaadinCoreShrinkWrap
import elemental.json.Json
import elemental.json.JsonObject
import java.lang.reflect.Method

public data class SemanticVersion(val major: Int, val minor: Int, val bugfix: Int) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int =
            compareValuesBy(this, other, { it.major }, { it.minor }, { it.bugfix })

    override fun toString(): String = "$major.$minor.$bugfix"
}

public object VaadinMeta {
    /**
     * Vaadin Flow `flow-server.jar` version: for example 1.2.0 for Vaadin 12
     */
    public val flowVersion: SemanticVersion get() = SemanticVersion(Version.getMajorVersion(), Version.getMinorVersion(), Version.getRevision())

    /**
     * Returns Vaadin version. Returns one of 14 or 15.
     */
    public val version: Int get() {
        // for Vaadin 14+ the version can be detected from the VaadinCoreShrinkWrap class.
        // This doesn't work for Vaadin 13 or lower, but nevermind - we only support Vaadin 15+ anyway.
        val version: String = VaadinCoreShrinkWrap::class.java.getAnnotation(NpmPackage::class.java).version
        return version.takeWhile { it != '.' }.toInt()
    }

    public val flowBuildInfo: JsonObject? get() {
        // Use DeploymentConfigurationFactory.getResourceFromClassloader() to make sure to read
        // the same flow-build-info.json that Vaadin reads.
        val m: Method = DeploymentConfigurationFactory::class.java.getDeclaredMethod("getResourceFromClassloader")
        m.isAccessible = true
        val json: String = m.invoke(null) as String? ?: return null
        return Json.parse(json)
    }

    /**
     * Always false.
     */
    public val isCompatibilityMode: Boolean get() {
        check(version >= 15) { "Karibu-Testing 1.2.x is only compatible with Vaadin 15 and above, but got $version" }
        return false
    }
}
