import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.github.mvysny.kaributesting.v10.allTests
import com.github.mvysny.kaributesting.v10.jvmVersion
import com.vaadin.flow.server.VaadinService
import kotlin.test.expect

class AllTests : DynaTest({
    group("Vaadin env") {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("flow-build-info.json exists") {
            val res = Thread.currentThread().contextClassLoader.getResource("META-INF/VAADIN/config/flow-build-info.json")
            expect(true, "flow-build-info.json is not on the classpath!") { res != null }
        }

        test("Vaadin version") {
            expect(14) { VaadinMeta.version }
            expect(false) { VaadinMeta.isCompatibilityMode }
            expect(false) { VaadinService.getCurrent().deploymentConfiguration.isCompatibilityMode }
        }
    }

    if (jvmVersion < 13) {
        allTests()
    } else {
        // Sorry, no support for Vaadin 14+npm on Java 13 or higher: https://github.com/mvysny/karibu-testing/issues/29
    }
})
