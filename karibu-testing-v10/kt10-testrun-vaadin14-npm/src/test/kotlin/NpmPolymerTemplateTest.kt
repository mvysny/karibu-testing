import com.github.appreciated.app.layout.component.applayout.LeftLayouts
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.jvmVersion

class NpmPolymerTemplateTest : DynaTest({
    if (jvmVersion < 13) {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Instantiate LeftHybrid") {
            LeftLayouts.LeftHybrid()
        }
    } else {
        // Sorry, no support for Vaadin 14+npm on Java 13 or higher: https://github.com/mvysny/karibu-testing/issues/29
    }
})
