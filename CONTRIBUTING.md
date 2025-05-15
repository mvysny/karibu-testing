# Developing

Please feel free to open bug reports to discuss new features; PRs are welcome as well :)

# Running tests

To run all tests on all Vaadin versions, simply run `./gradlew test`.

In order to run the tests on various JDKs easily, you can use docker:

* For Java 8 run `docker run -it --rm openjdk:8 /bin/bash`
* For Java 11 run `docker run -it --rm openjdk:11 /bin/bash`
* For Java 17 run `docker run -it --rm openjdk:17-alpine /bin/sh` then install git: `apk add git`

In docker, simply type in the following commands:

```bash
git clone https://github.com/mvysny/karibu-testing
cd karibu-testing
./gradlew test
```

# Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza
2. Commit with the commit message of simply being the version being released, e.g. "1.2.13"
3. git tag the commit with the same tag name as the commit message above, e.g. `1.2.13`
4. `git push`, `git push --tags`
5. Run `./gradlew clean build publish closeAndReleaseStagingRepositories`
   - if releasing Karibu 2.0.0+ make sure you're building+releasing with Java JDK 17+; if releasing Karibu 1.x make sure you're building+releasing with Java 11 (not 17).
6. (Optional) watch [Maven Central Publishing Deployments](https://central.sonatype.com/publishing/deployments) as the deployment is published.
7. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 1.2.14, then commit with the commit message "1.2.14-SNAPSHOT" and push.
