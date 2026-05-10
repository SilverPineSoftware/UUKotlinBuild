package com.silverpine.uu

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
            nexusUrl.set(uri(System.getenv("MAVEN_CENTRAL_NEXUS_URL") ?: ""))
            snapshotRepositoryUrl.set(uri(System.getenv("MAVEN_CENTRAL_SNAPSHOT_URL") ?: ""))
        }
    }
}
