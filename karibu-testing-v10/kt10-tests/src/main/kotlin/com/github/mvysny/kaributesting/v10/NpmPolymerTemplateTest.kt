@file:Suppress("DEPRECATION")

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
@DynaTestDsl
fun DynaNodeGroup.npmPolymerTemplateTestBatch(isModuleTest: Boolean) {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("UnloadableComponent works without any mocking") {
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
