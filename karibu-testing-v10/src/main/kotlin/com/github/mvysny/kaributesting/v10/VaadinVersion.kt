package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.server.Version
import elemental.json.Json
import elemental.json.JsonObject
import java.net.URL

data class SemanticVersion(val major: Int, val minor: Int, val bugfix: Int) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int =
            compareValuesBy(this, other, { it.major }, { it.minor }, { it.bugfix })

    override fun toString() = "$major.$minor.$bugfix"
}

@Deprecated("use VaadinMeta.version")
val vaadinVersion: Int get() = VaadinMeta.version

object VaadinMeta {
    /**
     * Vaadin Flow `flow-server.jar` version: for example 1.2.0 for Vaadin 12
     */
    val flowVersion: SemanticVersion get() = SemanticVersion(Version.getMajorVersion(), Version.getMinorVersion(), Version.getRevision())

    /**
     * Guesses Vaadin version from [flowVersion]. Returns one of 11, 12, 13 or 14.
     */
    val version: Int get() = when {
        flowVersion < SemanticVersion(1, 2, 0) -> 11
        flowVersion < SemanticVersion(1, 3, 0) -> 12
        flowVersion < SemanticVersion(2, 0, 0) -> 13
        else -> 14
    }

    val flowBuildInfo: JsonObject? get() = Thread.currentThread().contextClassLoader
            .getResource("META-INF/VAADIN/config/flow-build-info.json")
            ?.readJson()

    val isCompatibilityMode: Boolean get() = flowBuildInfo?.getBoolean("compatibilityMode") ?: true
}

internal fun URL.readJson(): JsonObject = Json.parse(readText())
