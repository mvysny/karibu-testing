package com.github.mvysny.kaributesting.mockhttp

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import kotlin.test.expect

class MockResponseTest : DynaTest({
    lateinit var request: MockResponse
    beforeEach { request = MockResponse(MockHttpSession.create(MockContext())) }

    test("headers") {
        expect(null) { request.getHeader("foo") }
        expectList() { request.headerNames.toList() }
        expectList() { request.getHeaders("foo").toList() }
        request.setHeader("foo", "bar")
        expect("bar") { request.getHeader("foo") }
        expectList("foo") { request.headerNames.toList() }
        expectList("bar") { request.getHeaders("foo").toList() }
        request.headers["foo"] = arrayOf("bar", "baz")
        expect("bar") { request.getHeader("foo") }
        expectList("foo") { request.headerNames.toList() }
        expectList("bar", "baz") { request.getHeaders("foo").toList() }
    }
})
