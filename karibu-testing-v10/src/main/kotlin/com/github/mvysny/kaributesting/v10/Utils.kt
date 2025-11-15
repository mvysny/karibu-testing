package com.github.mvysny.kaributesting.v10

import com.github.mvysny.fakeservlet.FakeHttpSession
import com.github.mvysny.fakeservlet.FakeRequest
import com.github.mvysny.fakeservlet.FakeResponse
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.ExtendedClientDetails
import com.vaadin.flow.internal.ReflectTools
import com.vaadin.flow.router.AccessDeniedException
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.server.*
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import jakarta.servlet.Servlet
import jakarta.servlet.ServletContext
import java.lang.reflect.Constructor
import kotlin.test.expect

public fun Serializable.serializeToBytes(): ByteArray = ByteArrayOutputStream().use { ObjectOutputStream(it).writeObject(this); it }.toByteArray()
public inline fun <reified T: Serializable> ByteArray.deserialize(): T = ObjectInputStream(inputStream()).readObject() as T
public inline fun <reified T: Serializable> T.serializeDeserialize(): T = serializeToBytes().deserialize<T>()

public val IntRange.size: Int get() = (endInclusive + 1 - start).coerceAtLeast(0)

/**
 * Expects that [actual] list of objects matches [expected] list of objects. Fails otherwise.
 */
public fun <T> expectList(vararg expected: T, actual: ()->List<T>) {
    expect(expected.toList(), actual)
}

/**
 * Returns the major JVM version, e.g. 6 for Java 1.6, 8 for Java 8, 11 for Java 11 etc.
 */
public val jvmVersion: Int get() = System.getProperty("java.version").parseJvmVersion()

/**
 * Returns the major JVM version, 1 for 1.1, 2 for 1.2, 3 for 1.3, 4 for 1.4, 5
 * for 1.5 etc.
 */
internal fun String.parseJvmVersion(): Int {
    // taken from https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
    val version: String = removePrefix("1.").takeWhile { it.isDigit() }
    return version.toInt()
}

internal fun String.ellipsize(maxLength: Int, ellipsize: String = "..."): String {
    require(maxLength >= ellipsize.length) { "maxLength must be at least the size of ellipsize $ellipsize but it was $maxLength" }
    return when {
        (length <= maxLength) || (length <= ellipsize.length) -> this
        else -> take(maxLength - ellipsize.length) + ellipsize
    }
}

/**
 * For a class implementing the [HasErrorParameter] interface, determines the type of
 * the exception handled (the type of `T`). Returns null if the Class doesn't implement the
 * [HasErrorParameter] interface.
 */
internal fun Class<*>.getErrorParameterType(): Class<*>? =
        ReflectTools.getGenericInterfaceType(this, HasErrorParameter::class.java)

internal val Class<*>.isRouteNotFound: Boolean
    get() = getErrorParameterType() == NotFoundException::class.java
internal val Class<*>.isAccessDenied: Boolean
    get() = getErrorParameterType() == AccessDeniedException::class.java

public val currentRequest: VaadinRequest
    get() = VaadinService.getCurrentRequest()
            ?: throw IllegalStateException("No current request. Have you called MockVaadin.setup()?")
public val currentResponse: VaadinResponse
    get() = VaadinService.getCurrentResponse()
            ?: throw IllegalStateException("No current response. Have you called MockVaadin.setup()?")
public val currentSession: VaadinSession
    get() = VaadinSession.getCurrent()
        ?: throw IllegalStateException("No current session. Have you called MockVaadin.setup()?")
public val currentService: VaadinService
    get() = VaadinService.getCurrent()
        ?: throw IllegalStateException("No current service. Have you called MockVaadin.setup()?")

/**
 * Returns the [UI.getCurrent]; fails with informative error message if the UI.getCurrent() is null.
 */
public val currentUI: UI
    get() = UI.getCurrent()
            ?: throw IllegalStateException("UI.getCurrent() is null. Have you called MockVaadin.setup()?")

/**
 * Retrieves the mock request which backs up [VaadinRequest].
 * ```
 * currentRequest.mock.addCookie(Cookie("foo", "bar"))
 * ```
 */
@Deprecated("replaced by 'fake'", replaceWith = ReplaceWith("fake"))
public val VaadinRequest.mock: FakeRequest get() = fake

