package com.github.mvysny.kaributesting.v10

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Prints everything to an in-memory buffer. Call [toString] to obtain what was printed.
 */
class InMemoryPrintStream(private val bout: ByteArrayOutputStream = ByteArrayOutputStream()) : PrintStream(bout, true, Charsets.UTF_8) {
    override fun toString(): String = String(bout.toByteArray(), Charsets.UTF_8)
}
