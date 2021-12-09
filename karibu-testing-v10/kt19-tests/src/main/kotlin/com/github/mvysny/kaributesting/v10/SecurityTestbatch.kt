package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.vaadin.flow.server.auth.AccessAnnotationChecker
import com.vaadin.flow.server.auth.AnonymousAllowed
import java.security.Principal
import javax.annotation.security.DenyAll
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed
import kotlin.test.expect

fun DynaNodeGroup.securityTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke") {
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestDenyAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAnonymous::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestPermitAll::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestAdminOnly::class.java) }
    }

    test("user") {
        currentRequest.mock.userPrincipalInt = MockPrincipal("user", listOf("user"))
        currentRequest.mock.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestDenyAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAnonymous::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestPermitAll::class.java) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestAdminOnly::class.java) }
    }

    test("admin") {
        currentRequest.mock.userPrincipalInt = MockPrincipal("admin", listOf("admin"))
        currentRequest.mock.isUserInRole = { p, r -> (p as MockPrincipal).isUserInRole(r) }
        expect(false) { AccessAnnotationChecker().hasAccess(SecurityTestDenyAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAnonymous::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestPermitAll::class.java) }
        expect(true) { AccessAnnotationChecker().hasAccess(SecurityTestAdminOnly::class.java) }
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
