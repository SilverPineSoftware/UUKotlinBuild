# UUKotlinBuild

Shared Gradle convention plugins and a version catalog for Silverpine **UU** Kotlin/Android library repositories. Everything is published to [GitHub Packages](https://github.com/SilverpineSoftware/UUKotlinBuild/packages) under `com.silverpine.uu`.

Tag a release (`x.y.z`) and the publish workflow ships new plugin and catalog versions. Consumer repos pin one version via the `uu_build` property and use the `uuBuild` catalog for plugins and shared dependency versions.

---

## What gets published

| Artifact | Maven coordinates | Purpose |
| -------- | ----------------- | ------- |
| Convention plugins | `com.silverpine.uu:uu-kotlin-build:<version>` | Precompiled script plugins (see table below) |
| Version catalog | `com.silverpine.uu:uu-kotlin-build-catalog:<version>` | Imported in consumer `settings.gradle.kts` as `uuBuild` |
| Plugin markers | `com.silverpine.uu.<plugin-id>:com.silverpine.uu.<plugin-id>.gradle.plugin:<version>` | Gradle plugin resolution (no manual `resolutionStrategy`) |

The catalog version always matches the convention-plugin release version. The catalog exposes a `uu_build` version alias and plugin aliases such as `uu-library`, `uu-library-app`, `uu-publish`, and `uu-android-test`.

---

## Convention plugins

| Plugin id | Catalog alias | Apply on | What it does |
| --------- | ------------- | -------- | ------------ |
| `com.silverpine.uu.library` | `uu-library` | Android library module | `com.android.library`, Maven Publish, signing, `BuildConfig` stamping from git, release AAR + sources JAR, POM metadata from `gradle.properties` |
| `com.silverpine.uu.library-app` | `uu-library-app` | Sample `app` module | Android application with `applicationId` derived from `uu_namespace` (inserts `.sample` segment), SDK/Java from shared properties |
| `com.silverpine.uu.publish` | `uu-publish` | Root project | Sonatype / Nexus Publish when `MAVEN_CENTRAL_*` and related env vars are set (publish CI only) |
| `com.silverpine.uu.android-test` | `uu-android-test` | Library module (with instrumented tests) | Gradle Managed Devices (Pixel API 27–36, device groups `quick`, `quickNet`, `ci`, `complete`), unit-test Android resources |

The catalog also publishes AGP, Kotlin, Nexus Publish, and `kotlin-compose` plugin aliases so consumers do not duplicate those versions in every repo.

---

## Gradle properties the plugins expect

Convention plugins read these with `findProperty(...)`. **Unset properties fail the build** with a clear error.

| Property | Used by | Meaning |
| -------- | ------- | ------- |
| `uu_build` | `settings.gradle.kts` | Version of `uu-kotlin-build-catalog` and convention plugins to resolve |
| `uu_namespace` | `uu.library`, `uu.library-app` | Android `namespace` / base package (sample app rewrites to `…sample.…`) |
| `uu_publish_artifact_id` | `uu.library` | Maven `artifactId` (e.g. `uu-core-ktx`) |
| `uu_publish_description` | `uu.library` | POM description |
| `uu_scm_module_name` | `uu.library` | GitHub repo name for POM/SCM URLs (e.g. `UUKotlinCore`) |
| `uu_min_sdk` | `uu.library`, `uu.library-app` | `minSdk` |
| `uu_target_sdk` | `uu.library`, `uu.library-app` | `compileSdk` / `targetSdk` |
| `uu_java_version` | `uu.library`, `uu.library-app` | JVM toolchain / `compileOptions` (e.g. `17`) |

**Where to define them in a consumer repo**

| Layout | Root `gradle.properties` | `library/gradle.properties` |
| ------ | -------------------------- | --------------------------- |
| Library only (e.g. UUKotlinCore) | `uu_min_sdk`, `uu_target_sdk`, `uu_java_version` | `uu_namespace`, publish fields |
| Library + sample `app` | SDK/Java **and** `uu_namespace` (so `:app` can read it) | Publish fields only |

You can commit defaults in the repo and override per developer or in CI (see below).

**GitHub Packages credentials** (not read by convention plugins, but required to resolve plugins and catalog):

| Property | Purpose |
| -------- | ------- |
| `gpr.user` | GitHub username for Packages |
| `gpr.token` | PAT with `read:packages` (local dev) |

In CI, `settings.gradle.kts` typically falls back to `GITHUB_ACTOR` + `GPR_TOKEN` or `GITHUB_TOKEN`.

---

## Local development

### 1. One-time: `~/.gradle/gradle.properties`

Put shared, machine-wide values in your **user** Gradle properties file (not in git):

```properties
# GitHub Packages (read UUKotlinBuild + other UU artifacts)
gpr.user=your-github-username
gpr.token=ghp_xxxxxxxx   # PAT with read:packages

# Pin the UUKotlinBuild release all UU repos should use locally
uu_build=0.0.27

# Defaults for convention plugins (CI org variables mirror these)
uu_min_sdk=26
uu_target_sdk=36
uu_java_version=17
```

That lets every UU clone resolve plugins and catalog without repeating versions in each repo.

### 2. Overrides

Gradle merges properties from several sources. In practice:

1. **Command line** `-Pname=value` wins over everything.
2. **Environment** `ORG_GRADLE_PROJECT_<name>` (used in GitHub Actions) overrides committed project files for that job.
3. **Project** `gradle.properties` (repo root or a subproject folder such as `library/`) overrides **user** `~/.gradle/gradle.properties` for that project.
4. **User** `~/.gradle/gradle.properties` supplies your global defaults.

So: set org-wide defaults in `~/.gradle/gradle.properties`, commit repo-specific publish metadata under `library/`, and use root `gradle.properties` for SDK/Java (and `uu_namespace` when the repo has a sample app). CI can override `uu_build`, SDK, and Java via organization variables without editing the repo.

### 3. Consumer `settings.gradle.kts`

Register the catalog and authenticate to GitHub Packages (pattern used across UU repos):

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
                    ?: System.getenv("GPR_TOKEN")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "UUKotlinBuildGitHubPackages"
            url = uri("https://maven.pkg.github.com/SilverpineSoftware/UUKotlinBuild")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.token").orNull
                    ?: System.getenv("GPR_TOKEN")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
        mavenLocal()   // optional: when testing catalog/plugins via publishToMavenLocal
    }
    versionCatalogs {
        register("uuBuild") {
            val uuBuildVersion = providers.gradleProperty("uu_build").orNull
                ?: error("Set `uu_build=<version>` in gradle.properties.")
            from("com.silverpine.uu:uu-kotlin-build-catalog:$uuBuildVersion")
        }
    }
}
```

### 4. Root `build.gradle.kts`

```kotlin
plugins {
    alias(uuBuild.plugins.android.application) apply false
    alias(uuBuild.plugins.android.library) apply false
    alias(uuBuild.plugins.kotlin.android) apply false
    alias(uuBuild.plugins.nexus.publish)
    alias(uuBuild.plugins.kotlin.serialization) apply false
    alias(uuBuild.plugins.kotlin.compose) apply false   // if the sample app uses Compose
    alias(uuBuild.plugins.uu.library) apply false
    alias(uuBuild.plugins.uu.library.app) apply false   // if you have :app
    alias(uuBuild.plugins.uu.android.test) apply false
    alias(uuBuild.plugins.uu.publish)
}
```

### 5. Library module `build.gradle.kts`

```kotlin
plugins {
    alias(uuBuild.plugins.uu.library)
    alias(uuBuild.plugins.uu.android.test)   // if you run managed-device tests
    // plus module-specific plugins, e.g. kotlin.serialization, parcelize
}

