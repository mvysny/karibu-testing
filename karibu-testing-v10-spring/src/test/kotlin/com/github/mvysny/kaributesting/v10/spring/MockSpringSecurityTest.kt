package com.github.mvysny.kaributesting.v10.spring

import com.github.mvysny.fakeservlet.FakeRequest
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.currentRequest
import com.vaadin.flow.server.auth.AccessAnnotationChecker
import com.vaadin.flow.server.auth.AnonymousAllowed
import jakarta.annotation.security.DenyAll
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.expect

/**
 * Verifies that [MockSpringSecurity.mock] bridges [SecurityContextHolder] onto the faked request, so
 * that [AccessAnnotationChecker] honors the currently-authenticated Spring Security user and roles.
 * Populating [SecurityContextHolder] directly is exactly what `@WithMockUser` does under the hood.
 */
class MockSpringSecurityTest {
    @BeforeEach fun setup() {
        MockSpringSecurity.mock()
        MockVaadin.setup()
    }

    @AfterEach fun teardown() {
        MockVaadin.tearDown()
        SecurityContextHolder.clearContext()
        // mockRequestFactory is a global that tearDown() doesn't reset; restore the default.
        MockVaadin.mockRequestFactory = { FakeRequest(it) }
    }

    /** Emulates `@WithMockUser(username = ..., roles = ...)`. */
    private fun login(username: String, vararg roles: String) {
        val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(username, "password", authorities)
    }

    @Test fun `anonymous user has no principal and only sees AnonymousAllowed`() {
        expect(null) { currentRequest.userPrincipal }
        expect(false) { AccessAnnotationChecker().hasAccess(DenyAllView::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(AnonymousView::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(PermitAllView::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(AdminView::class.java) }
    }

    @Test fun `logged-in user carries principal and passes PermitAll but not admin roles`() {
        login("user", "USER")
        expect("user") { currentRequest.userPrincipal.name }
        expect(true) { AccessAnnotationChecker().hasAccess(AnonymousView::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(PermitAllView::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(AdminView::class.java) }
    }

    @Test fun `admin role satisfies RolesAllowed`() {
        login("admin", "ADMIN")
        expect(true) { AccessAnnotationChecker().hasAccess(PermitAllView::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(AdminView::class.java) }
    }

    @Test fun `context is read lazily - login after setup is honored`() {
        expect(false) { AccessAnnotationChecker().hasAccess(AdminView::class.java) }
        login("admin", "ADMIN")
        expect(true) { AccessAnnotationChecker().hasAccess(AdminView::class.java) }
    }
}

@DenyAll internal class DenyAllView
@PermitAll internal class PermitAllView
@RolesAllowed("ADMIN") internal class AdminView
@AnonymousAllowed internal class AnonymousView
