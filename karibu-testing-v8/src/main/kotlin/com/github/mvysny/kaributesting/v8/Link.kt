@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.server.ExternalResource
import com.vaadin.server.Resource
import com.vaadin.ui.Link
import com.vaadin.ui.UI

/**
 * Navigates to where this router link points to.
 * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
 * more details.
 */
public fun Link._click() {
    checkEditableByUser()
    click()
}

/**
 * Navigates to where this router link points to. The difference to [_click] is that this one doesn't check whether
 * the link is actually visible and enabled.
 *
 * This only works for links with the [Link.getResource] containing [ExternalResource] which points to a View.
 */
public fun Link.click() {
    val res: Resource = resource ?: throw AssertionError("${toPrettyString()}: resource is null")
    if (res !is ExternalResource) throw AssertionError("${toPrettyString()}: resource $res is not ExternalResource")
    val url = res.url
    UI.getCurrent().navigator.navigateTo(url)
}
