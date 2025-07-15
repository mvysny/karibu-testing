package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.markdown.Markdown
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractMarkdownTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test
    fun smoke() {
        val md = Markdown()
        UI.getCurrent().add(md)
        md.content = "# Hello world!"
    }
}
