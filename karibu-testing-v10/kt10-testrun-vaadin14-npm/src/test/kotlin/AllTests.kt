import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.allTests
import com.github.mvysny.kaributesting.v10.vaadinVersion
import kotlin.test.expect

class AllTests : DynaTest({
    test("Vaadin version") {
        expect(14) { vaadinVersion }
    }
    allTests()
})
