package com.github.mvysny.kaributesting.v10;

import com.vaadin.flow.component.Component;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import kotlin.ranges.IntRange;

/**
 * A criterion for matching components. The component must match all of non-null fields.
 * @author mavi
 */
public class SearchSpecJ<T extends Component> {
    @NotNull
    private final SearchSpec<T> spec;

    public SearchSpecJ(@NotNull SearchSpec<T> spec) {
        this.spec = spec;
    }

    /**
     * The required {@link Component#getId()}; if {@code null}, no particular id is matched.
     * @param id the id
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withId(@Nullable String id) {
        spec.setId(id);
        return this;
    }

    /**
     * The required {@link com.github.mvysny.kaributools.ComponentUtilsKt#getLabel(Component)}; if {@code null}, no particular label is matched.
     * @param label
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withLabel(@Nullable String label) {
        spec.setLabel(label);
        return this;
    }

    /**
     * The required {@link com.github.mvysny.kaributools.ComponentUtilsKt#getCaption(Component)}; if {@code null}, no particular caption is matched.
     * @param caption
     * @return this
     * @deprecated Use 'text' for Buttons, 'label' for everything else
     */
    @Deprecated(forRemoval = true)
    @NotNull
    public SearchSpecJ<T> withCaption(@Nullable String caption) {
        spec.setCaption(caption);
        return this;
    }

    /**
     * The required {@link HasValue#getValue()}. If {@code null}, no particular value is matched.
     * @param value the expected value
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withValue(@Nullable Object value) {
        spec.setValue(value);
        return this;
    }

    /**
     * The required {@link com.github.mvysny.kaributools.ComponentUtilsKt#getPlaceholder(Component)}; if {@code null}, no particular placeholder is matched.
     * @param placeholder the placeholder
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withPlaceholder(@Nullable String placeholder) {
        spec.setPlaceholder(placeholder);
        return this;
    }

    /**
     * if not null, the component's {@link com.vaadin.flow.dom.Element#getText} must match given text.
     * @param text the expected text
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withText(@Nullable String text) {
        spec.setText(text);
        return this;
    }

    /**
     * expected count of matching components, defaults to {@code 0..Int.MAX_VALUE}
     * @param count expected count of matching components. Any count of component within this range is accepted.
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withCount(@NotNull IntRange count) {
        spec.setCount(count);
        return this;
    }

    /**
     * Expected count of matching components, defaults to {@code 0..Int.MAX_VALUE}
     * @param count expected count
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withCount(int count) {
        return withCount(count, count);
    }

    /**
     * Expected count of matching components, defaults to {@code 0..Int.MAX_VALUE}
     * @param min minimum count, inclusive
     * @param max maximum count, inclusive
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withCount(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Parameter min: invalid value " + min + ": must be less than or equal to max: " + max);
        }
        return withCount(new IntRange(min, max));
    }

    /**
     * If not null, the component must match all of these classes. Space-separated.
     * @param classes expected space-separated classes.
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withClasses(@Nullable String classes) {
        spec.setClasses(classes);
        return this;
    }

    /**
     * If not null, the component must NOT match any of these classes. Space-separated.
     * @param classes space-separated classes, neither of which must be present on the component.
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withoutClasses(@Nullable String classes) {
        spec.setWithoutClasses(classes);
        return this;
    }

    /**
     * If not null, the component must match all of these themes. Space-separated.
     * @param themes expected space-separated themes.
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withThemes(@Nullable String themes) {
        spec.setThemes(themes);
        return this;
    }

    /**
     * If not null, the component must NOT match any of these themes. Space-separated.
     * @param themes space-separated themes, neither of which must be present on the component.
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withoutThemes(@Nullable String themes) {
        spec.setWithoutThemes(themes);
        return this;
    }

    /**
     * Adds additional predicate which the component needs to match. Not null.
     * <p/>
     * Please remember to provide a proper {@link Object#toString()} for the predicate,
     * so that you'll get an informative error message on lookup failure.
     * @param predicate the matcher
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withPredicate(@NotNull Predicate<T> predicate) {
        spec.getPredicates().add(predicate);
        return this;
    }

    /**
     * Provides a nice summary of all rules set to this spec.
     * @return {@link SearchSpec#toString()}
     */
    @Override
    @NotNull
    public String toString() {
        return spec.toString();
    }

    /**
     * Returns a predicate which matches components based on this spec. All rules are matched, except the {@link #withCount(int)} rule. The
     * rules are matched against given component only (not against its children).
     * @return the predicate, with {@link Object#toString()} returning {@link SearchSpecJ#toString()}.
     */
    @NotNull
    public Predicate<Component> toPredicate() {
        final Function1<Component, Boolean> predicate = spec.toPredicate();
        final String toString = spec.toString();
        return new Predicate<Component>() {
            @Override
            public boolean test(Component component) {
                return predicate.invoke(component);
            }

            @Override
            public String toString() {
                return toString;
            }
        };
    }

    /**
     * Only matches component with given icon. Only works for Button and Icon.
     * @param collection the icon collection, e.g. "vaadin" for Vaadin-provided icons
     * @param iconName the name of the individual icon.
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withIcon(@NotNull String collection, @NotNull String iconName) {
        spec.iconIs(collection, iconName);
        return this;
    }

    /**
     * Only matches component with given icon. Only works for Button and Icon.
     * @param vaadinIcon the icon to match
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withIcon(@NotNull VaadinIcon vaadinIcon) {
        spec.iconIs(vaadinIcon);
        return this;
    }

    /**
     * Only matches component with given enabled state.
     * @param enabled the enabled state to match
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withEnabled(boolean enabled) {
        spec.setEnabled(enabled);
        return this;
    }
}
