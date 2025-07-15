@file:Suppress("FunctionName", "DEPRECATION")

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
import kotlin.test.fail

/**
 * Downloads contents of this link.
 * @throws IllegalStateException if the link was not visible, not enabled. See [checkEditableByUser] for
 * more details.
 */
public fun Anchor._download(): ByteArray {
    _expectEditableByUser()
    return download()
}

/**
 * Downloads contents of this link.
 */
public fun Anchor.download(): ByteArray {
    val uri = href
    expect(
        false,
        "href hasn't been set for ${this.toPrettyString()}"
    ) { uri.isNullOrBlank() }
    return downloadResource(uri)
}

/**
 * Downloads contents of this image.
 */
public fun Image.download(): ByteArray {
    val uri = src
    expect(
        false,
        "src hasn't been set for ${this.toPrettyString()}"
    ) { uri.isNullOrBlank() }
    return downloadResource(uri)
}

/**
 * Downloads [AbstractStreamResource] with given [uri] and returns it as a [ByteArray].
 */
public fun downloadResource(uri: String): ByteArray {
    require(!uri.isBlank()) { "uri is blank" }
    val s: AbstractStreamResource? =
        VaadinSession.getCurrent().resourceRegistry.getResource(URI(uri))
            .orElse(null)
    expect(
        true,
        "No such StreamResource registered: '$uri'. Available resources: ${VaadinSession.getCurrent().resourceRegistry.resources.keys}"
    ) {
        s != null
    }
    if (s is StreamResource) {
        val bout = ByteArrayOutputStream()
        s.writer.accept(bout, VaadinSession.getCurrent())
        return bout.toByteArray()
    }
    // Vaadin 24.8 DownloadHandler
    if (s is StreamResourceRegistry.ElementStreamResource) {
        val req = MockVaadin.createVaadinRequest()
        val res = MockVaadin.createVaadinResponse()
        s.elementRequestHandler.handleRequest(req, res, currentSession, s.owner)
        return res.fake.buffer.toByteArray()
    }
    fail("Unsupported resource type: $s")
}

private val resField: Field =
    StreamResourceRegistry::class.java.getDeclaredField("res")
        .apply { isAccessible = true }

/**
 * Retrieves current list of resources mappings from this registry.
 */
@Suppress("UNCHECKED_CAST")
public val StreamResourceRegistry.resources: Map<URI, AbstractStreamResource>
    get() =
        resField.get(this) as Map<URI, AbstractStreamResource>
