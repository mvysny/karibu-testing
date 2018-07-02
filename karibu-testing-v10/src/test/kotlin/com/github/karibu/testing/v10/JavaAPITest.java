package com.github.karibu.testing.v10;

import org.junit.jupiter.api.Test;

/**
 * Tests that the {@link MockVaadin} is usable from Java API as well: https://github.com/mvysny/karibu-testing/issues/3
 */
public class JavaAPITest {
    @Test
    public void testSetup() {
        MockVaadin.setup(new Routes(), MockedUI::new, MockService::new);
    }
}
