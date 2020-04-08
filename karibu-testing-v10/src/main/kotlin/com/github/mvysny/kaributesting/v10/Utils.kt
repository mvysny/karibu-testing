package com.github.mvysny.kaributesting.v10

import elemental.json.Json
import elemental.json.JsonArray
import elemental.json.JsonObject
import elemental.json.JsonValue
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URL
import kotlin.test.expect

fun Serializable.serializeToBytes(): ByteArray = ByteArrayOutputStream().use { it -> ObjectOutputStream(it).writeObject(this); it }.toByteArray()
inline fun <reified T: Serializable> ByteArray.deserialize(): T = ObjectInputStream(inputStream()).readObject() as T
inline fun <reified T: Serializable> T.serializeDeserialize() = serializeToBytes().deserialize<T>()

val IntRange.size: Int get() = (endInclusive + 1 - start).coerceAtLeast(0)

/**
 * Expects that [actual] list of objects matches [expected] list of objects. Fails otherwise.
 */
fun <T> expectList(vararg expected: T, actual: ()->List<T>) = expect(expected.toList(), actual)

/**
 * Parses the contents of given URL as a Json.
 */
internal fun URL.readJson(): JsonObject = Json.parse(readText())

/**
 * Adds a [value] at the end of the array.
 */
fun JsonArray.add(value: JsonValue) {
    set(length(), value)
}

private fun JsonObject.put(key: String, value: Any) {
    when (value) {
        is JsonValue -> put(key, value)
        is String -> put(key, value)
        is Double -> put(key, value)
        is Boolean -> put(key, value)
        else -> throw IllegalArgumentException("Unsupported value type ${value.javaClass} for $value")
    }
}

internal fun jsonCreateObject(vararg contents: Pair<String, Any>): JsonObject = Json.createObject().apply {
    contents.forEach { put(it.first, it.second) }
}

/**
 * Makes a [Field] non-final.
 *
 * Contains dangerous reflection into Java [Field] which may not work with all Java VMs.
 */
internal fun Field.makeNotFinal() {
    if (!isFinal) return
    // from https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
    val modifiersField: Field = try {
        Field::class.java.getDeclaredField("modifiers")
    } catch (ex: NoSuchFieldException) {
        if (jvmVersion >= 12) {
            throw RuntimeException("Unfortunately Karibu-Testing cannot hook into the NpmTemplateParser.INSTANCE field on Java 12 or higher; see https://github.com/mvysny/karibu-testing/issues/31 for more details.", ex)
        } else {
            throw ex
        }
    }
    modifiersField.isAccessible = true
    modifiersField.setInt(this, modifiers and Modifier.FINAL.inv())
}

val Field.isFinal: Boolean get() = (modifiers and Modifier.FINAL) != 0

/**
 * Returns the major JVM version, e.g. 6 for Java 1.6, 8 for Java 8, 11 for Java 11 etc.
 */
val jvmVersion: Int get() = System.getProperty("java.version").parseJvmVersion()

internal fun String.parseJvmVersion(): Int {
    // taken from https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
    val version: String = removePrefix("1.").takeWhile { it.isDigit() }
    return version.toInt()
}
