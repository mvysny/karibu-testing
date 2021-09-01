package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.dom.Element
import com.vaadin.flow.dom.ElementUtil
import com.vaadin.flow.internal.StateNode
import com.vaadin.flow.internal.nodefeature.ElementListenerMap
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * Fires a DOM [event] on this element.
 */
public fun Element._fireDomEvent(event: DomEvent) {
    node.getFeature(ElementListenerMap::class.java).fireEvent(event)
}

/**
 * Gets the element mapped to the given state node.
 */
public val StateNode._element: Element get() = Element.get(this)

/**
 * Returns all virtual child elements added via [Element.appendVirtualChild].
 */
public fun Element.getVirtualChildren(): List<Element> {
    if (node.hasFeature(VirtualChildrenList::class.java)) {
        val virtualChildrenList: VirtualChildrenList? =
            node.getFeatureIfInitialized(VirtualChildrenList::class.java)
                .orElse(null)
        if (virtualChildrenList != null) {
            return virtualChildrenList.iterator().asSequence().map { it._element } .toList()
        }
    }
    return listOf()
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
