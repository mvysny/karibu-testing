package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.server.Version

data class SemanticVersion(val major: Int, val minor: Int, val bugfix: Int) : Comparable<SemanticVersion> {
    override fun compareTo(other: SemanticVersion): Int =
            compareValuesBy(this, other, { it.major }, { it.minor }, { it.bugfix })

    override fun toString() = "$major.$minor.$bugfix"
}

/**
 * Vaadin Flow version: for example 1.2.0 for Vaadin 12
 */
val flowVersion: SemanticVersion get() = SemanticVersion(Version.getMajorVersion(), Version.getMinorVersion(), Version.getRevision())

/**
 * Returns Vaadin version: 11, 12 or 13.
 */
val vaadinVersion: Int get() = when {
    flowVersion < SemanticVersion(1, 2, 0) -> 11
    flowVersion < SemanticVersion(1, 3, 0) -> 12
    else -> 13
}
