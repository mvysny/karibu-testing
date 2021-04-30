import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.VaadinMeta
import com.github.mvysny.kaributesting.v10.allTests
import com.github.mvysny.kaributesting.v10.jvmVersion
import com.vaadin.flow.server.VaadinService
import java.net.URL
import kotlin.test.expect

class AllTests : DynaTest({
    test("flow-build-info.json doesn't exist") {
        val res: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/VAADIN/config/flow-build-info.json")
        expect(null, "flow-build-info.json exists on the classpath!") { res }
    }

    group("Vaadin env") {
        beforeEach { MockVaadin.setup() }
        afterEach { MockVaadin.tearDown() }

        test("Vaadin version") {
            expect(20) { VaadinMeta.version }
            expect(false) { VaadinMeta.isCompatibilityMode }
        }
    }
    allTests(isModuleTest = true)
})
