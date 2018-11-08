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
})
