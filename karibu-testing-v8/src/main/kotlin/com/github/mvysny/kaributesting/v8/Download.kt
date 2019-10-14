@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v8

import com.vaadin.server.ConnectorResource
import com.vaadin.server.Resource
import com.vaadin.server.StreamResource
import com.vaadin.server.ThemeResource
import com.vaadin.ui.Image
import com.vaadin.ui.Link
import com.vaadin.ui.UI
import java.io.InputStream
import kotlin.test.expect

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
    val ui: UI = UI.getCurrent()!!
    val theme: String = currentTheme
    val input: InputStream = ui.session.service.getThemeResourceAsStream(ui, theme, resourceId)
            ?: throw AssertionError("No resource $resourceId found for theme $theme")
    return input.use { it.readBytes() }
}

/**
 * Downloads the contents of this [ConnectorResource].
 */
fun ConnectorResource.download(): ByteArray = stream.stream.use { it.readBytes() }

/**
 * Downloads contents of this image. Only works with [StreamResource]; all other resources
 * are rejected with [AssertionError].
 */
fun Image.download(): ByteArray {
    expect(true, "src hasn't been set for ${this.toPrettyString()}") { source != null }
    return source.download()
}
