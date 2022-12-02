package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.TestPerson
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.gridpro.GridPro
import com.vaadin.flow.component.textfield.NumberField
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.gridProTestbatch() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    if (VaadinVersion.get.major >= 14) {
        // Only GridPro 2.0.0 has addCellEditStartedListener()
        test("CellEditStartedEvent") {
            val grid = GridPro<TestPerson>(TestPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("age")
            var listenerCalled = false
            grid.addCellEditStartedListener {
                listenerCalled = true
                expect(25) { it.item.age }
                expect(true) { it.isFromClient }
            }
            grid._fireCellEditStartedEvent(TestPerson("foo", 25), col.column)
            expect(true) { listenerCalled }
        }
    }

    group("item updater") {
        test("string + textfield") {
            val grid = GridPro<TestPerson>(TestPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("age")
                    .text { p, name -> p.name = name }
            val p = TestPerson("Foo", 45)
            grid._proedit(p) { col._text("Bar") }
            expect("Bar") { p.name }
        }
        if (VaadinVersion.get.major >= 14) {
            // Only GridPro 2.0.0 has custom fields
            test("int + custom field") {
                val grid = GridPro<TestPerson>(TestPerson::class.java)
                grid.removeAllColumns()
                val numberField = NumberField()
                val col: Grid.Column<TestPerson> = grid.addEditColumn("age")
                    .custom(numberField) { p, age -> p.age = age.toInt() }
                val p = TestPerson("Foo", 45)
                numberField.value = 3.15
                grid._proedit(p) { col._customFlush() }
                expect(3) { p.age }
            }
        }
        test("boolean + checkbox") {
            val grid = GridPro<TestProPerson>(TestProPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("alive")
                    .checkbox { p, alive -> p.alive = alive }
            val p = TestProPerson("Foo", 45)
            grid._proedit(p) { col._checkbox(false) }
            expect(false) { p.alive }
        }
        test("select + enum") {
            val grid = GridPro<TestProPerson>(TestProPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("status")
                    .select( { p: TestProPerson, status: MaritalStatus -> p.status = status }, MaritalStatus::class.java)
            val p = TestProPerson("Foo", 45)
            grid._proedit(p) { col._select(MaritalStatus.Divorced) }
            expect(MaritalStatus.Divorced) { p.status }
        }
    }
}

enum class MaritalStatus { Single, Married, Divorced, Widowed }

data class TestProPerson(var name: String, var age: Int, var alive: Boolean = true, var status: MaritalStatus = MaritalStatus.Single)
