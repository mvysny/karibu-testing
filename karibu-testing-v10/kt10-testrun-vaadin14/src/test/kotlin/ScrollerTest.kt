import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._expectNone
import com.github.mvysny.kaributesting.v10._expectOne
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.Scroller

class ScrollerTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("_get goes inside of Scroller") {
        val s = Scroller()
        s._expectNone<Span>()
        s.content = Span("Hello")
        s._expectOne<Span>()
    }
})
