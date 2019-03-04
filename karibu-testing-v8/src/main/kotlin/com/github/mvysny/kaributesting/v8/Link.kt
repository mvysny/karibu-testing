package com.github.mvysny.kaributesting.v8

import com.vaadin.server.ConnectorResource
import com.vaadin.server.ExternalResource
import com.vaadin.server.Resource
import com.vaadin.server.ThemeResource
import com.vaadin.ui.Link
import com.vaadin.ui.UI

/**
 * Navigates to where this router link points to.
 * @throws IllegalArgumentException if the link was not visible, not enabled. See [checkEditableByUser] for
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

fun Link._download(): ByteArray {
    checkEditableByUser()
    return download()
}

fun Link.download(): ByteArray {
    val res: Resource = resource ?: throw AssertionError("${toPrettyString()}: resource is null")
    return when (res) {
        is ThemeResource -> res.download()
        is ConnectorResource -> res.download()
        else -> throw AssertionError("")
    }
}

fun Resource.download(): ByteArray = when (this) {
    is ThemeResource -> download()
    is ConnectorResource -> download()
    else -> throw AssertionError("Cannot download from resource $this: unsupported type of resource")
}

fun ThemeResource.download(): ByteArray {
    val theme = UI.getCurrent().theme!!

}

/**
 * Downloads the contents of this [ConnectorResource]
 */
fun ConnectorResource.download(): ByteArray = stream.stream.use { it.readBytes() }
