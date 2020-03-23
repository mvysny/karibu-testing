package com.github.mvysny.kaributesting.v10.groovy

import groovy.transform.CompileStatic

@CompileStatic
class TestUtils {
    static void expectThrows(Class<? extends Throwable> expected, String msg = null, Closure block) {
        boolean succeeded = false
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
        }
        if (succeeded) {
            throw new AssertionError("Expected to fail with ${expected.name} but completed successfully".toString() as Object)
        }
    }
}
