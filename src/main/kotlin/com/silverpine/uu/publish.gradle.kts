package com.silverpine.uu

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

/**
 * Only configure Sonatype when URLs are present (e.g. publish CI).
 * Empty strings must not be passed to [uri]; otherwise: "Cannot convert '' to URI".
 */
private fun optionalEnv(name: String): String? =
    System.getenv(name)?.takeUnless { it.isBlank() }

val mavenCentralNexusUrl = optionalEnv("MAVEN_CENTRAL_NEXUS_URL")
val mavenCentralSnapshotUrl = optionalEnv("MAVEN_CENTRAL_SNAPSHOT_URL")

nexusPublishing {
    repositories {
        if (mavenCentralNexusUrl != null && mavenCentralSnapshotUrl != null) {
            sonatype {
                stagingProfileId.set(optionalEnv("SONATYPE_STAGING_PROFILE_ID").orEmpty())
                username.set(optionalEnv("OSSRH_USERNAME").orEmpty())
                password.set(optionalEnv("OSSRH_PASSWORD").orEmpty())
                nexusUrl.set(uri(mavenCentralNexusUrl))
                snapshotRepositoryUrl.set(uri(mavenCentralSnapshotUrl))
            }
        }
    }
}
