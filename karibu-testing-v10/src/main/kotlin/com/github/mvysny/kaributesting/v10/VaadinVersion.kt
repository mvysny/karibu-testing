package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper
import com.github.mvysny.kaributesting.v10.mock.checkVaadinSupportedByKaribuTesting
import com.github.mvysny.kaributools.VaadinVersion
import elemental.json.JsonObject
import java.net.URL

public object VaadinMeta {
    public val flowBuildInfo: JsonObject? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        MockVaadinHelper.getTokenFileFromClassloader()
    }

    /**
     * Always false.
     */
    public val isCompatibilityMode: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        checkVaadinSupportedByKaribuTesting()
        if (VaadinVersion.get.major == 14) {
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
