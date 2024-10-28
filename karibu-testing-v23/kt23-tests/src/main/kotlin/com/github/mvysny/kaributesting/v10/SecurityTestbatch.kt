@file:Suppress("DEPRECATION", "removal")

package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.UI
import com.vaadin.flow.server.auth.AccessAnnotationChecker
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.server.auth.ViewAccessChecker
import org.myapp.AdminView
import org.myapp.LoginView
import java.security.Principal
import jakarta.annotation.security.DenyAll
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * Tests that we can mock `HttpServletRequest.isUserInRole()` and `HttpServletRequest.getUserPrincipal()`
 * in order for [AccessAnnotationChecker] to work properly.
 */
abstract class AbstractSecurityTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun smoke() {
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestDenyAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAnonymous::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestPermitAll::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestAdminOnly::class.java) }
    }

    @Test fun user() {
        currentRequest.fake.userPrincipalInt = MockPrincipal("user", listOf("user"))
        currentRequest.fake.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestDenyAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAnonymous::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestPermitAll::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestAdminOnly::class.java) }
    }

    @Test fun admin() {
        currentRequest.fake.userPrincipalInt = MockPrincipal("admin", listOf("admin"))
        currentRequest.fake.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestDenyAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAnonymous::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestPermitAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAdminOnly::class.java) }
    }

    @Test fun `navigation honors ViewAccessChecker`() {
        MockVaadin.tearDown()
        MockVaadin.setup(Routes().apply {
            routes.add(LoginView::class.java)
            routes.add(AdminView::class.java)
        })
        val checker = ViewAccessChecker()
        checker.enable()
        checker.setLoginView(LoginView::class.java)
        UI.getCurrent().addBeforeEnterListener(checker)
        navigateTo<AdminView>()
        _expectOne<LoginView>()

        currentRequest.fake.userPrincipalInt = MockPrincipal("admin", listOf("admin"))
        currentRequest.fake.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        navigateTo<AdminView>()
        _expectOne<AdminView>()
    }
}

@DenyAll
internal class SecurityTestDenyAll

@PermitAll
internal class SecurityTestPermitAll

@RolesAllowed("admin")
internal class SecurityTestAdminOnly

@AnonymousAllowed
internal class SecurityTestAnonymous

internal data class MockPrincipal(private val name: String, val allowedRoles: List<String> = listOf()) :
    Principal {
    override fun getName(): String = name

    fun isUserInRole(role: String): Boolean = allowedRoles.contains(role)
}
