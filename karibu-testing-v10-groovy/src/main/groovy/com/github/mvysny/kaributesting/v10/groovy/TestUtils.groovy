package com.github.mvysny.kaributesting.v10.groovy

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@CompileStatic
class TestUtils {
    /**
     * Asserts that given block fails with given exception and optionally with given message.
     * @param expected the expected exception class. The exception thrown may also be a subtype.
     * @param msg optional substring which the exception message must contain.
     * @throws AssertionError if the block completed successfully or threw some other exception.
     * @return the exception thrown, so that you can assert on it.
     */
    @NotNull
    static <T extends Throwable> T expectThrows(@NotNull Class<? extends Throwable> expected,
                             @Nullable String msg = null,
                             @NotNull Closure block) {
        boolean succeeded = false
        T result = null
        try {
            block()
            succeeded = true
        } catch (Throwable e) {
            if (!expected.isInstance(e)) {
                throw new AssertionError("Expected to fail with ${expected.name} but failed with $e", e)
            }
            if (msg != null && !e.message.contains(msg)) {
                throw new AssertionError("${e.class.name} message: Expected '$msg' but was '${e.message}'", e)
            }
            result = e as T
        }
        if (succeeded) {
            throw new AssertionError("Expected to fail with ${expected.name} but completed successfully".toString() as Object)
        }
        result
    }
}
