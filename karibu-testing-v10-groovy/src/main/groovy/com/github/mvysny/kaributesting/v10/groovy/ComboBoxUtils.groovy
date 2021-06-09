package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.ComboBoxKt
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.select.Select
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * A set of basic extension methods for {@link ComboBox}.
 * @author mavi
 */
@CompileStatic
class ComboBoxUtils {
    /**
     * Emulates an user inputting something into the combo box, filtering items.  You can use [getSuggestionItems]
     * to retrieve those items and to verify that the filter on your data provider works properly.
     */
    static void setUserInput(@NotNull ComboBox self, @Nullable String userInput) {
        ComboBoxKt.setUserInput(self, userInput)
    }

    /**
     * Simulates the user creating a custom item. Only works if the field is editable by the user
     * and allows custom values ([ComboBox.isAllowCustomValue] is true).
     */
    static void _fireCustomValueSet(@NotNull ComboBox self, @NotNull String userInput) {
        ComboBoxKt._fireCustomValueSet(self, userInput)
    }

    /**
     * Fetches items currently displayed in the suggestion box.
     */
    @NotNull
    static <T> List<T> getSuggestionItems(@NotNull ComboBox<T> self) {
        ComboBoxKt.getSuggestionItems(self)
    }

    /**
     * Fetches captions of items currently displayed in the suggestion box.
     */
    @NotNull
    static List<String> getSuggestions(@NotNull ComboBox self) {
        ComboBoxKt.getSuggestions(self)
    }

    /**
     * Fetches items currently displayed in the suggestion box.
     */
    @NotNull
    static <T> List<T> getSuggestionItems(@NotNull Select<T> self) {
        ComboBoxKt.getSuggestionItems(self)
    }

    /**
     * Fetches captions of items currently displayed in the suggestion box.
     */
    @NotNull
    static List<String> getSuggestions(@NotNull Select self) {
        ComboBoxKt.getSuggestions(self)
    }
}
