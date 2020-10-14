package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.mockhttp.MockHttpSession
import com.github.mvysny.kaributesting.mockhttp.MockRequest
import com.github.mvysny.kaributesting.mockhttp.MockResponse
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
    static MockRequest getMock(@NotNull VaadinRequest self) {
        UtilsKt.getMock(self)
    }

    /**
     * Retrieves the mock request which backs up [VaadinResponse].
     * <pre>
     * currentResponse.mock.getCookie("foo").value
     * </pre>
     */
    @NotNull
    static MockResponse getMock(@NotNull VaadinResponse self) {
        UtilsKt.getMock(self)
    }

    /**
     * Retrieves the mock session which backs up [VaadinSession].
     * <pre>
     * VaadinSession.getCurrent().mock
     * </pre>
     */
    @NotNull
    static MockHttpSession getMock(@NotNull VaadinSession self) {
        UtilsKt.getMock(self)
    }
}
