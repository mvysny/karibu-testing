package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.mock.MockVaadinHelper
import com.github.mvysny.kaributesting.v10.mock.checkVaadinSupportedByKaribuTesting
import tools.jackson.databind.JsonNode

public object VaadinMeta {
    public val flowBuildInfo: JsonNode? by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
