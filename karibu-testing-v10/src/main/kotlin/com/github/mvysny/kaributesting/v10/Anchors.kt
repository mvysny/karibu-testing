package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Anchor
import java.lang.reflect.Field

/**
 * Navigates to where this router link points to. The difference to [_click] is that this one doesn't check whether
 * the link is actually visible and enabled.
 */
public fun Anchor._click() {
    checkEditableByUser()
    click()
}

/**
 * Navigates to where this router link points to. The difference to [_click] is that this one doesn't check whether
 * the link is actually visible and enabled.
 */
public fun Anchor.click() {
    UI.getCurrent().navigate(href)
}

/**
 * First "fix" for https://github.com/vaadin/flow/issues/10924 (the introduction
 * of "disabledHref") is now being reversed for another solution. So, if the field
 * exists, use it, otherwise just return the contents of [Anchor.getHref].
 */
private val _Anchor_disabledHref: Field? =
    Anchor::class.java.declaredFields.firstOrNull { it.name == "disabledHref" } ?.apply {
        isAccessible = true
    }

public var Anchor._href: String
    get() {
        var hr: String = href
        if (_Anchor_disabledHref != null) {
            val disabledHref = _Anchor_disabledHref.get(this) as String?
            if (disabledHref != null) {
                hr = disabledHref
            }
        }
        return hr
    }
    set(value) {
        href = value
    }
