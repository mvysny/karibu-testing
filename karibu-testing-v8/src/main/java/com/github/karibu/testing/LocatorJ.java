package com.github.karibu.testing;

import com.vaadin.data.HasValue;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import kotlin.Unit;

/**
 * To use this class, don't forget to add <code>import static com.github.karibu.testing.LocatorJ.*;</code> to your test class.
 *
 * @author mavi
 */
public class LocatorJ {
    /**
     * Finds a VISIBLE component of given type which matches given class. UI.getCurrent() all of its descendants are searched.
     *
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
     *
     * @param clazz the component must be of this class.
     * @param spec  allows you to add search criterion.
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
     *
     * @param receiver the parent layout to search in, not null.
     * @param clazz    the component class
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
     *
     * @param receiver the parent layout to search in, not null.
     * @param clazz    the component must be of this class.
     * @param spec     allows you to add search criterion.
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

    /**
     * Clicks the button, but only if it is actually possible to do so by the user. If the button is read-only or disabled, an exception is thrown.
     *
     * @throws IllegalStateException if the button was not visible or not enabled.
     */
    public static void _click(@NotNull Button receiver) {
        BasicUtilsKt._click(receiver);
    }

    /**
     * Sets the value of given component, but only if it is actually possible to do so by the user.
     * If the component is read-only or disabled, an exception is thrown.
     *
     * @throws IllegalStateException if the field was not visible, not enabled or was read-only.
     */
    public static <V> void _setValue(@NotNull HasValue<V> receiver, @Nullable V value) {
        BasicUtilsKt.set_value(receiver, value);
    }

    /**
     * Finds a list of VISIBLE components of given [clazz]. [UI.getCurrent] and all of its descendants are searched.
     *
     * @return the list of matching components, may be empty.
     */
    public static <T extends Component> List<T> _find(@NotNull Class<T> clazz) {
        return LocatorKt._find(clazz, spec -> Unit.INSTANCE);
    }

    /**
     * Finds a list of VISIBLE components of given [clazz]. [UI.getCurrent] and all of its descendants are searched.
     *
     * @return the list of matching components, may be empty.
     */
    public static <T extends Component> List<T> _find(@NotNull Class<T> clazz, @NotNull Consumer<SearchSpecJ<T>> spec) {
        return LocatorKt._find(clazz, ss -> {
            spec.accept(new SearchSpecJ<>(ss));
            return Unit.INSTANCE;
        });
    }

    /**
     * Finds a list of VISIBLE components of given [clazz]. The [receiver] and all of its descendants are searched.
     *
     * @return the list of matching components, may be empty.
     */
    public static <T extends Component> List<T> _find(@NotNull Component receiver, @NotNull Class<T> clazz) {
        return LocatorKt._find(receiver, clazz, spec -> Unit.INSTANCE);
    }

    /**
     * Finds a list of VISIBLE components of given [clazz] which matches [spec]. The [receiver] and all of its descendants are searched.
     *
     * @return the list of matching components, may be empty.
     */
    public static <T extends Component> List<T> _find(@NotNull Component receiver, @NotNull Class<T> clazz, @NotNull Consumer<SearchSpecJ<T>> spec) {
        return LocatorKt._find(receiver, clazz, ss -> {
            spec.accept(new SearchSpecJ<>(ss));
            return Unit.INSTANCE;
        });
    }

    /**
     * Expects that there are no VISIBLE components in the current UI of given [clazz]. The [UI.getCurrent()] and all of its descendants are searched.
     *
     * @throws IllegalArgumentException if one or more components matched.
     */
    public static <T extends Component> void _assertNone(@NotNull Class<T> clazz) {
        LocatorKt._expectNone(clazz, spec -> Unit.INSTANCE);
    }

    /**
     * Expects that there are no VISIBLE components in the current UI of given [clazz] which matches [spec]. The [UI.getCurrent] and all of its descendants are searched.
     * @throws IllegalArgumentException if one or more components matched.
     */
    public static <T extends Component> void _assertNone(@NotNull Class<T> clazz, @NotNull Consumer<SearchSpecJ<T>> spec) {
        LocatorKt._expectNone(clazz, ss -> {
            spec.accept(new SearchSpecJ<>(ss));
            return Unit.INSTANCE;
        });
    }

    /**
     * Expects that there are no VISIBLE components of given [clazz]. The [receiver] and all of its descendants are searched.
     * @throws IllegalArgumentException if one or more components matched.
     */
    public static <T extends Component> void _assertNone(@NotNull Component receiver, @NotNull Class<T> clazz) {
        LocatorKt._expectNone(clazz, ss -> Unit.INSTANCE);
    }

    /**
     * Expects that there are no VISIBLE components of given [clazz] matching given [spec]. The [receiver] and all of its descendants are searched.
     * @throws IllegalArgumentException if one or more components matched.
     */
    public static <T extends Component> void _assertNone(@NotNull Component receiver, @NotNull Class<T> clazz, @NotNull Consumer<SearchSpecJ<T>> spec) {
        LocatorKt._expectNone(clazz, ss -> {
            spec.accept(new SearchSpecJ<>(ss));
            return Unit.INSTANCE;
        });
    }
}
