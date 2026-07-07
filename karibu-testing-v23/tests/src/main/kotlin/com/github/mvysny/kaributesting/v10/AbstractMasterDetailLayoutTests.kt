package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout
import org.junit.jupiter.api.Test

abstract class AbstractMasterDetailLayoutTests {
    @Test
    fun lookup() {
        val ml = MasterDetailLayout()
        ml.master = Span("Master")
        ml.detail = Span("Detail")
        ml._expectOne<Span> { text = "Master" }
        ml._expectOne<Span> { text = "Detail" }
    }
}