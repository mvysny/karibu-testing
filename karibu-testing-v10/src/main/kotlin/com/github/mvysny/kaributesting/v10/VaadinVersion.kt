package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper
import com.github.mvysny.kaributesting.v10.mock.checkVaadinSupportedByKaribuTesting
import elemental.json.JsonObject

public object VaadinMeta {
    public val flowBuildInfo: JsonObject? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        MockVaadinHelper.getTokenFileFromClassloader()
    }

    /**
     * Always false.
     */
    public val isCompatibilityMode: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        checkVaadinSupportedByKaribuTesting()
        false
    }
}
