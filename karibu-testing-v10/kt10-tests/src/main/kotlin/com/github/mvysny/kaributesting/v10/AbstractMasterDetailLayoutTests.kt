package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractMasterDetailLayoutTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test
    fun smoke() {
        MasterDetailLayout()
    }

    @Test
    fun `detail component lookup works`() {
        val m = MasterDetailLayout()
        m.detail = TextField()
        m._expectOne<TextField>()
    }
}
