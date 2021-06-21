package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Anchor

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

public var Anchor._href: String
    get() {
        var hr: String = href
        if (VaadinMeta.version >= 21) {
            // work around https://github.com/vaadin/flow/issues/10924
            val disabledHrefField =
                Anchor::class.java.getDeclaredField("disabledHref")
            disabledHrefField.isAccessible = true
            val disabledHref = disabledHrefField.get(this) as String?
            if (disabledHref != null) {
                hr = disabledHref
            }
        }
        return hr
    }
    set(value) {
        href = value
    }
