package com.github.mvysny.kaributesting.v10.groovy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mvysny.kaributesting.v10.BasicUtilsKt
import com.github.mvysny.kaributesting.v10.ElementUtilsKt
import com.github.mvysny.kaributesting.v10.SearchSpec
import com.github.mvysny.kaributools.ComponentUtilsKt
import com.github.mvysny.kaributools.IconName
import com.github.mvysny.kaributools.IconUtilsKt
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.dom.Element
import elemental.json.Json
import elemental.json.JsonObject
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jsoup.nodes.Node

/**
 * A set of basic extension methods for all Vaadin components.
 * @author mavi
 */
@CompileStatic
class BasicUtils {
    /**
     * Allows us to fire any Vaadin event on any Vaadin component.
     * @receiver the component, not null.
     * @param event the event, not null.
     */
    static void _fireEvent(@NotNull Component self, @NotNull ComponentEvent<?> event) {
        BasicUtilsKt._fireEvent(self, event)
    }

    /**
     * Fires a DOM [event] on this component.
     */
    static void _fireDomEvent(@NotNull Element self, @NotNull DomEvent event) {
        ElementUtilsKt._fireDomEvent(self, event)
    }

    /**
     * Fires a DOM event on this component.
     * @param eventType the event type, e.g. "click"
     * @param eventData optional event data, defaults to an empty object.
     */
    static void _fireDomEvent(@NotNull Component self, @NotNull String eventType, @NotNull ObjectNode eventData = new ObjectMapper().createObjectNode()) {
        BasicUtilsKt._fireDomEvent(self, eventType, eventData)
    }

    /**
     * The same as {@link Component#getId()} but without Optional.
     * <p></p>
     * Workaround for https://github.com/vaadin/flow/issues/664
     */
    @Nullable
    static String getId_(@NotNull Component self) {
        return BasicUtilsKt.getId_(self)
    }

    /**
     * Checks whether the component is visible (usually {@link Component#isVisible()} but for {@link com.vaadin.flow.component.Text}
     * the text must be non-empty).
     */
    static boolean get_isVisible(@NotNull Component self) {
        return BasicUtilsKt.get_isVisible(self)
    }

    /**
     * Returns direct text contents (it doesn't peek into the child elements).
     */
    static String get_text(@NotNull Component self) {
        return BasicUtilsKt.get_text(self)
    }

    /**
     * Checks that a component is actually editable by the user:
     * <ul><li>The component must be effectively visible: it itself must be visible, its parent must be visible and all of its ascendants must be visible.
     *   For the purpose of testing individual components not attached to the [UI], a component may be considered visible even though it's not
     *   currently nested in a [UI].</li>
     * <li>The component must be effectively enabled: it itself must be enabled, its parent must be enabled and all of its ascendants must be enabled.</li>
     * <li>If the component is [HasValue], it must not be [HasValue.isReadOnly].</li></ul>
     * @throws IllegalStateException if any of the above doesn't hold.
     */
    static void checkEditableByUser(@NotNull Component self) {
        BasicUtilsKt.checkEditableByUser(self)
    }

    /**
     * Fails if the component is editable. See [checkEditableByUser] for more details.
     * @throws AssertionError if the component is editable.
     */
    static void expectNotEditableByUser(@NotNull Component self) {
        BasicUtilsKt.expectNotEditableByUser(self)
    }

    /**
     * This function actually works, as opposed to [Element.getTextRecursively].
     */
    @NotNull
    static String getTextRecursively2(@NotNull Element self) {
        return com.github.mvysny.kaributools.ElementUtilsKt.getTextRecursively2(self)
    }

    /**
     * This function actually works, as opposed to [Element.getTextRecursively].
     */
    @NotNull
    static String getTextRecursively(@NotNull Node self) {
        return com.github.mvysny.kaributools.ElementUtilsKt.getTextRecursively(self)
    }

    /**
     * Computes that this component and all of its parents are enabled.
     * @return false if this component or any of its parent is disabled.
     * @deprecated replaced by {@link #isEnabled(com.vaadin.flow.component.Component)}.
     */
    @Deprecated
    static boolean isEffectivelyEnabled(@NotNull Component self) {
        return BasicUtilsKt.isEffectivelyEnabled(self)
    }

