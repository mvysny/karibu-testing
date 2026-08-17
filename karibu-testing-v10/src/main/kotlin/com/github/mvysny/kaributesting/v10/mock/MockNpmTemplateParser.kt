package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.mock.MockNpmTemplateParser.Companion.customLoaders
import java.io.File
import java.lang.RuntimeException
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Mock-loads Lit Templates. For `./`-prefixed [com.vaadin.flow.component.dependency.JsModule]
 * URLs (templates belonging to the app itself or to a jar add-on), the sources are looked up in:
 * * [customLoaders]
 * * the local filesystem: `frontend/` and `src/main/frontend/`
 * * the classpath: `META-INF/frontend/` and then `META-INF/resources/frontend/`
 *
 * Both classpath locations are supported by Vaadin, but the recommendation changed between
 * Vaadin 24 and Vaadin 25: Vaadin 24's
 * [Loading Resources](https://vaadin.com/docs/24/flow/advanced/loading-resources) told add-ons
 * to ship frontend files in `src/main/resources/META-INF/resources/frontend/`, while
 * [Vaadin 25's](https://vaadin.com/docs/latest/flow/advanced/loading-resources) says
 * `src/main/resources/META-INF/frontend/` and
 * [deprecates](https://vaadin.com/docs/latest/building-apps/components/package-component-flow)
 * the former (everything under `META-INF/resources/` is also served as static resources, which
 * exposes the un-bundled sources to browsers). Vaadin 25 still reads both, and so do we.
 *
 * Bare npm specifiers (e.g. `@appreciated/color-picker-field/src/color-picker-field.js`) are
 * loaded from the `node_modules/` folder instead.
 */
public class MockNpmTemplateParser {

    public companion object {
        /**
         * Register custom template loaders here if the default algorithm doesn't work for your app for some reason.
         */
        public val customLoaders: CopyOnWriteArrayList<CustomNpmTemplateLoader> = CopyOnWriteArrayList<CustomNpmTemplateLoader>()

        /**
         * @param tag the value of the [com.vaadin.flow.component.Tag] annotation, e.g. `my-component`
         * @param url the URL resolved according to the [com.vaadin.flow.component.dependency.JsModule] spec, for example `./view/my-view.js` or `@vaadin/vaadin-button.js`.
         */
        @JvmStatic
        public fun mockGetSourcesFromTemplate(tag: String, url: String): String {
            for (customLoader: CustomNpmTemplateLoader in customLoaders) {
                val template: String? = customLoader.getSourcesFromTemplate(tag, url)
                if (template != null) {
                    return template
                }
            }

            // the locations searched so far, to be listed in the error message below.
            val searchedIn: MutableList<String> = mutableListOf()
            val advice: String

            if (url.startsWith("./")) {
                // relative URLs, located in the `frontend/`/`src/main/frontend/` folder,
                // or in the `META-INF/frontend/`/`META-INF/resources/frontend/` resource folder.
                val relativeUrl: String = url.substring(2)

                // try loading from the local fs
                listOf("frontend", "src/main/frontend").forEach { frontendDirName ->
                    val frontend: File = File(frontendDirName).absoluteFile
                    val templateFile = File(frontend, relativeUrl)
                    if (templateFile.exists()) {
                        return templateFile.readText()
                    }
                    searchedIn.add(templateFile.absolutePath)
                }

                // try loading from classpath. `META-INF/frontend/` is where Vaadin 25 expects jar
                // add-ons to place their frontend files; `META-INF/resources/frontend/` is the
                // Vaadin 24 location, deprecated but still supported by Vaadin 25.
                listOf("META-INF/frontend", "META-INF/resources/frontend").forEach { resourceDirName ->
                    val classpathEntry = "$resourceDirName/$relativeUrl"
                    val resource: URL? = Thread.currentThread().contextClassLoader.getResource(classpathEntry)
                    if (resource != null) {
                        return resource.readText()
                    }
                    searchedIn.add("classpath:$classpathEntry")
                }

                advice = " 1. make sure the file is present in one of the locations above. A jar add-on should ship its frontend files in `src/main/resources/META-INF/frontend/` - see https://vaadin.com/docs/latest/flow/advanced/loading-resources\n"
            } else {
                // probably a npm module such as @appreciated/color-picker-field
                // try the `node_modules/` folder.
                val nodeModules: File = File("node_modules").absoluteFile
                require(nodeModules.exists()) {
                    "$nodeModules folder doesn't exist, cannot load template sources for <$tag> $url. Please make sure that the `node_modules/` folder is populated, by running mvn vaadin:build-frontend before the tests. Read https://github.com/mvysny/karibu-testing/blob/master/karibu-testing-v10/README.md#where-template-sources-are-loaded-from for more info"
                }
                val templateFile = File(nodeModules, url)
                if (templateFile.exists()) {
                    return templateFile.readText()
                }
                searchedIn.add(templateFile.absolutePath)

                advice = " 1. make sure that the `node_modules/` folder is populated, by running mvn vaadin:build-frontend . Read https://github.com/mvysny/karibu-testing/blob/master/karibu-testing-v10/README.md#where-template-sources-are-loaded-from for more info\n"
            }

            throw RuntimeException("""Can't load template sources for <$tag> $url. Searched in:
${searchedIn.joinToString("\n") { " * $it" }}
Please:
$advice 2. as a workaround, introduce your own CustomNpmTemplateLoader to MockNpmTemplateParser.customLoaders which is able to load the template""")
        }
    }
}

public interface CustomNpmTemplateLoader {
    /**
     * Try to load sources for given Polymer Template.
     * @param tag the value of the [com.vaadin.flow.component.Tag] annotation, e.g. `my-component`
     * @param url the URL resolved according to the [com.vaadin.flow.component.dependency.JsModule] spec, for example `./view/my-view.js` or `@vaadin/vaadin-button.js`.
     * @return the contents of the JavaScript file or null if the JavaScript file could not be resolved.
     */
    public fun getSourcesFromTemplate(tag: String, url: String): String?
}