dependencies {
    implementation(uuBuild.androidx.appcompat)
    testImplementation(platform(uuBuild.junit.bom))
    testImplementation(uuBuild.junit.jupiter)
    // ...
}
```

### 6. Sample app module (optional)

```kotlin
plugins {
    alias(uuBuild.plugins.uu.library.app)
    alias(uuBuild.plugins.kotlin.compose)   // when using Compose; also configure composeCompiler { }
}
```

App-specific dependencies stay in the app module; use the repo’s own `libs` catalog for Compose BOM and UI libraries.

### 7. Example `gradle.properties` layout

**Root** (`gradle.properties`):

```properties
uu_min_sdk=26
uu_target_sdk=36
uu_java_version=17
uu_namespace=com.silverpine.uu.foo   # required when :app exists
version=1.2.3
```

**`library/gradle.properties`:**

```properties
uu_publish_artifact_id=uu-foo-ktx
uu_publish_description=Useful Utilities Foo
uu_scm_module_name=UUKotlinFoo
```

For a **library-only** repo, put `uu_namespace` in `library/gradle.properties` instead of the root.

---

## GitHub Actions

Consumer library repos (UUKotlinCore, UUKotlinNetworking, etc.) do not embed this logic; they call reusable workflows in **[UUGithubActions](https://github.com/SilverPineSoftware/UUGithubActions)**.

### Organization variables

Configure these at the org (or repo) level under **Settings → Secrets and variables → Actions → Variables**:

| Variable | Maps to Gradle property | Used for |
| -------- | ---------------------- | -------- |
| `UU_KOTLIN_BUILD` | `uu_build` | Catalog + convention plugin version |
| `UU_ANDROID_MIN_SDK` | `uu_min_sdk` | `minSdk` |
| `UU_ANDROID_TARGET_SDK` | `uu_target_sdk` | `compileSdk` / `targetSdk` |
| `UU_JAVA_VERSION` | `uu_java_version` | Gradle Android/Kotlin JVM settings |
| `UU_JAVA_DISTRIBUTION` | *(not a Gradle property)* | `actions/setup-java` distribution (e.g. `temurin`) |

### Wiring variables into Gradle

Reusable workflows run the composite action **`uu-android-load-environment-vars`** first. It appends to `GITHUB_ENV`:

- `ORG_GRADLE_PROJECT_uu_build`
- `ORG_GRADLE_PROJECT_uu_min_sdk`
- `ORG_GRADLE_PROJECT_uu_target_sdk`
- `ORG_GRADLE_PROJECT_uu_java_version`

Gradle exposes those as project properties for the rest of the job. Empty org variables are skipped so committed `gradle.properties` defaults still apply.

Example (from `uu_android_tests.yml`):

```yaml
- name: Wire org variables into Gradle project properties
  uses: SilverPineSoftware/UUGithubActions/.github/actions/uu-android-load-environment-vars@main
  with:
    uu_kotlin_build: ${{ vars.UU_KOTLIN_BUILD }}
    uu_android_min_sdk: ${{ vars.UU_ANDROID_MIN_SDK }}
    uu_android_target_sdk: ${{ vars.UU_ANDROID_TARGET_SDK }}
    uu_java_version: ${{ vars.UU_JAVA_VERSION }}
