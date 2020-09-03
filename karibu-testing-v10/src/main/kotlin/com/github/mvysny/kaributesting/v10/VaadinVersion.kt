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

    public companion object {
        private val VERSION_REGEX = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")

        public fun fromString(version: String): SemanticVersion {
            val match = requireNotNull(VERSION_REGEX.matchEntire(version),
                    { "The version must be in the form of major.minor.bugfix" })
            return SemanticVersion(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())
        }

        public val VAADIN_14_3: SemanticVersion = SemanticVersion(14, 3, 0)
    }
}

public object VaadinMeta {
    /**
     * Vaadin Flow `flow-server.jar` version: for example 1.2.0 for Vaadin 12
     */
    public val flowVersion: SemanticVersion get() = SemanticVersion(Version.getMajorVersion(), Version.getMinorVersion(), Version.getRevision())

    /**
     * Returns Vaadin version. Returns one of 14, 15, 16 or 17.
     */
    public val version: Int get() = fullVersion.major

    /**
     * Returns a full Vaadin version.
     */
    public val fullVersion: SemanticVersion get() {
        // for Vaadin 14+ the version can be detected from the VaadinCoreShrinkWrap class.
        // This doesn't work for Vaadin 13 or lower, but nevermind - we only support Vaadin 14+ anyway.
        val annotation: NpmPackage = checkNotNull(VaadinCoreShrinkWrap::class.java.getAnnotation(NpmPackage::class.java),
                { "Karibu-Testing 1.2.x only supports Vaadin 14 and higher" })
        val version: String = annotation.version
        return SemanticVersion.fromString(version)
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
        check(fullVersion >= SemanticVersion.VAADIN_14_3) {
            "Karibu-Testing 1.2.x is only compatible with Vaadin ${SemanticVersion.VAADIN_14_3} and above, but got $version"
        }
        if (version == 14) {
            // todo assert that the compatibility mode is not used
        }
        return false
    }
}
