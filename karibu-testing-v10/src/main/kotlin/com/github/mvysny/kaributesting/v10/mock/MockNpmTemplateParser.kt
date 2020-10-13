package com.github.mvysny.kaributesting.v10.mock

import com.github.mvysny.kaributesting.v10.mock.MockNpmTemplateParser.Companion.customLoaders
import com.vaadin.flow.component.polymertemplate.NpmTemplateParser
import java.io.File
import java.lang.RuntimeException
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Mock-loads NPM Polymer Templates from:
 * * [customLoaders]
 * * a local filesystem (`frontend/`)
 * * from classpath `META-INF/resources/frontend/`
 */
public class MockNpmTemplateParser : NpmTemplateParser() {

    /**
     * @param tag the value of the [com.vaadin.flow.component.Tag] annotation, e.g. `my-component`
     * @param url the URL resolved according to the [com.vaadin.flow.component.dependency.JsModule] spec, for example `./view/my-view.js` or `@vaadin/vaadin-button.js`.
     */
    override fun getSourcesFromTemplate(tag: String, url: String): String? {
        return mockGetSourcesFromTemplate(tag, url)
    }

    public companion object {
        /**
         * Register custom template loaders here if the default algorithm doesn't work for your app for some reason.
         */
        public val customLoaders: CopyOnWriteArrayList<CustomNpmTemplateLoader> = CopyOnWriteArrayList<CustomNpmTemplateLoader>()

        /**
         * @param tag the value of the [com.vaadin.flow.component.Tag] annotation, e.g. `my-component`
         * @param url the URL resolved according to the [com.vaadin.flow.component.dependency.JsModule] spec, for example `./view/my-view.js` or `@vaadin/vaadin-button.js`.
         */
        public fun mockGetSourcesFromTemplate(tag: String, url: String): String {
            for (customLoader: CustomNpmTemplateLoader in customLoaders) {
                val template: String? = customLoader.getSourcesFromTemplate(tag, url)
                if (template != null) {
                    return template
                }
            }

            if (url.startsWith("./")) {
                // relative URLs, located in `frontend/` folder or `META-INF/resources/frontend/` resource
                val relativeUrl: String = url.substring(2)

                // try loading from the local fs
                val frontend: File = File("frontend").absoluteFile
                val templateFile = File(frontend, relativeUrl)
                if (templateFile.exists()) {
                    return templateFile.readText()
                }

                // try loading from classpath
                val classpathEntry = "META-INF/resources/frontend/$relativeUrl"
                val resource: URL? = Thread.currentThread().contextClassLoader.getResource(classpathEntry)
                if (resource != null) {
                    return resource.readText()
                }
            } else {
                // probably a npm module such as @appreciated/color-picker-field
                // try the `node_modules/` folder.
                val nodeModules: File = File("node_modules").absoluteFile
                require(nodeModules.exists()) {
                    "$nodeModules folder doesn't exist, cannot load template sources for <$tag> $url. Please make sure that the `node_modules/` folder is populated, by running mvn vaadin:build-frontend before the tests. Read https://github.com/mvysny/karibu-testing/blob/master/karibu-testing-v10/README.md for more info"
                }
                val templateFile = File(nodeModules, url)
                if (templateFile.exists()) {
                    return templateFile.readText()
                }
            }

            throw RuntimeException("""Can't load template sources for <$tag> $url. Please:
 1. make sure that the `node_modules/` folder is populated, by running mvn vaadin:build-frontend . Read https://github.com/mvysny/karibu-testing/blob/master/karibu-testing-v10/README.md for more info
 2. as a workaround, introduce your own CustomNpmTemplateLoader to MockNpmTemplateParser.customLoaders which is able to load the template""")
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