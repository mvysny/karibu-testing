package com.github.mvysny.kaributesting.mockhttp

import com.github.mvysny.dynatest.DynaTest
import kotlin.test.expect

/**
 * @author mavi
 */
class SessionAttributeMapTest : DynaTest({
    lateinit var session: MockHttpSession
    lateinit var attrs: MutableMap<String, Any>
    beforeEach {
        session = MockHttpSession.create(MockContext())
        attrs = session.attributes
    }

    group("size") {
        test("Initially zero") {
            expect(0) { attrs.size }
        }
        test("put increases by 1") {
            attrs["foo"] = "bar"
            expect(1) { attrs.size }
        }
        test("setAttribute increases by 1") {
            session.setAttribute("foo", "bar")
            expect(1) { attrs.size }
        }
        test("clear sets size to 0") {
            attrs["foo"] = "bar"
            attrs.clear()
            expect(0) { attrs.size }
        }
    }

    group("is empty") {
        test("Initially true") {
            expect(true) { attrs.isEmpty() }
        }
        test("put makes map not empty") {
            attrs["foo"] = "bar"
            expect(false) { attrs.isEmpty() }
        }
        test("setAttribute makes map not empty") {
            session.setAttribute("foo", "bar")
            expect(false) { attrs.isEmpty() }
        }
        test("clear sets size to 0") {
            attrs["foo"] = "bar"
            attrs.clear()
            expect(true) { attrs.isEmpty() }
        }
    }

    group("get") {
        test("get from empty map returns null") {
            expect(null) { attrs["foo"] }
        }
        test("get non-existing key returns null") {
            session.setAttribute("foo", "bar")
            expect(null) { attrs["bar"] }
        }
        test("existing key retrieval") {
            session.setAttribute("foo", "bar")
            expect("bar") { attrs["foo"] }
        }
        test("get deleted key returns null") {
            session.setAttribute("foo", "bar")
            attrs.remove("foo")
            expect(null) { attrs["foo"] }
        }
    }

    group("remove") {
        test("remove from empty map does nothing") {
            expect(null) { attrs.remove("foo") }
            expect(true) { attrs.isEmpty() }
        }
        test("remove non-existing key does nothing") {
            attrs["bar"] = "foo"
            expect(null) { attrs.remove("foo") }
            expect("foo") { attrs["bar"] }
            expect("foo") { session.getAttribute("bar") }
        }
        test("remove existing key") {
            attrs["bar"] = "foo"
            expect("foo") { attrs.remove("bar") }
            expect(true) { attrs.isEmpty() }
            expect(null) { session.getAttribute("bar") }
        }
    }

    group("clear") {
        test("clear empty map does nothing") {
            attrs.clear()
            expect(true) { attrs.isEmpty() }
        }
        test("clear map with one key") {
            attrs["foo"] = "bar"
            attrs.clear()
            expect(true) { attrs.isEmpty() }
        }
        test("clear big map") {
            (0..1000).forEach { session.setAttribute(it.toString(), it) }
            expect(false) { attrs.isEmpty() }
            attrs.clear()
            expect(true) { attrs.isEmpty() }
            (0..1000).forEach {
                expect(null) { session.getAttribute(it.toString()) }
            }
        }
    }
})
