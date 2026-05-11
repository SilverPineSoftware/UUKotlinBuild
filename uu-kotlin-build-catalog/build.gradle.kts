plugins {
    `version-catalog`
    `maven-publish`
}

group = "com.silverpine.uu"
version = providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()

catalog {
    versionCatalog {
        from(files(rootProject.layout.projectDirectory.file("gradle/libs.versions.toml")))
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
