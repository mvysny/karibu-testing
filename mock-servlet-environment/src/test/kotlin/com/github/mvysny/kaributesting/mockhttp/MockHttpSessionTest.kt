package com.github.mvysny.kaributesting.mockhttp

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.cloneBySerialization
import com.github.mvysny.dynatest.expectList
import java.io.Serializable
import javax.servlet.http.HttpSession
import kotlin.test.expect

class MockHttpSessionTest : DynaTest({
    lateinit var session: HttpSession
    beforeEach { session = MockHttpSession.create(MockContext()) }

    test("attributes") {
        expect(null) { session.getAttribute("foo") }
        expectList() { session.attributeNames.toList() }
        session.setAttribute("foo", "bar")
        expectList("foo") { session.attributeNames.toList() }
        expect("bar") { session.getAttribute("foo") }
        session.setAttribute("foo", null)
        expect(null) { session.getAttribute("foo") }
        expectList() { session.attributeNames.toList() }
        session.setAttribute("foo", "bar")
        expect("bar") { session.getAttribute("foo") }
        expectList("foo") { session.attributeNames.toList() }
        session.removeAttribute("foo")
        expect(null) { session.getAttribute("foo") }
        expectList() { session.attributeNames.toList() }
    }

    test("serializable") {
        session.setAttribute("foo", "bar")
        (session as Serializable).cloneBySerialization()
    }
})