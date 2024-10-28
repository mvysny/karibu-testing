package com.github.mvysny.kaributesting.v10

import java.io.ByteArrayOutputStream
import java.io.PrintStream
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
    // tests for this function are present in the dynatest-engine project
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
