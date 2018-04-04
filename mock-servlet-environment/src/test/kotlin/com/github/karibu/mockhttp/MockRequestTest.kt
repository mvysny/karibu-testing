package com.github.karibu.mockhttp

import com.github.mvysny.dynatest.DynaTest
import javax.servlet.http.HttpServletRequest
import kotlin.test.expect

/**
 * @author mavi
 */
class MockRequestTest : DynaTest({
    lateinit var request: HttpServletRequest
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
})
