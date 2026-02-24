# Developing

Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

# Running tests

To run all tests on all Vaadin versions, simply run `./gradlew test`.

# Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza
2. Commit with the commit message of simply being the version being released, e.g. "1.2.13"
3. git tag the commit with the same tag name as the commit message above, e.g. `1.2.13`
4. `git push`, `git push --tags`
5. Run `./gradlew clean build publish closeAndReleaseStagingRepositories`
6. (Optional) watch [Maven Central Publishing Deployments](https://central.sonatype.com/publishing/deployments) as the deployment is published.
7. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 1.2.14, then commit with the commit message "1.2.14-SNAPSHOT" and push.
