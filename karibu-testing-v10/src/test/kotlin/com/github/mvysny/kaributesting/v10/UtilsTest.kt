package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
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

    group("QueryParameters") {
        test("get") {
            expect(null) { QueryParameters.empty()["foo"] }
            expect("bar") { QueryParameters("foo=bar")["foo"] }
        }
        test("get fails with multiple parameters") {
            expectThrows(IllegalStateException::class, "Multiple values present for foo: [bar, baz]") {
                QueryParameters("foo=bar&foo=baz")["foo"]
            }
        }
        test("getValues") {
            expectList() { QueryParameters.empty().getValues("foo") }
            expectList("bar") { QueryParameters("foo=bar").getValues("foo") }
            expectList("bar", "baz") { QueryParameters("foo=bar&foo=baz").getValues("foo") }
        }
    }
})
