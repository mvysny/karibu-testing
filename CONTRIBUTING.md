# Developing

Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

# Running tests

To run all tests on all Vaadin versions, simply run `./gradlew test`.

# Releasing

To release the library to Maven Central:

1. Run all tests: `./gradlew test`
2. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza, e.g. "2.7.1"
3. Update `CHANGELOG.md`: rename the `## [Unreleased] (x.y.z)` heading to `## [x.y.z] - YYYY-MM-DD`
   (today's date), making sure every notable change since the last release is listed under it.
4. Run `./gradlew clean build publish closeAndReleaseStagingRepositories`
5. (Optional) watch [Maven Central Publishing Deployments](https://central.sonatype.com/publishing/deployments) as the deployment is published.
6. Commit with the commit message of simply being the version being released, e.g. "2.7.1"
7. git tag the commit with the same tag name as the commit message above, e.g. `2.7.1`
8. `git push`, `git push --tags`
9. Create a GitHub release for the tag, using the freshly-added `CHANGELOG.md` section as the
   release notes. Extract that version's section and hand it to `gh` as the release body, e.g.:
   ```bash
   VERSION=2.7.1
   awk "/^## \[$VERSION\]/{f=1; next} /^## \[/{f=0} f" CHANGELOG.md > /tmp/relnotes.md
   gh release create "$VERSION" --title "$VERSION" --notes-file /tmp/relnotes.md
   ```
10. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
    e.g. 2.7.2-SNAPSHOT, then commit with the commit message "2.7.2-SNAPSHOT" and push.
    Add a fresh `## [Unreleased] (2.7.2)` section to the top of `CHANGELOG.md` for the next round of changes.
