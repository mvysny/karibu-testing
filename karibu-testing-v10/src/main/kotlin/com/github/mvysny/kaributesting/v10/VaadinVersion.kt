package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper
import com.github.mvysny.kaributesting.v10.mock.checkVaadinSupportedByKaribuTesting
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.server.Version
import com.vaadin.shrinkwrap.VaadinCoreShrinkWrap
import elemental.json.JsonObject
import java.net.URL

/**
 * See https://semver.org/ for more details.
 */
public data class SemanticVersion(
        val major: Int,
        val minor: Int,
        val bugfix: Int,
        val prerelease: String? = null
) : Comparable<SemanticVersion> {

    init {
        if (prerelease != null) {
            require(prerelease.isNotBlank()) { "$prerelease is blank" }
            require(!prerelease.startsWith('-')) { "$prerelease starts with a dash" }
        }
    }

    override fun compareTo(other: SemanticVersion): Int {
        return compareValuesBy(this, other,
                { it.major },
                { it.minor },
                { it.bugfix },
                // use the unicode character \ufffd when prerelease is null. That places the final version after any -beta versions or such.
                { it.prerelease ?: "\ufffd" })
    }

    /**
     * Formats the version in the following format: `major.minor.bugfix[-prerelease]`.
     */
    override fun toString(): String = "$major.$minor.$bugfix${if (prerelease != null) "-$prerelease" else ""}"

    public fun isExactly(major: Int, minor: Int): Boolean = this.major == major && this.minor == minor
    public fun isExactly(major: Int): Boolean = this.major == major
    public fun isAtLeast(major: Int, minor: Int): Boolean = this >= SemanticVersion(major, minor, 0, "\u0001")
    public fun isAtLeast(major: Int): Boolean = this.major >= major

    public companion object {
        private val VERSION_REGEX = Regex("(\\d+)\\.(\\d+)\\.(\\d+)([-.](.*))?")

        /**
         * Parses the [version] string. Accepts the following formats:
         * * `major.minor.bugfix[-prerelease]`
         * * `major.minor.bugfix[.prerelease]`
         *
         * Always able to parse the output of
         * [SemanticVersion.toString].
         */
        public fun fromString(version: String): SemanticVersion {
            val match: MatchResult = requireNotNull(VERSION_REGEX.matchEntire(version)) {
                "The version must be in the form of major.minor.bugfix but is $version"
            }
            return SemanticVersion(
                    match.groupValues[1].toInt(),
                    match.groupValues[2].toInt(),
                    match.groupValues[3].toInt(),
                    match.groupValues[5].takeIf { it.isNotBlank() }
            )
        }

        public val VAADIN_14_3_0: SemanticVersion = SemanticVersion(14, 3, 0)
        public val VAADIN_14_4_0: SemanticVersion = SemanticVersion(14, 4, 0)
        public val VAADIN_14_5_0: SemanticVersion = SemanticVersion(14, 5, 0)
    }
}

public object VaadinMeta {
    /**
     * Vaadin Flow `flow-server.jar` version: for example 1.2.0 for Vaadin 12
     */
    public val flowVersion: SemanticVersion by lazy {
        SemanticVersion(
            Version.getMajorVersion(),
            Version.getMinorVersion(),
            Version.getRevision()
        )
    }

    /**
     * Returns Vaadin version. Returns one of 14, 15, 16 or 17.
     */
    public val version: Int get() = fullVersion.major

    /**
     * Returns a full Vaadin version.
     */
    public val fullVersion: SemanticVersion by lazy {
        // for Vaadin 14+ the version can be detected from the VaadinCoreShrinkWrap class.
        // This doesn't work for Vaadin 13 or lower, but nevermind - we only support Vaadin 14+ anyway.
        val annotation: NpmPackage = checkNotNull(VaadinCoreShrinkWrap::class.java.getAnnotation(NpmPackage::class.java),
                { "This version of Karibu-Testing only supports Vaadin 14 and higher" })
        val version: String = annotation.version
        SemanticVersion.fromString(version)
    }

    public val flowBuildInfo: JsonObject? by lazy {
        MockVaadinHelper.getTokenFileFromClassloader()
    }

    /**
     * Always false.
     */
    public val isCompatibilityMode: Boolean by lazy {
        checkVaadinSupportedByKaribuTesting()
        if (version == 14) {
            checkNotVaadin14CompatMode()
        }
        false
    }

    private fun checkNotVaadin14CompatMode() {
        val error = "This version of Karibu-Testing doesn't support Vaadin 14 Compatibility mode; please use Karibu-Testing 1.1.x instead. Alternatively, if you're not using compatibility mode, please exclude all webjars from your vaadin/vaadin-core dependency; please see the Skeleton Starter or Karibu10-helloworld-app on how to do that."

        // The WAR project should package the flow-build-info.json config file which
        // clearly states the Vaadin configuration including the compatibility mode setting
        val fbi: JsonObject? = flowBuildInfo
        if (fbi != null) {
            check(!fbi.getBoolean("compatibilityMode")) {
                "flow-build-info.json is set to compatibility mode: $fbi. $error"
            }
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
        check(polymerHtml == null) {
            "Polymer 3 webjar is on the classpath, indicating compatibility mode: $polymerHtml. $error"
        }
    }
}
