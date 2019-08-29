import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.allTests
import com.github.mvysny.kaributesting.v10.vaadinVersion
import com.vaadin.flow.server.VaadinService
import kotlin.test.expect

class AllTests : DynaTest({
    group("Vaadin env") {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Vaadin version") {
            expect(14) { vaadinVersion }
            expect(true) { VaadinService.getCurrent().deploymentConfiguration.isCompatibilityMode }
        }
    }
    allTests()
})
