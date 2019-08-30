import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.github.mvysny.kaributesting.v10.allTests
import com.vaadin.flow.server.VaadinService
import kotlin.test.expect

class AllTests : DynaTest({
    group("Vaadin env") {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Vaadin version") {
            expect(14) { VaadinMeta.version }
            expect(false) { VaadinMeta.isCompatibilityMode }
            expect(false) { VaadinService.getCurrent().deploymentConfiguration.isCompatibilityMode }
        }
    }
    allTests()
})
