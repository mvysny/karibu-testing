package com.github.mvysny.kaributesting.v8;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Not really a test, doesn't run any test methods. It just fails compilation if the MockVaadin API
 * is hard to use from Java. This is an API test.
 * @author mavi
 */
public class MockVaadinJApiTest {

    public MockVaadinJApiTest() {
        MockVaadinKt.getMock(VaadinRequest.getCurrent()).addCookie(new Cookie("foo", "bar"));
        assertEquals("bar", MockVaadinKt.getMock(VaadinResponse.getCurrent()).getCookie("foo").getValue());

        MockVaadin.setup(() -> {
            MockVaadinKt.getMock(VaadinRequest.getCurrent()).addCookie(new Cookie("foo", "bar"));
            return new LocatorJApiTest.MyUI();
        });
    }
}
