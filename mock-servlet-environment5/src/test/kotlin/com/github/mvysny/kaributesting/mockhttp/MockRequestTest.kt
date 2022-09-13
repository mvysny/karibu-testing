package com.github.mvysny.kaributesting.mockhttp

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import kotlin.test.expect

/**
 * @author mavi
 */
class MockRequestTest : DynaTest({
    lateinit var request: MockRequest
    beforeEach { request = MockRequest(MockHttpSession.create(MockContext())) }

    test("attributes") {
        expect(null) { request.getAttribute("foo") }
        request.setAttribute("foo", "bar")
        expect("bar") { request.getAttribute("foo") }
        request.setAttribute("foo", null)
        expect(null) { request.getAttribute("foo") }
        request.setAttribute("foo", "bar")
        expect("bar") { request.getAttribute("foo") }
        request.removeAttribute("foo")
        expect(null) { request.getAttribute("foo") }
    }

    test("parameters") {
        expect(null) { request.getParameter("foo") }
        expectList() { request.parameterNames.toList() }
        expect(null) { request.getParameterValues("foo") }
        request.setParameter("foo", "bar")
        expect("bar") { request.getParameter("foo") }
        expectList("foo") { request.parameterNames.toList() }
        expectList("bar") { request.getParameterValues("foo")!!.toList() }
        request.parameters["foo"] = arrayOf("bar", "baz")
        expect("bar") { request.getParameter("foo") }
        expectList("foo") { request.parameterNames.toList() }
        expectList("bar", "baz") { request.getParameterValues("foo")!!.toList() }
    }

    test("getSession(false) returns the old invalid session") {
        val session = request.session as MockHttpSession
        expect(true) { session.isValid }
        session.setAttribute("foo", "bar")
        session.invalidate()
        expect(session) { request.getSession(false) }
        expect(false) { session.isValid }
    }

    test("getSession(true) creates a new session when invalidated") {
        var session = request.session as MockHttpSession
        expect(true) { session.isValid }
        session.setAttribute("foo", "bar")
        session.invalidate()
        expect(false) { session.isValid }
        expect(true) { session != request.getSession(true) }
        session = request.getSession(true) as MockHttpSession
        expect(true) { session.isValid }
        expect(null) { session.getAttribute("foo") }
    }

    test("getSession() creates a new session when invalidated") {
        var session = request.session as MockHttpSession
        expect(true) { session.isValid }
        session.setAttribute("foo", "bar")
        session.invalidate()
        expect(false) { session.isValid }
        expect(true) { session != request.session }
        session = request.session as MockHttpSession
        expect(true) { session.isValid }
        expect(null) { session.getAttribute("foo") }
    }

    test("principal") {
        expect(null) { request.userPrincipal }
        request.userPrincipalInt = MockPrincipal("foo")
        expect(MockPrincipal("foo")) { request.userPrincipal }
    }

    test("isUserInRole") {
        expect(false) { request.isUserInRole("foo") }
        request.userPrincipalInt = MockPrincipal("foo")
        expect(false) { request.isUserInRole("foo") }
        request.userPrincipalInt = MockPrincipal("foo", listOf("foo"))
        expect(false) { request.isUserInRole("foo") }
        request.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        expect(true) { request.isUserInRole("foo") }
    }
})
