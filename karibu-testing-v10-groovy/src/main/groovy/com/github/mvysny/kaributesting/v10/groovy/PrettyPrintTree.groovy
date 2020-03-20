package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.PrettyPrintTreeKt
import com.vaadin.flow.component.Component
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * Also see {@link PrettyPrintTreeKt#setPrettyPrintUseAscii(boolean)} and
 * {@link PrettyPrintTreeKt#setPrettyStringHook(kotlin.jvm.functions.Function2)}.
 * @author mavi
 */
@CompileStatic
class PrettyPrintTree {
    @NotNull
    static String toPrettyTree(@NotNull Component self) {
        PrettyPrintTreeKt.toPrettyTree(self)
    }

    /**
     * Returns the most basic properties of the component, formatted as a concise string:
     * <ul>
     * <li>The component class</li>
     * <li>The [Component.getId]</li>
     * <li>Whether the component is [Component.isVisible]</li>
     * <li>Whether it is a [HasValue] that is read-only</li>
     * <li>the styles</li>
     * <li>The [Component.label] and text</li>
     * <li>The [HasValue.getValue]</li>
     * </ul>
     */
    @NotNull
    static String toPrettyString(@NotNull Component self) {
        PrettyPrintTreeKt.toPrettyString(self)
    }
}
