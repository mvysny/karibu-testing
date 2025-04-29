package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.littemplate.LitTemplate
import com.vaadin.flow.component.template.Id
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.data.binder.Binder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * @param isModuleTest if true then this test run simulates a jar reusable component.
 */
abstract class AbstractLitTemplateTests(val isModuleTest: Boolean) {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `test loading of stuff from node_modules`() {
        assumeFalse(isModuleTest)
        val f = LitColorPickerField()
        expect(true) { f.foo._isVisible }
        UI.getCurrent().add(f)
    }

    @Test fun `loading from frontend`() {
        UI.getCurrent().add(MyTest())
        UI.getCurrent().add(MyTest2())
    }

    @Test fun `proper error message on unloadable component`() {
        // this actually works?!? okay...
        LitUnloadableComponent()
        LitUnloadableComponent2()
    }

    @Test fun `proper error message on unloadable template`() {
        expectThrows(RuntimeException::class, "Can't load template sources for <non-existent3> ./non-existent.js. Please:") {
            // mockGetSourcesFromTemplate() should fail with an informative error message; stacktrace:
//            at com.github.mvysny.kaributesting.v10.mock.MockNpmTemplateParser$Companion.mockGetSourcesFromTemplate(MockNpmTemplateParser.kt:78)
//            at com.github.mvysny.kaributesting.v10.mock.MockLitTemplateParserImpl.getSourcesFromTemplate(MockInstantiator.kt:75)
//            at com.vaadin.flow.component.littemplate.internal.LitTemplateParserImpl.getTemplateContent(LitTemplateParserImpl.java:115)
//            at com.vaadin.flow.component.littemplate.LitTemplateDataAnalyzer.parseTemplate(LitTemplateDataAnalyzer.java:75)
            LitUnloadableTemplate()
        }
        expectThrows(RuntimeException::class, "load template sources for <non-existent4> @foo/non-existent.js. Please") {
            LitUnloadableTemplate2()
        }
    }

    @Test fun form() {
        UI.getCurrent().add(MyForm())
        UI.getCurrent().add(MyForm3())
    }

    @Nested inner class includeVirtualChildrenInTemplates() {
        @AfterEach fun resetConfig() { includeVirtualChildrenInTemplates = false }
        @Test fun `false`() {
            val form = MyForm()
            expect(setOf(form)) { form._find<Component>().toSet() }
            form._expectNone<EmailField>()
        }
        @Test fun `true`() {
            includeVirtualChildrenInTemplates = true
            val form = MyForm()
            expect(setOf(form, form.emailField, form.firstNameField, form.lastNameField)) { form._find<Component>().toSet() }
            expect(form.emailField) { form._get<EmailField>() }
        }

        @Nested inner class CompositeTests {
            inner class CompositeForm : Composite<MyForm>()
            @Test fun `false`() {
                val form = CompositeForm()
                expect(setOf(form, form.content)) { form._find<Component>().toSet() }
                form._expectNone<EmailField>()
            }
            @Test fun `true`() {
                includeVirtualChildrenInTemplates = true
                val form = CompositeForm()
                expect(setOf(form, form.content, form.content.emailField, form.content.firstNameField, form.content.lastNameField)) { form._find<Component>().toSet() }
                expect(form.content.emailField) { form._get<EmailField>() }
            }
        }
    }
}

@Tag("color-picker-field")
@NpmPackage(value = "@appreciated/color-picker-field", version = "2.0.0-beta.5")
@JsModule("@appreciated/color-picker-field/src/color-picker-field.js")
class LitColorPickerField : LitTemplate() {
    @Id
    lateinit var foo: TextField
}

@Tag("non-existent")
@JsModule("./non-existent.js")
class LitUnloadableComponent : Component()

@Tag("non-existent2")
@JsModule("@foo/non-existent.js")
class LitUnloadableComponent2 : Component()

@Tag("non-existent3")
@JsModule("./non-existent.js")
class LitUnloadableTemplate : LitTemplate()

@Tag("non-existent4")
@JsModule("@foo/non-existent.js")
class LitUnloadableTemplate2 : LitTemplate()

data class Employee(var firstName: String? = null, var lastName: String? = null, var email: String? = null)

@Tag("my-form")
@JsModule("./src/my-form.ts")
class MyForm : LitTemplate() {
    @Id
    lateinit var firstNameField: TextField

    @Id
    lateinit var lastNameField: TextField

    @Id
    lateinit var emailField: EmailField
    val binder: Binder<Employee> = BeanValidationBinder(Employee::class.java)

    init {
        binder.forField(firstNameField).bind("firstName")
        binder.forField(lastNameField).bind("lastName")
        binder.forField(emailField).bind("email")
    }
}

@Tag("my-test-element")
@JsModule("./src/my-test-element.js")
class MyTest(prop1: String = "") : Component() {
    init {
        element.setProperty("prop1", prop1)
    }
}

@Tag("my-form2")
@JsModule("./src/my-form2.ts")
class MyForm3 : LitTemplate() {
    @Id
    lateinit var firstNameField: TextField

    @Id
    lateinit var lastNameField: TextField

    @Id
    lateinit var emailField: EmailField
    val binder: Binder<Employee> = BeanValidationBinder(Employee::class.java)

    init {
        binder.forField(firstNameField).bind("firstName")
        binder.forField(lastNameField).bind("lastName")
        binder.forField(emailField).bind("email")
    }
}

@Tag("my-test-element2")
@JsModule("./src/my-test-element2.js")
class MyTest2(prop1: String = "") : Component() {
    init {
        element.setProperty("prop1", prop1)
    }
}
