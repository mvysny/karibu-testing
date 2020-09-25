import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._isVisible
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.littemplate.LitTemplate
import com.vaadin.flow.component.polymertemplate.Id
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect

class LitTemplateTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("test loading of stuff from node_modules") {
        val f = LitColorPickerField()
        expect(true) { f.foo._isVisible }
    }

    test("proper error message on unloadable component") {
        // this actually works?!? okay...
        LitUnloadableComponent()
        LitUnloadableComponent2()
    }
})

@Tag("color-picker-field")
@NpmPackage(value = "@appreciated/color-picker-field", version = "2.0.0-beta.5")
@JsModule("@appreciated/color-picker-field/src/color-picker-field.js")
class LitColorPickerField : LitTemplate() {
    @Id
    lateinit var foo: TextField
}

@Tag("non-existent")
@JsModule("./non-existent.js")
class LitUnloadableComponent : LitTemplate()

@Tag("non-existent3")
@JsModule("@foo/non-existent.js")
class LitUnloadableComponent2 : LitTemplate()
