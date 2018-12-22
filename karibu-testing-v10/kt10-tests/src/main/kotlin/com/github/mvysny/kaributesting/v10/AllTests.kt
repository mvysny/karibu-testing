package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.server.Version

fun DynaNodeGroup.allTests() {
    group("basic utils") {
        basicUtilsTestbatch()
    }
    group("combo box") {
        comboBoxTestbatch()
    }
    group("grid") {
        gridTestbatch()
    }
    group("locator addons") {
        locatorAddonsTestbatch()
    }
    group("locatorj") {
        locatorJTest()
    }
    group("locator") {
        locatorTest()
    }
    group("mock vaadin") {
        mockVaadinTest()
    }
    group("polymer template") {
        polymerTemplateTest()
    }
    group("pretty print tree") {
        prettyPrintTreeTest()
    }
    group("search spec") {
        searchSpecTest()
    }
    group("notifications") {
        notificationsTestBattery()
    }
}

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
 * Returns Vaadin version: 11 or 12.
 */
val vaadinVersion: Int get() = when {
    flowVersion < SemanticVersion(1, 2, 0) -> 11
    else -> 12
}