```

### Typical consumer workflow

A publish pipeline in a UU library repo often:

1. Calls `uu_android_tests` (unit tests on `:library`).
2. Calls `uu_android_instrumented_tests` (managed devices, excludes interaction/integration annotations).
3. Calls `uu_android_library_publish_release` or `uu_android_library_publish_snapshot`.

Pass **`READ_PACKAGES_PAT`** (repo secret with `read:packages`) as `secrets.READ_PACKAGES_PAT` so Gradle can resolve `UUKotlinBuild` from GitHub Packages when the default `GITHUB_TOKEN` cannot read another repo’s packages.

Maven Central publish still uses **secrets** (`OSSRH_*`, `SIGNING_*`, `MAVEN_CENTRAL_*`, etc.) consumed by `uu.publish` and the publish composite actions—not org variables.

### Instrumented tests

Managed-device groups are defined in `uu.android-test`. CI usually runs the `quickGroup` configuration. The instrumented-test action excludes tests annotated with `UUInteractionRequired` and `UUIntegrationTest` unless you run a broader configuration locally.

---

## Releasing a new UUKotlinBuild version

1. Bump `version=` in this repo’s `gradle.properties` on `develop` (or use **Create Release Tag** in Actions).
2. Create and push a numeric tag `x.y.z` (no `v` prefix). The **Publish to GitHub Packages** workflow runs `./gradlew build`, publishes plugins + catalog, creates a GitHub Release, and may bump the next patch on `develop`.
3. Update the org variable **`UU_KOTLIN_BUILD`** (and/or developers’ `uu_build` in `~/.gradle/gradle.properties`) to the new tag.

Consumers do not need to change plugin ids—only `uu_build` / `UU_KOTLIN_BUILD`.

---

## Building this repo locally

```bash
./gradlew assemble
./gradlew publishToMavenLocal -Pversion=0.0.1-LOCAL
```

Point a consumer at the local catalog:

```properties
uu_build=0.0.1-LOCAL
```

and keep `mavenLocal()` in `dependencyResolutionManagement.repositories` while iterating.

Publish to GitHub Packages (maintainers):

```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository -Pversion=1.2.3
```

Requires `gpr.user` / `gpr.token` or `GITHUB_ACTOR` / `GITHUB_TOKEN` with `write:packages`.

---

## Notes

- **`uu.library`** uses AGP’s `withSourcesJar()` on the release variant; no hand-rolled `androidSourcesJar` task.
- **Signing** for Maven Central uses env vars `SIGNING_KEY_ID`, `SIGNING_KEY`, `SIGNING_PASSWORD` in publish CI.
- **Transitive AGP/Kotlin** on the plugin classpath do not pin consumer AGP versions; consumers declare AGP/Kotlin via the `uuBuild` catalog at the root `plugins { }` block.
- For GMD (Gradle Managed Devices) tests that need network access, prefer **`aosp`** system images over **`aosp-atd`** on some hosts (see `pixel7api33Net` in `android-test.gradle.kts`).
