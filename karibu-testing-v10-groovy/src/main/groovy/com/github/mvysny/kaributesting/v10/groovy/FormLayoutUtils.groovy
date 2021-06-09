package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.FormLayoutUtilsKt
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.formlayout.FormLayout
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * A set of basic extension methods for {@link FormLayout}.
 * @author Martin Vysny <mavi@vaadin.com>
 */
@CompileStatic
class FormLayoutUtils {
    /**
     * Returns the single field nested in this form item. Fails if there are no
     * child components or if there are too many of them. Automatically skips
     * components placed in the `label` slot (see [FormLayout.FormItem.addToLabel]
     * for more details).
     */
    @NotNull static Component getField(@NotNull FormLayout.FormItem self) {
        FormLayoutUtilsKt.getField(self)
    }
}