    /**
     * Checks whether this component is {@link com.vaadin.flow.component.HasEnabled#isEnabled()}.
     * All components not implementing {@link com.vaadin.flow.component.HasEnabled} are considered enabled unless
     * some of their ascendants is disabled.
     */
    static boolean isEnabled(@NotNull Component self) {
        return BasicUtilsKt.isEnabled(self)
    }

    // modify when this is fixed: https://github.com/vaadin/flow/issues/4068
    @Nullable
    @CompileDynamic // workaround for NPE bug in Groovy compiler
    static String getPlaceholder(@NotNull Component self) {
        return ComponentUtilsKt.getPlaceholder(self)
    }
    @CompileDynamic // workaround for NPE bug in Groovy compiler
    static void setPlaceholder(@NotNull Component self, @Nullable String placeholder) {
        ComponentUtilsKt.setPlaceholder(self, placeholder)
    }

    /**
     * Removes the component from its parent. Does nothing if the component does not have a parent.
     */
    @CompileDynamic // workaround for NPE bug in Groovy compiler
    static void removeFromParent(@NotNull Component self) {
        ComponentUtilsKt.removeFromParent(self)
    }

    /**
     * Checks whether this component matches given spec. All rules are matched except the {@link SearchSpec#count} rule. The
     * rules are matched against given component only (not against its children).
     * <p></p>
     * Example of use:
     * <pre>
     * button.matches { caption = "Foo" }
     * </pre>
     */
    static boolean matches(@NotNull Component self,
                           @NotNull @DelegatesTo(type = "com.github.mvysny.kaributesting.v10.SearchSpec<com.vaadin.flow.component.Component>", strategy = Closure.DELEGATE_FIRST) Closure spec) {
        return BasicUtilsKt.matches(self, new Function1<SearchSpec<Component>, Unit>() {
            @Override
            Unit invoke(SearchSpec<Component> componentSearchSpec) {
                spec.delegate = componentSearchSpec
                spec.resolveStrategy = Closure.DELEGATE_FIRST
                spec()
                return Unit.INSTANCE
            }
        })
    }

    /**
     * Fires [FocusNotifier.FocusEvent] on the component, but only if it's editable.
     */
    @CompileDynamic
    static void _focus(@NotNull Component self) {
        BasicUtilsKt._focus(self)
    }

    /**
     * Fires [BlurNotifier.BlurEvent] on the component, but only if it's editable.
     */
    @CompileDynamic
    static void _blur(@NotNull Component self) {
        BasicUtilsKt._blur(self)
    }

    /**
     * Returns the notification text.
     */
    @NotNull
    static String getText(@NotNull Notification self) {
        com.github.mvysny.kaributools.NotificationsKt.getText(self)
    }

    @Nullable
    static IconName getIconName(@NotNull Icon self) {
        return IconUtilsKt.getIconName(self)
    }

    static void setIconName(@NotNull Icon self, @Nullable IconName iconName) {
        IconUtilsKt.setIconName(self, iconName)
    }

    /**
     * Closes the UI and simulates the end of the request. The [UI.close] is called,
     * but also the session is set to null which fires the detach listeners and makes
     * the UI and all of its components detached.
     */
    static void _close(@NotNull UI self) {
        BasicUtilsKt._close(self)
    }

    /**
     * Returns child components which were added to this component via
     * [com.vaadin.flow.dom.Element.appendVirtualChild].
     */
    static List<Component> _getVirtualChildren(@NotNull Component self) {
        BasicUtilsKt._getVirtualChildren(self)
    }

    /**
     * Fails if this component is not {@link com.vaadin.flow.component.HasEnabled#isEnabled()}.
     * May succeed when the parent is disabled.
     */
    static void _expectEnabled(@NotNull Component self) {
        BasicUtilsKt._expectEnabled(self)
    }

    /**
     * Fails if this component is {@link com.vaadin.flow.component.HasEnabled#isEnabled()}.
     * May succeed when the parent is disabled.
     */
    static void _expectDisabled(@NotNull Component self) {
        BasicUtilsKt._expectDisabled(self)
    }

    /**
     * Fails if given component is not {@link com.vaadin.flow.component.HasValue#isReadOnly() read-only}.
     */
    static void _expectReadOnly(@NotNull HasValue self) {
        BasicUtilsKt._expectReadOnly(self)
    }

    /**
     * Fails if given component is {@link HasValue#isReadOnly() read-only}.
     */
    static void _expectNotReadOnly(@NotNull HasValue self) {
        BasicUtilsKt._expectNotReadOnly(self)
    }
}
