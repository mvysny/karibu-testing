package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.LocatorKt
import com.github.mvysny.kaributesting.v10.SearchSpec
import com.vaadin.flow.component.Component
import groovy.transform.CompileStatic
import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull

/**
 * @author mavi
 */
@CompileStatic
class LocatorExtensionMethods {
    /**
     * Finds a VISIBLE component of given type which matches given <code>block</code>.
     * This component and all of its descendants are searched.
     * <p></p>
     * Example of use:
     * <pre>
     * layout._get(Button, { caption = "Foo" })._click()
     * </pre>
     * @param block the search specification
     * @return the only matching component, never null.
     * @throws IllegalArgumentException if no component matched, or if more than one component matches.
     */
    @NotNull
    static <C extends Component> C _get(@NotNull Component self,
                                        @NotNull Class<C> clazz,
                                        @NotNull @DelegatesTo(type = "com.github.mvysny.kaributesting.v10.SearchSpec<C>", strategy = Closure.DELEGATE_FIRST) Closure spec = {}) {
        LocatorKt._get(self, clazz, new Function1<SearchSpec<C>, Unit>() {
            @Override
            Unit invoke(SearchSpec<C> tSearchSpec) {
                spec.resolveStrategy = Closure.DELEGATE_FIRST
                spec.delegate = tSearchSpec
                spec()
                return Unit.INSTANCE
            }
        } as Function1)
    }

    /**
     * Finds a list of VISIBLE components of given <code>clazz</code> which matches <code>block</code>.
     * This component and all of its descendants are searched.
     * <p></p>
     * Example of use:
     * <pre>
     * layout._find(Button, { caption = "Foo" })
     * </pre>
     * @return the list of matching components, may be empty.
     */
    @NotNull
    static <C extends Component> List<C> _find(@NotNull Component self,
                                               @NotNull Class<C> clazz,
                                               @NotNull @DelegatesTo(type = "com.github.mvysny.kaributesting.v10.SearchSpec<C>", strategy = Closure.DELEGATE_FIRST) Closure spec = {}) {
        LocatorKt._find(self, clazz, new Function1<SearchSpec<C>, Unit>() {
            @Override
            Unit invoke(SearchSpec<C> tSearchSpec) {
                spec.resolveStrategy = Closure.DELEGATE_FIRST
                spec.delegate = tSearchSpec
                spec()
                return Unit.INSTANCE
            }
        } as Function1)
    }

    /**
     * Expects that there are no VISIBLE components of given <code>clazz</code>
     * which matches <code>block</code>. This component and all of its descendants are searched.
     * <p></p>
     * Example of use:
     * <pre>
     * layout._expectNone(Button, { caption = "Foo" })
     * </pre>
     * @throws IllegalArgumentException if one or more components matched.
     */
    static <C extends Component> void _expectNone(@NotNull Component self,
                                                  @NotNull Class<C> clazz,
                                                  @NotNull @DelegatesTo(type = "com.github.mvysny.kaributesting.v10.SearchSpec<C>", strategy = Closure.DELEGATE_FIRST) Closure spec = {}) {
        LocatorKt._expectNone(self, clazz, new Function1<SearchSpec<C>, Unit>() {
            @Override
            Unit invoke(SearchSpec<C> tSearchSpec) {
                spec.resolveStrategy = Closure.DELEGATE_FIRST
                spec.delegate = tSearchSpec
                spec()
                return Unit.INSTANCE
            }
        } as Function1)
    }

    /**
     * Expects that there is exactly one VISIBLE components of given <code>clazz</code>
     * which matches <code>block</code>. This component and all of its descendants are searched.
     * <p></p>
     * Example of use:
     * <pre>
     * layout._expectOne(Button, { caption = "Foo" })
     * </pre>
     * @throws AssertionError if none, or more than one components matched.
     */
    static <C extends Component> void _expectOne(@NotNull Component self,
                                                 @NotNull Class<C> clazz,
                                                 @NotNull @DelegatesTo(type = "com.github.mvysny.kaributesting.v10.SearchSpec<C>", strategy = Closure.DELEGATE_FIRST) Closure spec = {}) {
        LocatorKt._expectOne(self, clazz, new Function1<SearchSpec<C>, Unit>() {
            @Override
            Unit invoke(SearchSpec<C> tSearchSpec) {
                spec.resolveStrategy = Closure.DELEGATE_FIRST
                spec.delegate = tSearchSpec
                spec()
                return Unit.INSTANCE
            }
        } as Function1)
    }

    /**
     * Expects that there are exactly <code>count</code> VISIBLE components of
     * given <code>clazz</code> matching <code>block</code>. This component and all of its descendants are searched. Examples:
     * <pre>
     * // check that there are 5 buttons in a button bar
     * buttonBar._expect(Button, 5)
     * // check that there are either 3, 4 or 5 vertical layouts in the UI with given class
     * _expect(VerticalLayout, { count = 3..5; styles = "menubar" })
     * </pre>
     * Special cases: for asserting one component use [_expectOne]. For asserting no components use [_expectNone].
     * They provide
     * a better error message in case of a failure, and also communicate your intent
     * as a test writer better in the test code.
     * @throws AssertionError if incorrect count of component matched.
     */
    static <C extends Component> void _expect(@NotNull Component self,
                                              @NotNull Class<C> clazz,
                                              int count = 1,
                                              @NotNull @DelegatesTo(type = "com.github.mvysny.kaributesting.v10.SearchSpec<C>", strategy = Closure.DELEGATE_FIRST) Closure spec = {}) {
        LocatorKt._expect(self, clazz, count, new Function1<SearchSpec<C>, Unit>() {
            @Override
            Unit invoke(SearchSpec<C> tSearchSpec) {
                spec.resolveStrategy = Closure.DELEGATE_FIRST
                spec.delegate = tSearchSpec
                spec()
                return Unit.INSTANCE
            }
        } as Function1)
    }
}
