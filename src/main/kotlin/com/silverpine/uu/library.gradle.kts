package com.silverpine.uu

import java.text.SimpleDateFormat
import java.util.Date
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    `maven-publish`
    signing
}

fun uuTrimAndQuote(value: String): String = "\"" + value.trim() + "\""

fun uuRunGit(vararg args: String): String {
    return try {
        val execOutput = providers.exec {
            commandLine(listOf("git", *args))
            isIgnoreExitValue = true
        }
        val exit = execOutput.result.get().exitValue
        if (exit == 0) {
            execOutput.standardOutput.asText.get()
        } else {
            project.logger.lifecycle("git ${args.joinToString(" ")} exited with $exit")
            ""
        }
    } catch (ex: Exception) {
        project.logger.lifecycle("Caught exception running git ${args.joinToString(" ")}: $ex")
        ""
    }
}

val publishGroupId = "com.silverpine.uu"

val buildVersion: String = version.toString()
val buildBranch: String = uuTrimAndQuote(uuRunGit("rev-parse", "--abbrev-ref", "HEAD"))
val buildCommitHash: String = uuTrimAndQuote(uuRunGit("rev-parse", "HEAD")).also {
    logger.lifecycle("buildCommitHash: $it")
}
val buildDate: String = run {
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ")
    val raw = df.format(Date()).trim().replace("'", "")
    "\"$raw\"".also { logger.lifecycle("buildDate: $it") }
}

extra["BUILD_VERSION"] = buildVersion
extra["BUILD_BRANCH"] = buildBranch
extra["BUILD_COMMIT_HASH"] = buildCommitHash
extra["BUILD_DATE"] = buildDate
extra["PUBLISH_GROUP_ID"] = publishGroupId

group = publishGroupId

logger.lifecycle("BUILD_VERSION: $buildVersion")
logger.lifecycle("BUILD_BRANCH: $buildBranch")

fun requiredProp(name: String): String =
    findProperty(name)?.toString() ?: error("Required property '$name' is not set")

val artifactIdProp = requiredProp("uu_publish_artifact_id")
val descriptionProp = requiredProp("uu_publish_description")
val scmModuleName = requiredProp("uu_scm_module_name")
val namespaceProp = requiredProp("uu_namespace")
val minSdkProp = requiredProp("uu_min_sdk").toInt()
val targetSdkProp = requiredProp("uu_target_sdk").toInt()
val javaVersionProp = requiredProp("uu_java_version")

android {
    compileSdk = targetSdkProp
    namespace = namespaceProp

    defaultConfig {
        minSdk = minSdkProp
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "BUILD_VERSION", uuTrimAndQuote(buildVersion))
        buildConfigField("String", "BUILD_BRANCH", buildBranch)
        buildConfigField("String", "BUILD_COMMIT_HASH", buildCommitHash)
        buildConfigField("String", "BUILD_DATE", buildDate)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    // AGP 9+ bundles Kotlin; configure toolchain inside `android { }` so `kotlin`
    // resolves to the Android Kotlin DSL, not Gradle's `kotlin(...)` dependency helper.
    kotlin {
        jvmToolchain(javaVersionProp.toInt())
    }
}

val githubSourceSubpath =
    project.path.removePrefix(":").replace(':', '/') + "/src/main/java"

dokka {
    moduleName.set(artifactIdProp)
    moduleVersion.set(buildVersion)

    dokkaSourceSets.configureEach {
        documentedVisibilities.set(setOf(VisibilityModifier.Public))
        skipDeprecated.set(true)
        reportUndocumented.set(false)

        sourceLink {
            localDirectory.set(layout.projectDirectory.dir("src/main/java"))
            remoteUrl(
                "https://github.com/SilverpineSoftware/$scmModuleName/tree/main/$githubSourceSubpath",
            )
            remoteLineSuffix.set("#L")
        }
    }
}

val dokkaJavadocJar =
    tasks.register<Jar>("dokkaJavadocJar") {
        group = "documentation"
        description = "Javadoc JAR containing Dokka-generated API documentation (KDoc)"
        archiveClassifier.set("javadoc")
        from(
            tasks.named("dokkaGeneratePublicationJavadoc").flatMap {
                @Suppress("UNCHECKED_CAST")
                (it as org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask).outputDirectory
            },
        )
    }

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD"))
    sign(publishing.publications)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = publishGroupId
                artifactId = artifactIdProp
                version = buildVersion

                from(components["release"])
                artifact(dokkaJavadocJar)

                pom {
                    name.set(artifactIdProp)
                    description.set(descriptionProp)
                    url.set("https://github.com/SilverpineSoftware/$scmModuleName")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://github.com/SilverpineSoftware/$scmModuleName/blob/master/LICENSE")
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
                        connection.set("scm:git:github.com/SilverpineSoftware/$scmModuleName.git")
                        developerConnection.set("scm:git:ssh://github.com/SilverpineSoftware/$scmModuleName.git")
                        url.set("https://github.com/SilverpineSoftware/$scmModuleName/tree/main")
                    }
                }
            }
        }
    }
}
