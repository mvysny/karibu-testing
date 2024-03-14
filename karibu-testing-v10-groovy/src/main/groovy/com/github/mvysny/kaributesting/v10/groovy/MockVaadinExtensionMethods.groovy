package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.fakeservlet.FakeHttpSession
import com.github.mvysny.fakeservlet.FakeRequest
import com.github.mvysny.fakeservlet.FakeResponse
import com.github.mvysny.kaributesting.v10.UtilsKt
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinResponse
import com.vaadin.flow.server.VaadinSession
import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

@CompileStatic
class MockVaadinExtensionMethods {
    /**
     * Retrieves the mock request which backs up [VaadinRequest].
     * <pre>
     * currentRequest.mock.addCookie(Cookie("foo", "bar"))
     * </pre>
     */
    @NotNull
    static FakeRequest getMock(@NotNull VaadinRequest self) {
        UtilsKt.getMock(self)
    }

    /**
     * Retrieves the mock request which backs up [VaadinResponse].
     * <pre>
     * currentResponse.mock.getCookie("foo").value
     * </pre>
     */
    @NotNull
    static FakeResponse getMock(@NotNull VaadinResponse self) {
        UtilsKt.getMock(self)
    }

    /**
     * Retrieves the mock session which backs up [VaadinSession].
     * <pre>
     * VaadinSession.getCurrent().mock
     * </pre>
     */
    @NotNull
    static FakeHttpSession getMock(@NotNull VaadinSession self) {
        UtilsKt.getMock(self)
    }
}
