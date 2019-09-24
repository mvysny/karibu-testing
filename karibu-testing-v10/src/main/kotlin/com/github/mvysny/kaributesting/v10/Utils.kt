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

internal fun Field.makeNotFinal() {
    val modifiersField = Field::class.java.getDeclaredField("modifiers").apply { isAccessible = true }
    modifiersField.setInt(this, modifiers and Modifier.FINAL.inv())
}
