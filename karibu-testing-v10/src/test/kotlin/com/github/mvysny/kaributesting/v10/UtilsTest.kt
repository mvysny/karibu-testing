package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaTest
import com.vaadin.flow.router.*
import java.io.Serializable
import java.util.concurrent.Callable
import kotlin.test.expect

/**
 * @author mavi
 */
class UtilsTest : DynaTest({
    test("parse jvm version") {
        expect(6) { "1.6.0_23".parseJvmVersion() }
        expect(7) { "1.7.0".parseJvmVersion() }
        expect(7) { "1.7.0_80".parseJvmVersion() }
        expect(8) { "1.8.0_211".parseJvmVersion() }
        expect(9) { "9.0.1".parseJvmVersion() }
        expect(11) { "11.0.4".parseJvmVersion() }
        expect(12) { "12".parseJvmVersion() }
        expect(12) { "12.0.1".parseJvmVersion() }
    }

    test("isRouteNotFound") {
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

    test("ellipsize") {
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

    test("hasCustomToString") {
        expect(true) { String::class.java.hasCustomToString() }
        expect(false) { Object::class.java.hasCustomToString() }
        class Hello()
        expect(false) { Hello::class.java.hasCustomToString() }
        class Hello2() {
            override fun toString(): String = "Hello2()"
        }
        expect(true) { Hello2::class.java.hasCustomToString() }
    }
})
