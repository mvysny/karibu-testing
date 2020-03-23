package com.github.mvysny.kaributesting.v10.groovy

import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import static com.github.mvysny.kaributesting.v10.groovy.TestUtils.expectThrows
import static kotlin.test.AssertionsKt.expect

@CompileStatic
class TestUtilsTest {
    @Test
    void "expectThrows() - throwing expected exception succeeds"() {
        expectThrows(RuntimeException) { throw new RuntimeException("Expected") }
    }

    @Test
    void "expectThrows() - fails if block completes successfully"() {
        try {
            expectThrows(RuntimeException) {}
            // expected to be failed with AssertionError
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) {
            // okay
            expect("Expected to fail with java.lang.RuntimeException but completed successfully") { e.message }
            expect(null) { e.cause }
        }
    }

    @Test
    void "expectThrows() - fails if block throws something else"() {
        try {
            // this should fail with AssertionError since some other exception has been thrown
            expectThrows(RuntimeException) {
                throw new IOException("simulated")
            }
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) {
            // okay
            expect("Expected to fail with java.lang.RuntimeException but failed with java.io.IOException: simulated") { e.message }
            expect(IOException) { e.cause?.class }
        }
    }

    @Test
    void "expectThrows() - message - throwing expected exception succeeds"() {
        expectThrows(RuntimeException, "Expected") { throw new RuntimeException("Expected") }
    }

    @Test
    void "expectThrows() - message - fails if the message is different"() {
        try {
            expectThrows(RuntimeException, "foo") { throw new RuntimeException("actual") }
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) {
            // expected
            expect("java.lang.RuntimeException message: Expected 'foo' but was 'actual'") { e.message }
        }
    }

    @Test
    void "expectThrows() - message - fails if block completes successfully"() {
        try {
            expectThrows(RuntimeException, "foo") {}
            // expected to be failed with AssertionError
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) {
            // okay
            expect("Expected to fail with java.lang.RuntimeException but completed successfully") { e.message }
        }
    }

    @Test
    void "expectThrows() - message - fails if block throws something else"() {
        expectThrows(AssertionError, "Expected to fail with java.lang.RuntimeException but failed with java.io.IOException: simulated") {
            // this should fail with AssertionError since some other exception has been thrown
            expectThrows(RuntimeException, "simulated") {
                throw new IOException("simulated")
            }
        }
    }

    @Test
    void "expectThrows() - thrown exception attached as cause to the AssertionError"() {
        try {
            expectThrows(IOException, "foo") {
                throw new IOException("simulated")
            }
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) {
            // okay
            expect("java.io.IOException message: Expected 'foo' but was 'simulated'") { e.message }
            expect(IOException) { e.cause.class }
        }
    }

    @Test
    void "expectThrows() - AssertionError - throwing expected exception succeeds"() {
        expectThrows(AssertionError) { throw new AssertionError((Object) "Expected") }
    }

    @Test
    void "expectThrows() - AssertionError - fails if block completes successfully"() {
        try {
            expectThrows(AssertionError) {}
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) { /*okay*/
        }
    }

    @Test
    void "expectThrows() - AssertionError - fails if block throws something else"() {
        try {
            // this should fail with AssertionError since some other exception has been thrown
            expectThrows(AssertionError) {
                throw new IOException("simulated")
            }
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) { /*okay*/
        }
    }

    @Test
    void "expectThrows() - fails on unexpected message"() {
        try {
            // this should fail with AssertionError since some other exception has been thrown
            expectThrows(IOException, "expected") {
                throw new IOException("simulated")
            }
            throw new RuntimeException("Should have failed")
        } catch (AssertionError e) { /*okay*/
        }
    }
}
