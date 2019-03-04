@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.annotations.Theme
import com.vaadin.server.*
import com.vaadin.ui.Link
import com.vaadin.ui.UI

/**
 * Navigates to where this router link points to.
 * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
 * more details.
 */
fun Link._click() {
    checkEditableByUser()
    click()
}

/**
 * Navigates to where this router link points to. The difference to [_click] is that this one doesn't check whether
 * the link is actually visible and enabled.
 *
 * This only works for links with the [Link.getResource] containing [ExternalResource] which points to a View.
 */
fun Link.click() {
    val res: Resource = resource ?: throw AssertionError("${toPrettyString()}: resource is null")
    if (res !is ExternalResource) throw AssertionError("${toPrettyString()}: resource $res is not ExternalResource")
    val url = res.url
    UI.getCurrent().navigator.navigateTo(url)
}

/**
 * Downloads contents of this link. Only works with [ThemeResource] and [ConnectorResource]; all other resources
 * are rejected with [AssertionError].
 * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
 * more details.
 */
fun Link._download(): ByteArray {
    checkEditableByUser()
    return download()
}

/**
 * Downloads contents of this link. Only works with [ThemeResource] and [ConnectorResource]; all other resources
 * are rejected with [AssertionError].
 */
fun Link.download(): ByteArray {
    val res: Resource = resource ?: throw AssertionError("${toPrettyString()}: resource is null")
    return res.download()
}

/**
 * Downloads contents of this resource. Only works with [ThemeResource] and [ConnectorResource]; all other resources
 * are rejected with [AssertionError].
 */
fun Resource.download(): ByteArray = when (this) {
    is ThemeResource -> download()
    is ConnectorResource -> download()
    else -> throw AssertionError("Cannot download from resource $this: unsupported type of resource")
}

/**
 * Downloads contents of this theme resource, from the `Vaadin/themes/$theme` folder.
 */
fun ThemeResource.download(): ByteArray {
    val ui = UI.getCurrent()!!
    val theme = currentTheme
    val input = ui.session.service.getThemeResourceAsStream(ui, theme, resourceId)
            ?: throw AssertionError("No resource $resourceId found for theme $theme")
    return input.use { it.readBytes() }
}

/**
 * Downloads the contents of this [ConnectorResource].
 */
fun ConnectorResource.download(): ByteArray = stream.stream.use { it.readBytes() }
