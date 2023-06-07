package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.mockhttp.MockHttpSession
import com.github.mvysny.kaributesting.mockhttp.MockRequest
import com.github.mvysny.kaributesting.mockhttp.MockResponse
import com.vaadin.flow.component.UI
import com.vaadin.flow.internal.ReflectTools
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.server.*
import elemental.json.*
import elemental.json.impl.JreJsonValue
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javax.servlet.Servlet
import javax.servlet.ServletContext
import kotlin.test.expect

public fun Serializable.serializeToBytes(): ByteArray = ByteArrayOutputStream().use { ObjectOutputStream(it).writeObject(this); it }.toByteArray()
public inline fun <reified T: Serializable> ByteArray.deserialize(): T = ObjectInputStream(inputStream()).readObject() as T
public inline fun <reified T: Serializable> T.serializeDeserialize(): T = serializeToBytes().deserialize<T>()

public val IntRange.size: Int get() = (endInclusive + 1 - start).coerceAtLeast(0)

/**
 * Expects that [actual] list of objects matches [expected] list of objects. Fails otherwise.
 */
public fun <T> expectList(vararg expected: T, actual: ()->List<T>) {
    expect(expected.toList(), actual)
}

/**
 * Adds a [value] at the end of the array.
 */
public fun JsonArray.add(value: JsonValue) {
    set(length(), value)
}

/**
 * Adds a [string] at the end of the array.
 */
public fun JsonArray.add(string: String) {
    add(Json.create(string))
}

/**
 * Adds a [bool] at the end of the array.
 */
public fun JsonArray.add(bool: Boolean) {
    add(Json.create(bool))
}

/**
 * Adds a [number] at the end of the array.
 */
public fun JsonArray.add(number: Double) {
    add(Json.create(number))
}

/**
 * Returns the contents of this array as an immutable [List]. The returned list
 * is a copy and doesn't reflect mutations done in the array.
 */
public fun JsonArray.toList(): List<JsonValue> =
    (0 until length()).map { get(it) }

/**
 * Unwraps all items in given list.
 */
public fun List<JsonValue>.unwrap(): List<Any?> =
    map { it.unwrap() }

/**
 * Unwraps this value into corresponding Java type. Unwraps arrays recursively.
 */
public fun JsonValue.unwrap(): Any? = when(this) {
    is JsonArray -> this.toList().unwrap()
    else -> (this as JreJsonValue).`object`
}

/**
 * Returns the major JVM version, e.g. 6 for Java 1.6, 8 for Java 8, 11 for Java 11 etc.
 */
public val jvmVersion: Int get() = System.getProperty("java.version").parseJvmVersion()

/**
 * Returns the major JVM version, 1 for 1.1, 2 for 1.2, 3 for 1.3, 4 for 1.4, 5
 * for 1.5 etc.
 */
internal fun String.parseJvmVersion(): Int {
    // taken from https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
    val version: String = removePrefix("1.").takeWhile { it.isDigit() }
    return version.toInt()
}

internal fun String.ellipsize(maxLength: Int, ellipsize: String = "..."): String {
    require(maxLength >= ellipsize.length) { "maxLength must be at least the size of ellipsize $ellipsize but it was $maxLength" }
    return when {
        (length <= maxLength) || (length <= ellipsize.length) -> this
        else -> take(maxLength - ellipsize.length) + ellipsize
    }
}

/**
 * For a class implementing the [HasErrorParameter] interface, determines the type of
 * the exception handled (the type of `T`). Returns null if the Class doesn't implement the
 * [HasErrorParameter] interface.
 */
internal fun Class<*>.getErrorParameterType(): Class<*>? =
        ReflectTools.getGenericInterfaceType(this, HasErrorParameter::class.java)

internal val Class<*>.isRouteNotFound: Boolean
    get() = getErrorParameterType() == NotFoundException::class.java

public val currentRequest: VaadinRequest
    get() = VaadinService.getCurrentRequest()
            ?: throw IllegalStateException("No current request. Have you called MockVaadin.setup()?")
public val currentResponse: VaadinResponse
    get() = VaadinService.getCurrentResponse()
            ?: throw IllegalStateException("No current response. Have you called MockVaadin.setup()?")

/**
 * Returns the [UI.getCurrent]; fails with informative error message if the UI.getCurrent() is null.
 */
public val currentUI: UI
    get() = UI.getCurrent()
            ?: throw IllegalStateException("UI.getCurrent() is null. Have you called MockVaadin.setup()?")

/**
 * Retrieves the mock request which backs up [VaadinRequest].
 * ```
 * currentRequest.mock.addCookie(Cookie("foo", "bar"))
 * ```
 */
public val VaadinRequest.mock: MockRequest get() = (this as VaadinServletRequest).request as MockRequest

/**
 * Retrieves the mock request which backs up [VaadinResponse].
 * ```
 * currentResponse.mock.getCookie("foo").value
 * ```
 */
public val VaadinResponse.mock: MockResponse get() = (this as VaadinServletResponse).response as MockResponse

/**
 * Retrieves the mock session which backs up [VaadinSession].
 * ```
 * VaadinSession.getCurrent().mock
 * ```
 */
public val VaadinSession.mock: MockHttpSession get() = (session as WrappedHttpSession).httpSession as MockHttpSession

public val VaadinContext.context: ServletContext get() = (this as VaadinServletContext).context

public val Servlet.isInitialized: Boolean get() = servletConfig != null

/**
 * Checks whether the class overrides [Object.toString]. If yes, it's expected that
 * the overriden version provides an informative insight into the object state.
 */
internal fun Class<*>.hasCustomToString(): Boolean =
    getMethod("toString").declaringClass != java.lang.Object::class.java
