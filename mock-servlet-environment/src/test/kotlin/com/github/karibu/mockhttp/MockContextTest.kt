package com.github.karibu.mockhttp

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import kotlin.test.expect

class MockContextTest : DynaTest({
    lateinit var ctx: ServletContext
    beforeEach { ctx = MockContext() }

    test("attributes") {
        expect(null) { ctx.getAttribute("foo") }
        expectList() { ctx.attributeNames.toList() }
        ctx.setAttribute("foo", "bar")
        expect("bar") { ctx.getAttribute("foo") }
        expectList("foo") { ctx.attributeNames.toList() }
        ctx.setAttribute("foo", null)
        expect(null) { ctx.getAttribute("foo") }
        expectList() { ctx.attributeNames.toList() }
        ctx.setAttribute("foo", "bar")
        expectList("foo") { ctx.attributeNames.toList() }
        expect("bar") { ctx.getAttribute("foo") }
        ctx.removeAttribute("foo")
        expectList() { ctx.attributeNames.toList() }
        expect(null) { ctx.getAttribute("foo") }
    }

    test("init parameters") {
        expect(null) { ctx.getInitParameter("foo") }
        expectList() { ctx.initParameterNames.toList() }
        expect(true) { ctx.setInitParameter("foo", "bar") }
        expectList("foo") { ctx.initParameterNames.toList() }
        expect("bar") { ctx.getInitParameter("foo") }
        expect(false) { ctx.setInitParameter("foo", "baz") }
        expect("bar") { ctx.getInitParameter("foo") }
        expectList("foo") { ctx.initParameterNames.toList() }
    }
})
