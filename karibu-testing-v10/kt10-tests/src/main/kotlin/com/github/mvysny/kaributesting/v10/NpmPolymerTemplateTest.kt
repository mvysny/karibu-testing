@file:Suppress("DEPRECATION")
package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
abstract class AbstractNpmPolymerTemplateTests(val isModuleTest: Boolean) {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun smoke() {
        UnloadableComponent()
    }
}

/**
 * Still loads and works with KT (even though the JS file is missing on the FS).
 * The reason is that only PolymerTemplate-based components actually attempt to parse the js file server-side.
 */
@Tag("non-existent2")
@JsModule("./non-existent.js")
class UnloadableComponent : Component()
