@file:Suppress("FunctionName")

package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.server.AbstractStreamResource
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.server.StreamResourceRegistry
import com.vaadin.flow.server.VaadinSession
import java.io.ByteArrayOutputStream
import java.lang.reflect.Field
import java.net.URI
import kotlin.test.expect

/**
 * Downloads contents of this link. Only works with [StreamResource]; all other resources
 * are rejected with [AssertionError].
 * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
 * more details.
 */
fun Anchor._download(): ByteArray {
    checkEditableByUser()
    return download()
}

/**
 * Downloads contents of this link. Only works with [StreamResource]; all other resources
 * are rejected with [AssertionError].
 */
fun Anchor.download(): ByteArray {
    val uri = href
    expect(false, "href hasn't been set for ${this.toPrettyString()}") { uri.isNullOrBlank() }
    return downloadResource(uri)
}

/**
 * Downloads contents of this image. Only works with [StreamResource]; all other resources
 * are rejected with [AssertionError].
 */
fun Image.download(): ByteArray {
    val uri = src
    expect(false, "src hasn't been set for ${this.toPrettyString()}") { uri.isNullOrBlank() }
    return downloadResource(uri)
}

/**
 * Downloads [StreamResource] with given [uri] and returns it as a [ByteArray]. Only works with [StreamResource]; all other resources
 * are rejected with [AssertionError].
 */
fun downloadResource(uri: String): ByteArray {
    require(!uri.isBlank()) { "uri is blank" }
    val s: AbstractStreamResource? = VaadinSession.getCurrent().resourceRegistry.getResource(URI(uri)).orElse(null)
    expect(true, "No such StreamResource registered: '$uri'. Available resources: ${VaadinSession.getCurrent().resourceRegistry.resources.keys}") {
        s != null
    }
    expect(true, "Only StreamResources are supported but got $s") { s is StreamResource }
    val bout = ByteArrayOutputStream()
    (s as StreamResource).writer.accept(bout, VaadinSession.getCurrent())
    return bout.toByteArray()
}

private val resField: Field = StreamResourceRegistry::class.java.getDeclaredField("res").apply { isAccessible = true }

/**
 * Retrieves current list of resources mappings from this registry.
 */
@Suppress("UNCHECKED_CAST")
val StreamResourceRegistry.resources: Map<URI, AbstractStreamResource> get() = resField.get(this) as Map<URI, AbstractStreamResource>
