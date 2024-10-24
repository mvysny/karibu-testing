package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.router.*
import elemental.json.Json
import org.junit.jupiter.api.Nested
import java.io.Serializable
import java.util.concurrent.Callable
import kotlin.test.Test
import kotlin.test.expect

/**
 * Tests the `Utils.kt` set of utility functions.
 * @author mavi
 */
class UtilsTest {
    @Nested inner class serializeDeserialize() {
        @Test fun primitives() {
            expect("foo") { "foo".serializeDeserialize() }
            expect(false) { false.serializeDeserialize() }
        }
    }

    @Nested inner class JsonTests() {
        @Nested inner class unwrap() {
            @Test fun primitives() {
                expect(false) { Json.create(false).unwrap() }
                expect("foo") { Json.create("foo").unwrap() }
                expect(null) { Json.createNull().unwrap() }
                expect(4.0) { Json.create(4.0).unwrap() }
            }
            @Nested inner class array() {
                @Test fun empty() {
                    expectList() { Json.createArray().unwrap() as List<*> }
                }
                @Test fun `populated with primitives`() {
                    val a = Json.createArray()
                    a.set(0, false)
                    a.set(1, 1.0)
                    a.set(2, "hello")
                    a.set(3, Json.createNull())
                    expectList(false, 1.0, "hello", null) { a.unwrap() as List<*> }
                }
            }
        }
        @Nested inner class `JsonArray-toList` {
            @Test fun empty() {
                expectList() { Json.createArray().toList() }
            }
            @Test fun primitives() {
                val a = Json.createArray()
                a.set(0, false)
                a.set(1, 1.0)
                a.set(2, "hello")
                a.set(3, Json.createNull())
                expectList(false, 1.0, "hello", null) { a.toList().unwrap() }
            }
        }
        @Test fun `JsonArray-add`() {
            val a = Json.createArray()
            a.add(true)
            a.add(2.0)
            a.add("hello")
            a.add(Json.createNull())
            expectList(true, 2.0, "hello", null) { a.toList().unwrap() }
        }
    }

    @Nested inner class `jvm version` {
        @Test fun smoke() {
            println("JVM Version: $jvmVersion")
        }
        @Test fun parse() {
            expect(6) { "1.6.0_23".parseJvmVersion() }
            expect(7) { "1.7.0".parseJvmVersion() }
            expect(7) { "1.7.0_80".parseJvmVersion() }
            expect(8) { "1.8.0_211".parseJvmVersion() }
            expect(9) { "9.0.1".parseJvmVersion() }
            expect(11) { "11.0.4".parseJvmVersion() }
            expect(12) { "12".parseJvmVersion() }
            expect(12) { "12.0.1".parseJvmVersion() }
        }
    }

    @Test fun isRouteNotFound() {
        expect(false) { Any().javaClass.isRouteNotFound }
        expect(false) { InternalServerError::class.java.isRouteNotFound }
        expect(true) { RouteNotFoundError::class.java.isRouteNotFound }
        expect(true) { MockRouteNotFoundError::class.java.isRouteNotFound }

        class Foo : Serializable, Callable<Unit>, HasErrorParameter<NotFoundException>, Runnable {
            override fun setErrorParameter(event: BeforeEnterEvent?, parameter: ErrorParameter<NotFoundException>?): Int = 0
            override fun call() {}
            override fun run() {}
        }

        expect(true) { Foo::class.java.isRouteNotFound }
    }

    @Test fun ellipsize() {
        expect("a") { "a".ellipsize(3) }
        expect("a") { "a".ellipsize(4) }
        expect("a") { "a".ellipsize(100) }
        expect("...") { "aaaaaa".ellipsize(3) }
        expect("a...") { "aaaaaa".ellipsize(4) }
        expect("aa...") { "aaaaaa".ellipsize(5) }
        expect("aaaaaa") { "aaaaaa".ellipsize(6) }
        expect("aaaaaa") { "aaaaaa".ellipsize(7) }
        expect("aaaaaa") { "aaaaaa".ellipsize(100) }
    }

    @Test fun hasCustomToString() {
        expect(true) { String::class.java.hasCustomToString() }
        expect(false) { Object::class.java.hasCustomToString() }
        class Hello()
        expect(false) { Hello::class.java.hasCustomToString() }
        class Hello2() {
            override fun toString(): String = "Hello2()"
        }
        expect(true) { Hello2::class.java.hasCustomToString() }
    }
}
