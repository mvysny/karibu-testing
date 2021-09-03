package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.dom.Element
import com.vaadin.flow.internal.nodefeature.ElementListenerMap

/**
 * Fires a DOM [event] on this element.
 */
public fun Element._fireDomEvent(event: DomEvent) {
    node.getFeature(ElementListenerMap::class.java).fireEvent(event)
}

/**
 * Returns all components that are closest to [this] element.
 */
public fun Element._findComponents(): List<Component> {
    val components = mutableListOf<Component>()

    ComponentUtil.findComponents(this) {
        components.add(it)
    }

    return components
}
