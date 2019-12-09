package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.server.Version
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

    val isCompatibilityMode: Boolean get() = flowBuildInfo?.getBoolean("compatibilityMode") ?: true
}
