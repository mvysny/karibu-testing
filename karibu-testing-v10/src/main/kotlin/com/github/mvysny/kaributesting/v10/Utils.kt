package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.internal.ReflectTools
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.NotFoundException
import elemental.json.Json
import elemental.json.JsonArray
import elemental.json.JsonObject
import elemental.json.JsonValue
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.URL
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
