package com.github.karibu.testing;

import com.vaadin.ui.Component;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import kotlin.Unit;

/**
 * @author mavi
 */
public class LocatorJ {
    /**
     * Finds a VISIBLE component of given type which matches given class. UI.getCurrent() all of its descendants are searched.
     * @param clazz the component class
     * @return the only matching component, never null.
     * @throws IllegalArgumentException if no component matched, or if more than one component matches.
     */
    @NotNull
    public static <T extends Component> T _get(@NotNull Class<T> clazz) {
        return LocatorKt._get(clazz, searchSpec -> Unit.INSTANCE);
    }

    /**
     * Finds a VISIBLE component in the current UI of given clazz which matches given spec. The [UI.getCurrent] and all of its descendants are searched.
     * <p></p>
     * Example:
     * <code>import static com.github.karibu.testing.LocatorJ.*; _get(TextField.class, spec -> spec.withCaption("Name:").withId("name"));</code>
     * @param clazz the component must be of this class.
     * @param spec allows you to add search criterion.
     * @return the only matching component, never null.
     * @throws IllegalArgumentException if no component matched, or if more than one component matches.
     */
    @NotNull
    public static <T extends Component> T _get(@NotNull Class<T> clazz, @NotNull Consumer<SearchSpecJ<T>> spec) {
        return LocatorKt._get(clazz, ss -> {
            spec.accept(new SearchSpecJ<>(ss));
            return Unit.INSTANCE;
        });
    }

    /**
     * Finds a VISIBLE component of given type which matches given class. The receiver and all of its descendants are searched.
     * @param receiver the parent layout to search in, not null.
     * @param clazz the component class
     * @return the only matching component, never null.
     * @throws IllegalArgumentException if no component matched, or if more than one component matches.
     */
    @NotNull
    public static <T extends Component> T _get(@NotNull Component receiver, @NotNull Class<T> clazz) {
        return LocatorKt._get(receiver, clazz, searchSpec -> Unit.INSTANCE);
    }

    /**
     * Finds a VISIBLE component in the current UI of given clazz which matches given spec. The receiver and all of its descendants are searched.
     * <p></p>
     * Example:
     * <code>import static com.github.karibu.testing.LocatorJ.*; _get(layout, TextField.class, spec -> spec.withCaption("Name:").withId("name"));</code>
     * @param receiver the parent layout to search in, not null.
     * @param clazz the component must be of this class.
     * @param spec allows you to add search criterion.
     * @return the only matching component, never null.
     * @throws IllegalArgumentException if no component matched, or if more than one component matches.
     */
    @NotNull
    public static <T extends Component> T _get(@NotNull Component receiver, @NotNull Class<T> clazz, @NotNull Consumer<SearchSpecJ<T>> spec) {
        return LocatorKt._get(clazz, ss -> {
            spec.accept(new SearchSpecJ<>(ss));
            return Unit.INSTANCE;
        });
    }
}
