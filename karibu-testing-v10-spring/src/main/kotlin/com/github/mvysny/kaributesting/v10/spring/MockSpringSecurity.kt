package com.github.mvysny.kaributesting.v10.spring

import com.github.mvysny.fakeservlet.FakeRequest
import com.github.mvysny.kaributesting.v10.MockVaadin
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.security.Principal

/**
 * Bridges Spring Security into Karibu's faked servlet environment.
 *
 * Spring Security Test's `@WithMockUser`, `@WithUserDetails` and `@WithSecurityContext` populate
 * [SecurityContextHolder] before the test body runs; in production a servlet filter
 * (`SecurityContextHolderAwareRequestWrapper`) then exposes that context via
 * [jakarta.servlet.http.HttpServletRequest.getUserPrincipal] and
 * [jakarta.servlet.http.HttpServletRequest.isUserInRole], which is what Vaadin's route security
 * ([com.vaadin.flow.server.auth.NavigationAccessControl] /
 * [com.vaadin.flow.server.auth.AccessAnnotationChecker]) reads. Karibu runs no filter, so
 * [mock] installs a [FakeRequest] that reads the same two values straight from [SecurityContextHolder].
 */
public object MockSpringSecurity {
    /**
     * Makes the faked Vaadin request source its user principal and roles from Spring Security's
     * [SecurityContextHolder], so that `@WithMockUser` (and friends) drive Vaadin route security the
     * same way they would in production. Call this **before** [MockVaadin.setup] — ideally from
     * a JUnit `@BeforeEach` method, since [MockVaadin.mockRequestFactory] is a global that
     * [MockVaadin.tearDown] does not reset.
     *
     * This does **not** log anyone in: you still establish the security context yourself, either via
     * a `@WithMockUser`-style annotation or by populating [SecurityContextHolder] manually.
     *
     * The context is read lazily on every call, so it doesn't matter whether the user is logged in
     * before or after [MockVaadin.setup], and login/logout mid-test is honored.
     *
     * @param rolePrefix prepended to the role name before matching it against the authenticated
     * user's authorities in [jakarta.servlet.http.HttpServletRequest.isUserInRole]. Defaults to
     * `"ROLE_"`, mirroring Spring's `GrantedAuthorityDefaults`: `@WithMockUser(roles = "ADMIN")`
     * yields the authority `ROLE_ADMIN`, matched against `@RolesAllowed("ADMIN")`. Pass `""` for
     * apps that gate on raw authorities (e.g. `@WithMockUser(authorities = "...")`).
     */
    @JvmStatic
    @JvmOverloads
    public fun mock(rolePrefix: String = "ROLE_") {
        // treats both null and AnonymousAuthenticationToken as "not logged in", exactly like the
        // real SecurityContextHolderAwareRequestWrapper does.
        val trustResolver = AuthenticationTrustResolverImpl()
        MockVaadin.mockRequestFactory = { session ->
            val request = object : FakeRequest(session) {
                override fun getUserPrincipal(): Principal? =
                    SecurityContextHolder.getContext().authentication
                        ?.takeUnless { trustResolver.isAnonymous(it) }
            }
            request.isUserInRole = { principal, role ->
                (principal as? Authentication)?.authorities.orEmpty()
                    .any { it.authority == rolePrefix + role }
            }
            request
        }
    }
}
