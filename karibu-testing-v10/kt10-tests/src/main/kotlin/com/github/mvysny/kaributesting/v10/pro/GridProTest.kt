package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.TestPerson
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.gridpro.GridPro
import com.vaadin.flow.component.textfield.NumberField
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractGridProTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun CellEditStartedEvent() {
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

    @Nested inner class `item updater` {
        @Test fun `string + textfield`() {
            val grid = GridPro<TestPerson>(TestPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("age")
                    .text { p, name -> p.name = name }
            val p = TestPerson("Foo", 45)
            grid._proedit(p) { col._text("Bar") }
            expect("Bar") { p.name }
        }
        @Test fun `int + custom field`() {
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
        @Test fun `boolean + checkbox`() {
            val grid = GridPro<TestProPerson>(TestProPerson::class.java)
            grid.removeAllColumns()
            val col = grid.addEditColumn("alive")
                    .checkbox { p, alive -> p.alive = alive }
            val p = TestProPerson("Foo", 45)
            grid._proedit(p) { col._checkbox(false) }
            expect(false) { p.alive }
        }
        @Test fun `select + enum`() {
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
