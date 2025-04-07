package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.IconName
import com.github.mvysny.kaributools.iconName
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem
import com.vaadin.flow.component.icon.Icon
import kotlin.streams.asSequence

/**
 * Returns the icon of this component.
 * Only works with [Button], [Icon], [MenuItem], [GridMenuItem]
 * returns null for everything else.
 */
@Suppress("ObjectPropertyName")
public val Component._iconName: IconName? get() = when (this) {
    is Button -> icon?._iconName
    is Icon -> iconName
    is MenuItem -> children.asSequence().firstOrNull { it is Icon }?._iconName
    is GridMenuItem<*> -> children.asSequence().firstOrNull { it is Icon }?._iconName
    else -> null
}
