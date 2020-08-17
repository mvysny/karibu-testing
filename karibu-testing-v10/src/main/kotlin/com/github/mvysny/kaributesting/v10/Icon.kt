package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import java.io.Serializable

/**
 * Represents an icon name and a collection from which the icon came.
 */
public data class IconName(val collection: String, val name: String) : Serializable {
    init {
        require(collection.isNotBlank()) { "$collection: collection is blank" }
        require(name.isNotBlank()) { "$name: name is blank" }
    }

    /**
     * Creates a new [Icon] component for this icon.
     */
    public fun toIcon(): Icon = Icon(collection, name)

    /**
     * Checks whether this icon represents a [VaadinIcon]. Use [asVaadinIcon] to obtain
     * the original icon.
     */
    public val isVaadinIcon: Boolean get() = collection == "vaadin"

    /**
     * If this icon represents a [VaadinIcon], return the appropriate [VaadinIcon]
     * constant, else return null.
     */
    public fun asVaadinIcon(): VaadinIcon? {
        if (!isVaadinIcon) {
            return null
        }
        val enumName: String = name.toUpperCase().replace('-', '_')
        return VaadinIcon.valueOf(enumName)
    }

    /**
     * Returns this icon name as a string in the format `collection:name`.
     */
    override fun toString(): String = "$collection:$name"

    public companion object {
        /**
         * Gets the icon name from given [vaadinIcon].
         */
        public fun of(vaadinIcon: VaadinIcon): IconName =
                IconName("vaadin", vaadinIcon.name.toLowerCase().replace('_', '-'))
    }
}

/**
 * Returns the icon name and collection from the [Icon] component. Returns null
 * if no icon is set.
 */
public var Icon.iconName: IconName?
    get() {
        val icon = element.getAttribute("icon")
        val iconPair = icon.split(':')
        if (iconPair.size == 2) {
            return IconName(iconPair[0], iconPair[1])
        }
        return null
    }
    set(value) {
        element.setAttribute("icon", value?.toString() ?: "")
    }
