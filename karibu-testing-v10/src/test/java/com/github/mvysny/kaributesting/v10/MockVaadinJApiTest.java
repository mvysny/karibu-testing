package com.github.mvysny.kaributesting.v10;

import com.github.mvysny.fakeservlet.FakeRequest;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

import jakarta.servlet.http.Cookie;
import org.jetbrains.annotations.Nullable;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Not really a test, doesn't run any test methods. It just fails compilation if the MockVaadin API
 * is hard to use from Java. This is an API test.
 * @author mavi
 */
public class MockVaadinJApiTest {

    public MockVaadinJApiTest() {
        UtilsKt.getFake(VaadinRequest.getCurrent()).addCookie(new Cookie("foo", "bar"));
        assertEquals("bar", UtilsKt.getFake(VaadinResponse.getCurrent()).getCookie("foo").getValue());

        MockVaadin.setup(new Routes(), () -> {
            UtilsKt.getFake(VaadinRequest.getCurrent()).addCookie(new Cookie("foo", "bar"));
            return new LocatorJApiTest.MyUI();
        });
        MockVaadin.INSTANCE.setMockRequestFactory(session -> new FakeRequest(session) {
            @Override
            public @Nullable Principal getUserPrincipal() {
                return null; // SecurityContextHolder.getContext().getAuthentication();
            }
        });
    }
}
