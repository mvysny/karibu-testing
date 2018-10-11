package com.github.karibu.testing.v10;

import com.vaadin.flow.component.Component;

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
     * The required Component.getId; if null, no particular id is matched.
     * @param id the id
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withId(@Nullable String id) {
        spec.setId(id);
        return this;
    }

    /**
     * The required Component.caption; if null, no particular caption is matched.
     * @param caption
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withCaption(@Nullable String caption) {
        spec.setCaption(caption);
        return this;
    }

    /**
     * The required Component.placeholder; if null, no particular placeholder is matched.
     * @param placeholder
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withPlaceholder(@Nullable String placeholder) {
        spec.setPlaceholder(placeholder);
        return this;
    }

    /**
     * if not null, the component's [com.vaadin.flow.dom.Element.getText] must match given text.
     * @param text
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withText(@Nullable String text) {
        spec.setText(text);
        return this;
    }

    /**
     * expected count of matching components, defaults to `0..Int.MAX_VALUE`
     * @param count
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withCount(@NotNull IntRange count) {
        spec.setCount(count);
        return this;
    }

    /**
     * Adds an additional predicate which the component needs to match. Not null.
     * <p/>
     * Please remember to provide a proper `toString()` for the predicate,
     * so that you'll get an informative error message on lookup failure.
     * @param predicate
     * @return this
     */
    @NotNull
    public SearchSpecJ<T> withPredicate(@NotNull Predicate<T> predicate) {
        spec.getPredicates().add(predicate);
        return this;
    }

    @Override
    public String toString() {
        return spec.toString();
    }
}
