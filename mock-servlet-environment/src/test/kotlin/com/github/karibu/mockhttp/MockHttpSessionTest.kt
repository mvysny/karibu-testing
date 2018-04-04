package com.github.karibu.mockhttp

import com.github.mvysny.dynatest.DynaTest
import javax.servlet.http.HttpSession
import kotlin.test.expect

class MockHttpSessionTest : DynaTest({
    lateinit var session: HttpSession
    beforeEach { session = MockHttpSession.create(MockContext()) }

    test("attributes") {
        expect(null) { session.getAttribute("foo") }
        session.setAttribute("foo", "bar")
        expect("bar") { session.getAttribute("foo") }
        session.setAttribute("foo", null)
        expect(null) { session.getAttribute("foo") }
        session.setAttribute("foo", "bar")
        expect("bar") { session.getAttribute("foo") }
        session.removeAttribute("foo")
        expect(null) { session.getAttribute("foo") }
    }
})