# UUKotlinBuild

Shared Gradle convention plugins (Kotlin DSL) for UU Kotlin/Android open-source libraries.

Distributed via [GitHub Packages](https://github.com/SilverpineSoftware/UUKotlinBuild/packages) — push a Git tag, the workflow publishes the plugins.

## What it provides

| Plugin id                          | Replaces (legacy Groovy script)        | Purpose                                                                 |
| ---------------------------------- | -------------------------------------- | ----------------------------------------------------------------------- |
| `com.silverpine.uu.library`        | `uu-build-library-common.gradle`       | Android library + Maven publish + signing + BuildConfig stamping        |
| `com.silverpine.uu.library-app`    | `uu-build-library-app-common.gradle`   | Sample-app module hosting a UU library                                  |
| `com.silverpine.uu.publish`        | `uu-publish-common.gradle`             | Sonatype / OSSRH credentials + Nexus Publish Plugin (apply at root)     |
| `com.silverpine.uu.android-test`   | `uu-build-test-common.gradle`          | Gradle Managed Devices (`pixel*api*`, `nexus1api30`) + unit-test config |

Coordinates:

- main artifact: `com.silverpine.uu:uu-kotlin-build:<version>`
- plugin markers: `com.silverpine.uu.<id>:com.silverpine.uu.<id>.gradle.plugin:<version>`

## Consuming it from a library project

GitHub Packages requires authentication for **read** even on public repos, so each
developer (and CI) needs a GitHub PAT with `read:packages` scope.

### One-time per developer: store credentials

In `~/.gradle/gradle.properties` (NOT in the repo):

```properties
gpr.user=your-github-username
gpr.token=ghp_yourPersonalAccessTokenWithReadPackagesScope
```

In CI, set `GITHUB_ACTOR` and `GITHUB_TOKEN` env vars (GitHub Actions provides these
automatically; add `permissions: { packages: read }` to the calling job).

### `settings.gradle.kts` of the consuming library

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            name = "UUKotlinBuildGitHubPackages"
            url = uri("https://maven.pkg.github.com/SilverpineSoftware/UUKotlinBuild")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.token").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

No `resolutionStrategy` rewriting is needed — the plugin marker artifacts live at
`com.silverpine.uu.<id>:...`, which resolves directly through the GitHub Packages repo.

### Root `build.gradle.kts` of the consuming library

```kotlin
plugins {
    id("com.android.library") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.silverpine.uu.publish") version "1.0.0"
}
```

### Library module `build.gradle.kts`

```kotlin
plugins {
    id("com.silverpine.uu.library") version "1.0.0"
    id("com.silverpine.uu.android-test") version "1.0.0"
}
```

### Sample-app module `build.gradle.kts`

```kotlin
plugins {
    id("com.silverpine.uu.library-app") version "1.0.0"
}
```

### `gradle.properties` (consumer)

```properties
uu_publish_artifact_id=uu-foo-ktx
uu_publish_description=My UU library
uu_scm_module_name=UUFooKtx
uu_namespace=com.silverpine.uu.foo
uu_min_sdk=24
uu_target_sdk=34
uu_java_version=17
```

## Releasing a new version

1. Push a `vX.Y.Z` tag:

   ```bash
   git tag v1.2.3
   git push origin v1.2.3
   ```

2. The `Publish to GitHub Packages` workflow runs and publishes
   `com.silverpine.uu:uu-kotlin-build:1.2.3` plus the four plugin markers
   (the leading `v` is stripped from the artifact version).

3. Consumers update their plugin version: `id("com.silverpine.uu.library") version "1.2.3"`.

You can also trigger the workflow manually from the Actions tab via *Run workflow* and
type any version string (useful for SNAPSHOT-like builds).

## Building locally

```bash
./gradlew assemble
./gradlew publishToMavenLocal -Puu_build_version=0.0.1-LOCAL
```

To dry-run the GitHub Packages flow against a local PAT:

```bash
GITHUB_ACTOR=$USER GITHUB_TOKEN=ghp_xxx ./gradlew \
  publishAllPublicationsToGitHubPackagesRepository -Puu_build_version=0.0.1-LOCAL
```

## Notes

* The legacy `*.gradle` Groovy scripts at the repo root are kept for now to ease the
  migration and can be deleted once all consumers move to the plugin ids.
* `com.silverpine.uu.library` configures `withSourcesJar()` on the `release` variant,
  so AGP itself produces the sources artifact — no manual `androidSourcesJar` task.
* The plugin's transitive AGP / Kotlin GP dependencies do not pin the consumer's AGP
  version. Consumers declare their own AGP in their root `plugins {}` block; Gradle
  resolves the highest version on the build classpath.
