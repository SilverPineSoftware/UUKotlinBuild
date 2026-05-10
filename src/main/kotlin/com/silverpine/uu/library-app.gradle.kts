package com.silverpine.uu

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
}

fun replaceWithSamplePart(namespace: String): String {
    val parts = namespace.split('.').toMutableList()
    return if (parts.size >= 2) {
        val last = parts.removeAt(parts.lastIndex)
        parts.add("sample")
        parts.add(last)
        parts.joinToString(".")
    } else {
        "$namespace.sample"
    }
}

fun requiredProp(name: String): String =
    findProperty(name)?.toString() ?: error("Required property '$name' is not set")

val namespaceProp = requiredProp("uu_namespace")
val sampleAppId = replaceWithSamplePart(namespaceProp)
val minSdkProp = requiredProp("uu_min_sdk").toInt()
val targetSdkProp = requiredProp("uu_target_sdk").toInt()
val javaVersionProp = requiredProp("uu_java_version")

android {
    compileSdk = targetSdkProp
    namespace = sampleAppId

    defaultConfig {
        applicationId = sampleAppId
        minSdk = minSdkProp
        targetSdk = targetSdkProp
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVersionProp)
        targetCompatibility = JavaVersion.toVersion(javaVersionProp)
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersionProp))
        }
    }
}
