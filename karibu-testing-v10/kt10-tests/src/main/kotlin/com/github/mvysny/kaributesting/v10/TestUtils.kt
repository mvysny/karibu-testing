package com.github.mvysny.kaributesting.v10

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintStream
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.test.assertFailsWith

/**
 * Prints everything to an in-memory buffer. Call [toString] to obtain what was printed.
 */
class InMemoryPrintStream(private val bout: ByteArrayOutputStream = ByteArrayOutputStream()) : PrintStream(bout, true, Charsets.UTF_8) {
    override fun toString(): String = String(bout.toByteArray(), Charsets.UTF_8)
}

/**
 * Expects that given block fails with an exception of type [clazz] (or its subtype).
 *
 * Note that this is different from [assertFailsWith] since this function
 * also asserts on [Throwable.message].
 * @param expectMessage optional substring which the [Throwable.message] must contain.
 * @throws AssertionError if the block completed successfully or threw some other exception.
 * @return the exception thrown, so that you can assert on it.
 */
fun <T: Throwable> expectThrows(clazz: KClass<out T>, expectMessage: String = "", block: ()->Unit): T {
    val ex = assertFailsWith(clazz, block)
    if (!(ex.message ?: "").contains(expectMessage)) {
        throw AssertionError("${clazz.javaObjectType.name} message: Expected '$expectMessage' but was '${ex.message}'", ex)
    }
    return ex
}

/**
 * Expects that given block fails with an exception of type [T] (or its subtype).
 *
 * Note that this is different from [assertFailsWith] since this function
 * also asserts on [Throwable.message].
 * @param expectMessage optional substring which the [Throwable.message] must contain.
 * @throws AssertionError if the block completed successfully or threw some other exception.
 * @return the exception thrown, so that you can assert on it.
 */
inline fun <reified T: Throwable> expectThrows(expectMessage: String = "", noinline block: ()->Unit): T =
    expectThrows(T::class, expectMessage, block)

/**
 * Serializes the object to a byte array
 * @return the byte array containing this object serialized form.
 */
fun Serializable?.serializeToBytes(): ByteArray = ByteArrayOutputStream().also { ObjectOutputStream(it).writeObject(this) }.toByteArray()

inline fun <reified T: Serializable> ByteArray.deserialize(): T? = T::class.java.cast(ObjectInputStream(inputStream()).readObject())

/**
 * Clones this object by serialization and returns the deserialized clone.
 * @return the clone of this
 */
fun <T : Serializable> T.cloneBySerialization(): T = javaClass.cast(serializeToBytes().deserialize())
