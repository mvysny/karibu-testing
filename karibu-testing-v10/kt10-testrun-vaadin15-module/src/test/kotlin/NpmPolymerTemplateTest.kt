import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.jvmVersion
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule

class NpmPolymerTemplateTest : DynaTest({
    if (jvmVersion < 12) {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("UnloadableComponent works without any mocking") {
            UnloadableComponent()
        }
    } else {
        println("Sorry, no support for Vaadin 14+npm on Java 12 or higher: https://github.com/mvysny/karibu-testing/issues/31")
    }
})

/**
 * Still loads and works with KT (even though the JS file is missing on the FS).
 * The reason is that only PolymerTemplate-based components actually attempt to parse the js file server-side.
 */
@Tag("non-existent2")
@JsModule("./non-existent.js")
class UnloadableComponent : Component()
