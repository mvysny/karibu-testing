package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.RouterLinkKt
import com.vaadin.flow.router.RouterLink
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * @author mavi
 */
@CompileStatic
class RouterLinkUtils {
    /**
     * Navigates to where this router link points to.
     * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
     * more details.
     */
    static void _click(@NotNull RouterLink self) {
        RouterLinkKt._click(self)
    }

    /**
     * Navigates to where this router link points to. The difference to [_click] is that this one doesn't check whether
     * the link is actually visible and enabled.
     */
    static void click(@NotNull RouterLink self) {
        RouterLinkKt.click(self)
    }
}
