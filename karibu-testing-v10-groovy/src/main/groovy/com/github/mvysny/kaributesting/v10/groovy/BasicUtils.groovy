package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.BasicUtilsKt
import com.github.mvysny.kaributesting.v10.HasValueUtilsKt
import com.github.mvysny.kaributesting.v10.IconKt
import com.github.mvysny.kaributesting.v10.IconName
import com.github.mvysny.kaributesting.v10.NotificationsKt
import com.github.mvysny.kaributesting.v10.SearchSpec
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.Focusable
import com.vaadin.flow.component.HasValue
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
        BasicUtilsKt._fireDomEvent(self, event)
    }

    /**
     * Fires a DOM event on this component.
     * @param eventType the event type, e.g. "click"
     * @param eventData optional event data, defaults to an empty object.
     */
    static void _fireDomEvent(@NotNull Component self, @NotNull String eventType, @NotNull JsonObject eventData = Json.createObject()) {
        BasicUtilsKt._fireDomEvent(self, eventType, eventData)
    }

    /**
     * Determines the component's `label` (usually it's the HTML element's `label` property, but it's [Checkbox.getLabel] for checkbox).
     * Intended to be used for fields such as [TextField].
     */
    @NotNull
    static String getLabel(@NotNull Component self) {
        return BasicUtilsKt.getLabel(self)
    }
    static void setLabel(@NotNull Component self, @NotNull String label) {
        BasicUtilsKt.setLabel(self, label)
    }

    /**
     * The Component's caption: [Button.getText] for [Button], [label] for fields such as [TextField].
     * <p></p>
     * For FormItem: Concatenates texts from all elements placed in the `label` slot. This effectively
     * returns whatever was provided in the String label via [FormLayout.addFormItem].
     */
    @NotNull
    static String getCaption(@NotNull Component self) {
        return BasicUtilsKt.getCaption(self)
    }
    static void setCaption(@NotNull Component self, @NotNull String caption) {
        BasicUtilsKt.setCaption(self, caption)
    }

    /**
     * The same as [Component.getId] but without Optional.
     *
     * Workaround for https://github.com/vaadin/flow/issues/664
     */
    @Nullable
    static String getId_(@NotNull Component self) {
        return BasicUtilsKt.getId_(self)
    }

    /**
     * Checks whether the component is attached to the UI.
     * <p></p>
     * Returns true for attached components even if the UI itself is closed.
     */
    static boolean isAttached(@NotNull Component self) {
        return BasicUtilsKt.isAttached(self)
    }

    /**
     * Checks whether the component is visible (usually [Component.isVisible] but for [Text]
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
        return BasicUtilsKt.getTextRecursively2(self)
    }

    /**
     * This function actually works, as opposed to [Element.getTextRecursively].
     */
    @NotNull
    static String getTextRecursively(@NotNull Node self) {
        return BasicUtilsKt.getTextRecursively(self)
    }

    /**
     * Computes that this component and all of its parents are enabled.
     * @return false if this component or any of its parent is disabled.
     */
    static boolean isEffectivelyEnabled(@NotNull Component self) {
        return BasicUtilsKt.isEffectivelyEnabled(self)
    }

    /**
     * Checks whether this component is {@link com.vaadin.flow.component.HasEnabled#isEnabled()}.
     * All components not implementing {@link com.vaadin.flow.component.HasEnabled} are considered enabled.
     */
    static boolean isEnabled(@NotNull Component self) {
        return BasicUtilsKt.isEnabled(self)
    }

    // modify when this is fixed: https://github.com/vaadin/flow/issues/4068
    @Nullable
    static String getPlaceholder(@NotNull Component self) {
        return BasicUtilsKt.getPlaceholder(self)
    }
    static void setPlaceholder(@NotNull Component self, @Nullable String placeholder) {
        BasicUtilsKt.setPlaceholder(self, placeholder)
    }

    /**
     * Removes the component from its parent. Does nothing if the component does not have a parent.
     */
    static void removeFromParent(@NotNull Component self) {
        BasicUtilsKt.removeFromParent(self)
    }

    /**
     * Checks whether this component matches given spec. All rules are matched except the [count] rule. The
     * rules are matched against given component only (not against its children).
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
        NotificationsKt.getText(self)
    }

    @Nullable
    static IconName getIconName(@NotNull Icon self) {
        return IconKt.getIconName(self)
    }

    static void setIconName(@NotNull Icon self, @Nullable IconName iconName) {
        IconKt.setIconName(self, iconName)
    }
}