/**
 * Retrieves the mock request which backs up [VaadinResponse].
 * ```
 * currentResponse.mock.getCookie("foo").value
 * ```
 */
@Deprecated("replaced by 'fake'", replaceWith = ReplaceWith("fake"))
public val VaadinResponse.mock: FakeResponse get() = fake

/**
 * Retrieves the mock session which backs up [VaadinSession].
 * ```
 * VaadinSession.getCurrent().mock
 * ```
 */
@Deprecated("replaced by 'fake'", replaceWith = ReplaceWith("fake"))
public val VaadinSession.mock: FakeHttpSession get() = fake

/**
 * Retrieves the mock request which backs up [VaadinRequest].
 * ```
 * currentRequest.fake.addCookie(Cookie("foo", "bar"))
 * ```
 */
public val VaadinRequest.fake: FakeRequest get() = (this as VaadinServletRequest).request as FakeRequest

/**
 * Retrieves the mock request which backs up [VaadinResponse].
 * ```
 * currentResponse.fake.getCookie("foo").value
 * ```
 */
public val VaadinResponse.fake: FakeResponse get() = (this as VaadinServletResponse).response as FakeResponse

/**
 * Retrieves the mock session which backs up [VaadinSession].
 * ```
 * VaadinSession.getCurrent().fake
 * ```
 */
public val VaadinSession.fake: FakeHttpSession get() = (session as WrappedHttpSession).httpSession as FakeHttpSession

public val VaadinContext.context: ServletContext get() = (this as VaadinServletContext).context

public val Servlet.isInitialized: Boolean get() = servletConfig != null

/**
 * Checks whether the class overrides [Object.toString]. If yes, it's expected that
 * the overriden version provides an informative insight into the object state.
 */
internal fun Class<*>.hasCustomToString(): Boolean =
    getMethod("toString").declaringClass != Object::class.java

/**
 * Creates new [ExtendedClientDetails].
 *
 * @param screenWidth
 *            Screen width
 * @param screenHeight
 *            Screen height
 * @param windowInnerWidth
 *            Window width
 * @param windowInnerHeight
 *            Window height
 * @param bodyClientWidth
 *            Body element width
 * @param bodyClientHeight
 *            Body element height
 * @param tzOffset
 *            TimeZone offset in minutes from GMT
 * @param rawTzOffset
 *            raw TimeZone offset in minutes from GMT (w/o DST adjustment)
 * @param dstShift
 *            the difference between the raw TimeZone and DST in minutes
 * @param dstInEffect
 *            is DST currently active in the region or not?
 * @param tzId
 *            time zone id
 * @param curDate
 *            the current date in milliseconds since the epoch
 * @param touchDevice
 *            whether browser responds to touch events
 * @param devicePixelRatio
 *            the ratio of the display's resolution in physical pixels to
 *            the resolution in CSS pixels
 * @param windowName
 *            a unique browser window name which persists on reload
 * @param navigatorPlatform
 *            navigation platform received from the browser
 */
public fun createExtendedClientDetails(
    screenWidth: String = "1920",
    screenHeight: String = "1080",
    windowInnerWidth: String = "1846",
    windowInnerHeight: String = "939",
    bodyClientWidth: String = "1846",
    bodyClientHeight: String = "939",
    tzOffset: String = "10800000",
    rawTzOffset: String = "7200000",
    dstShift: String = "3600000",
    dstInEffect: String = "true",
    tzId: String = "Europe/Helsinki",
    curDate: String? = null,
    touchDevice: String = "false",
    devicePixelRatio: String = "1.0",
    windowName: String = "ROOT-2521314-0.2626611481",
    navigatorPlatform: String = "Linux x86_64"
): ExtendedClientDetails {
    val constructor: Constructor<*> =
        ExtendedClientDetails::class.java.declaredConstructors[0]
    constructor.isAccessible = true
    val ecd: ExtendedClientDetails = constructor.newInstance(
        screenWidth, screenHeight, windowInnerWidth, windowInnerHeight,
        bodyClientWidth, bodyClientHeight,
        tzOffset, rawTzOffset, dstShift, dstInEffect, tzId,
        curDate, touchDevice, devicePixelRatio, windowName, navigatorPlatform
    ) as ExtendedClientDetails
    return ecd
}
