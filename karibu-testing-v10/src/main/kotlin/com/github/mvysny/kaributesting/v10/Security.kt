package com.github.mvysny.kaributesting.v10

import java.security.Principal

/**
 * A simple [Principal] carrying a user name and a set of granted roles, suitable for
 * driving Vaadin's route security ([com.vaadin.flow.server.auth.AccessAnnotationChecker] /
 * [com.vaadin.flow.server.auth.NavigationAccessControl]) in a browserless test.
 *
 * You rarely construct this directly - [MockVaadin.login] creates one for you and wires it into
 * the faked request. Match a role against Vaadin's `@RolesAllowed("admin")` by putting `"admin"`
 * into [roles].
 */
public data class MockPrincipal @JvmOverloads constructor(
    private val userName: String,
    public val roles: List<String> = listOf()
) : Principal {
    override fun getName(): String = userName

    /**
     * Whether this principal has been granted [role]. Backs
     * [jakarta.servlet.http.HttpServletRequest.isUserInRole] via [MockVaadin.login].
     */
    public fun isUserInRole(role: String): Boolean = roles.contains(role)
}
