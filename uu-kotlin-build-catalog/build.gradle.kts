plugins {
    `version-catalog`
    `maven-publish`
}

group = "com.silverpine.uu"
version = providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()

catalog {
    versionCatalog {
        // uu_build and Silverpine plugin aliases track root `version=` / `-Pversion` (gradle.properties); not duplicated in libs.versions.toml.
        val releaseVersion =
            providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()
        from(files(rootProject.layout.projectDirectory.file("gradle/libs.versions.toml")))
        version("uu_build", releaseVersion)
        plugin("uu-library", "com.silverpine.uu.library").versionRef("uu_build")
        plugin("uu-library-app", "com.silverpine.uu.library-app").versionRef("uu_build")
        plugin("uu-publish", "com.silverpine.uu.publish").versionRef("uu_build")
        plugin("uu-android-test", "com.silverpine.uu.android-test").versionRef("uu_build")
    }
}

publishing {
    publications {
        register<MavenPublication>("uuKotlinBuildCatalog") {
            from(components["versionCatalog"])
            artifactId = "uu-kotlin-build-catalog"
            pom {
                name.set("UU Kotlin Build version catalog")
                description.set(
                    "Shared Gradle version catalog (AGP, Kotlin, Nexus Publish) for Silverpine UU Android/Kotlin libraries.",
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
