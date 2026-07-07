package test.app

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.*
import java.lang.RuntimeException

/**
 * Having an app-custom [NotFoundException] handler should not crash the mocking process:
 * https://github.com/mvysny/karibu-testing/issues/50
 */
class MyRouteNotFoundError : Div(), HasErrorParameter<NotFoundException> {
    override fun setErrorParameter(event: BeforeEnterEvent, parameter: ErrorParameter<NotFoundException>): Int {
        throw RuntimeException(parameter.exception)
    }
}
