package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.CheckboxGroupKt
import com.vaadin.flow.component.checkbox.CheckboxGroup
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * A set of basic extension methods for CheckboxGroup.
 * @author mavi
 */
@CompileStatic
class CheckboxGroupUtils {
    /**
     * Fetches renderings of items currently displayed in the list box component.
     */
    @NotNull
    static List<String> getRenderedItems(@NotNull CheckboxGroup self) {
        CheckboxGroupKt.getItemLabels(self)
    }
}
