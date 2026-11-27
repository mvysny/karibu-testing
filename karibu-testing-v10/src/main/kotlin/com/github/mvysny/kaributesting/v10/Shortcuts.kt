package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.ShortcutRegistration
import com.vaadin.flow.dom.Element
import com.vaadin.flow.internal.nodefeature.ElementListenerMap
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.BooleanNode
import tools.jackson.databind.node.ObjectNode
import java.lang.reflect.Method

/**
 * The event data of a faked `keydown` of [key]. Flow notifies a listener only when its filter
 * key in the event data is truthy (`DomEventListenerWrapper.matchesFilter()`), so every filter
 * lookup is answered by [filters]; any other key reads the plain event data.
 *
 * If this stuff stops working, place a breakpoint into [get]/[has] to see which keys
 * Flow asks for.
 */
private class MockKeydownEventData(key: Key, private val filters: ShortcutFilters) : ObjectNode(ObjectMapper().deserializationConfig().nodeFactory) {
    init {
        // so that KeyDownEvent can be created from it
        put("event.key", key.keys.first())
    }

    override fun has(key: String): Boolean = filters.matches(key) ?: super.has(key)

    override fun get(key: String): JsonNode? = filters.matches(key)?.let { BooleanNode.valueOf(it) } ?: super.get(key)
}

/**
 * Tells whether the filter of a shortcut's `keydown` listener passes for a key press.
 */
private fun interface ShortcutFilters {
    /**
     * @param dataKey a key Flow looks up in the event data.
     * @return null if [dataKey] is not the key of a filter Karibu can evaluate.
     */
    fun matches(dataKey: String): Boolean?

    companion object {
        private val mgenerateEventKeyFilter: Method? = ShortcutRegistration::class.java.declaredMethods
            .firstOrNull { it.name == "generateEventKeyFilter" }
            ?.apply { isAccessible = true }
        private val mgenerateEventModifierFilter: Method? = ShortcutRegistration::class.java.declaredMethods
            .firstOrNull { it.name == "generateEventModifierFilter" }
            ?.apply { isAccessible = true }
        private val mgetSnippetEntries: Method? = ElementListenerMap::class.java.methods
            .firstOrNull { it.name == "getSnippetEntries" }

        /**
         * Picks the filters of the Flow version on the classpath.
         * @param target the element the `keydown` is fired on.
         */
        fun of(target: Element, key: Key, modifiers: Set<Key>): ShortcutFilters = when {
            mgenerateEventKeyFilter != null && mgenerateEventModifierFilter != null ->
                expressionFilters(key, modifiers)
            mgetSnippetEntries != null -> snippetFilters(target, key, modifiers)
            else -> throw IllegalStateException("Unsupported Vaadin version: ShortcutRegistration has neither generateEventKeyFilter() nor ElementListenerMap.getSnippetEntries()")
        }

        /**
         * Flow 25.3 and older: a filter is a JavaScript expression, the key and the
         * modifier filter followed by optional side effects. The expression is its own data key.
         */
        private fun expressionFilters(key: Key, modifiers: Set<Key>): ShortcutFilters {
            val filter = mgenerateEventKeyFilter!!.invoke(null, key).toString() + " && " +
                    mgenerateEventModifierFilter!!.invoke(null, modifiers).toString()
            return ShortcutFilters { dataKey ->
                if (!dataKey.startsWith("([")) return@ShortcutFilters null
                val probeFilter = dataKey.removeSuffix(" && (event.stopPropagation() || true)")
                    .removeSuffix(" && (event.preventDefault() || true)")
                probeFilter.startsWith(filter)
            }
        }

        /**
         * Flow 25.4 and newer: a filter is a call of a bundled JavaScript function, with the
         * data key `@<snippet key>`. The call of `ShortcutFilterJs.matchKeydown()` is evaluated as
         * the client's `Shortcut.js` does; any other filter never passes, as there's no JavaScript to run it.
         */
        private fun snippetFilters(target: Element, key: Key, modifiers: Set<Key>): ShortcutFilters {
            val pressedModifiers: Set<String> = modifiers.map { it.keys.first() }.toSet()
            val calls: Map<String, Any> = (mgetSnippetEntries!!.invoke(target.node.getFeature(ElementListenerMap::class.java), "keydown") as Set<*>)
                .filterNotNull()
                .filter { it.call("filter") as Boolean }
                .associate { it.call("key") as String to it.call("call")!! }
            return ShortcutFilters { dataKey ->
                val call = calls[dataKey] ?: return@ShortcutFilters null
                val definitionType = call.call("definitionType") as Class<*>
                if (definitionType.name != "com.vaadin.flow.component.ShortcutRegistration\$ShortcutFilterJs" || call.call("methodName") != "matchKeydown") {
                    return@ShortcutFilters false
                }
                // The owner-scope guard (arguments 2 and 3) needs a DOM to look for open popovers in,
                // so it's skipped: the key press counts as made inside the shortcut owner's scope.
                val args = call.call("flattenArguments") as List<*>
                val requiredModifiers = (args[0] as String).split(' ').filter { it.isNotEmpty() }
                val forbiddenModifiers = (args[1] as String).split(' ').filter { it.isNotEmpty() }
                val keys = args.drop(7)
                key.keys.any { it in keys } && pressedModifiers.containsAll(requiredModifiers) &&
                        forbiddenModifiers.none { it in pressedModifiers }
            }
        }

        private fun Any.call(method: String): Any? = javaClass.getMethod(method).invoke(this)
    }
}

/**
 * Fires a shortcut event with given [key] and [modifiers] in the current UI.
 * This will in turn notify all components currently attached to the current UI
 * which subscribed for this exact key combination.
 */
public fun fireShortcut(key: Key, vararg modifiers: Key) {
    // keep the modifiers of type Key[] instead of KeyModifier[], otherwise
    // you won't be able to call those from Kotlin: https://github.com/vaadin/flow/issues/5051
    // and https://youtrack.jetbrains.com/issue/KT-35021
    currentUI._fireShortcut(key, *modifiers)
}

/**
 * Use the global `fireShortcut()` function unless you know what you're doing!
 */
public fun Component._fireShortcut(key: Key, vararg modifiers: Key) {
    // the shortcut registration is only updated in [UI.beforeClientResponse]; run the registration code now.
    MockVaadin.clientRoundtrip()

    // this will fire the "keydown" DOM event, which in turn fires KeyDownEvent event,
    // which in turn invokes the ShortcutListener.
    val modifierSet = modifiers.toSet()
    _fireDomEvent("keydown", MockKeydownEventData(key, ShortcutFilters.of(element, key, modifierSet)))
}
