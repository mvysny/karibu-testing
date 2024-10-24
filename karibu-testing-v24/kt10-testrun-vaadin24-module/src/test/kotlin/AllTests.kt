import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.jvmVersion
import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.VaadinVersion
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
            expect(24) { VaadinVersion.get.major }
            expect(false) { VaadinMeta.isCompatibilityMode }
        }
    }
    allTests19(isModuleTest = true)
})

class AllTests24 {
    @Test fun `flow-build-info-json doesn't exist`() {
        val res: URL? = Thread.currentThread().contextClassLoader.getResource("META-INF/VAADIN/config/flow-build-info.json")
        expect(null, "flow-build-info.json exists on the classpath!") { res }
    }

    @Nested inner class VaadinEnv {
        @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
        @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

        @Test fun vaadinVersion() {
            expect(24) { VaadinVersion.get.major }
            expect(false) { VaadinMeta.isCompatibilityMode }
        }
    }

    @Nested inner class AllTests19 : AbstractAllTests19(true)
}
