package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.mockhttp.MockHttpSession
import com.github.mvysny.kaributesting.mockhttp.MockRequest
import com.github.mvysny.kaributesting.mockhttp.MockResponse
import com.vaadin.flow.component.UI
import com.vaadin.flow.internal.ReflectTools
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.QueryParameters
import com.vaadin.flow.server.*
import elemental.json.Json
import elemental.json.JsonArray
import elemental.json.JsonObject
import elemental.json.JsonValue
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.URL
import javax.servlet.Servlet
import javax.servlet.ServletContext
import kotlin.test.expect

public fun Serializable.serializeToBytes(): ByteArray = ByteArrayOutputStream().use { it -> ObjectOutputStream(it).writeObject(this); it }.toByteArray()
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
 * Parses the contents of given URL as a Json.
 */
internal fun URL.readJson(): JsonObject = Json.parse(readText())

/**
 * Adds a [value] at the end of the array.
 */
public fun JsonArray.add(value: JsonValue) {
    set(length(), value)
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

internal fun String.ellipsize(maxLength: Int, ellipsize: String = "..."): String = when {
    (length <= maxLength) || (length <= ellipsize.length) -> this
    else -> take(length - ellipsize.length) + ellipsize
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
 * Returns the singleton value associated with given [parameterName].
 * Returns null if there is no such parameter.
 * @throws IllegalStateException if the parameter has 2 or more values.
 */
public operator fun QueryParameters.get(parameterName: String): String? {
    val value = getValues(parameterName)
    return when {
        value.isEmpty() -> null
        value.size == 1 -> value.first()
        else -> throw IllegalStateException("Multiple values present for $parameterName: $value")
    }
}

/**
 * Returns the values associated with given [parameterName]. Returns an empty list
 * if there is no such parameter.
 */
public fun QueryParameters.getValues(parameterName: String): List<String> =
    parameters[parameterName] ?: listOf()

/**
 * Parses given query as a QueryParameters.
 * @param query the query string e.g. `foo=bar&quak=foo`; the parameters may repeat.
 */
public fun QueryParameters(query: String): QueryParameters = when {
    query.isBlank() -> QueryParameters.empty()
    else -> Location("?${query.trim('?')}").queryParameters
}
