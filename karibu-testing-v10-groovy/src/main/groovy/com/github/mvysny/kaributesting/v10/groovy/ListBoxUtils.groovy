package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.ListBoxKt
import com.vaadin.flow.component.listbox.ListBoxBase
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * A set of basic extension methods for ListBox
 * and MultiSelectListBox.
 * @author mavi
 */
@CompileStatic
class ListBoxUtils {
    /**
     * Fetches renderings of items currently displayed in the list box component.
     */
    @NotNull
    static List<String> getRenderedItems(@NotNull ListBoxBase self) {
        ListBoxKt.getRenderedItems(self)
    }
}
