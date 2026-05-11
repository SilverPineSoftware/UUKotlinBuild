plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    `kotlin-dsl`
    `maven-publish`
}

group = "com.silverpine.uu"
version = providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// `kotlin-dsl` makes every *.gradle.kts under src/main/kotlin a precompiled script plugin
// whose plugin id is the file name minus the `.gradle.kts` suffix.
//
// Plugins it produces:
//   * com.silverpine.uu.library
//   * com.silverpine.uu.library-app
//   * com.silverpine.uu.publish
//   * com.silverpine.uu.android-test

dependencies {
    // Precompiled script plugins resolve `plugins { id(...) }` blocks against
    // the build's runtime classpath, so AGP, Kotlin GP, and Nexus Publish must
    // be on `implementation`. Consumers will transitively pick them up; their
    // own AGP/Kotlin versions take precedence at the project plugin level.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.nexus.publish)
}

publishing {
    // The `java-gradle-plugin` plugin (auto-applied by `kotlin-dsl`) creates the
    // main `pluginMaven` publication and per-plugin marker publications lazily
    // after project evaluation, so we configure them via `matching {}`.
    publications.withType<MavenPublication>().matching { it.name == "pluginMaven" }.configureEach {
        artifactId = "uu-kotlin-build"
        pom {
            name.set("UU Kotlin Build Convention Plugins")
            description.set(
                "Shared Gradle convention plugins (Kotlin DSL) for UU Kotlin/Android open-source libraries.",
            )
            url.set("https://github.com/SilverpineSoftware/UUKotlinBuild")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/SilverpineSoftware/UUKotlinBuild/blob/main/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("ryandevore")
                    name.set("Ryan DeVore")
                    email.set("ryan@silverpine.com")
                }
            }
            scm {
                connection.set("scm:git:github.com/SilverpineSoftware/UUKotlinBuild.git")
                developerConnection.set("scm:git:ssh://github.com/SilverpineSoftware/UUKotlinBuild.git")
                url.set("https://github.com/SilverpineSoftware/UUKotlinBuild/tree/main")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
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
