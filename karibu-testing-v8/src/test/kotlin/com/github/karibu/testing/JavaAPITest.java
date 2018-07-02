package com.github.karibu.testing;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

import org.junit.jupiter.api.Test;

/**
 * Tests that the {@link MockVaadin} is usable from Java API as well: https://github.com/mvysny/karibu-testing/issues/3
 */
public class JavaAPITest {
    private static class MockUI extends UI {
        @Override
        protected void init(VaadinRequest request) {
        }
    }

    @Test
    public void testSetup() {
        MockVaadin.setup(MockUI::new);
    }
}
