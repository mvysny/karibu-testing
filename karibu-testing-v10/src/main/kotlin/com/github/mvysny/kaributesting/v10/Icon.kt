package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.IconName
import com.github.mvysny.kaributools.iconName
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon

/**
 * Returns the icon of this component. Only works with [Button] and [Icon], returns
 * null for everything else.
 */
public val Component._iconName: IconName? get() = when {
    this is Button -> icon?._iconName
    this is Icon -> iconName
    else -> null
}
