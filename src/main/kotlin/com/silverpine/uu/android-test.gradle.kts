package com.silverpine.uu

plugins {
    id("com.android.library")
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }

        managedDevices {
            localDevices {
                create("pixel1api27") {
                    device = "Pixel"
                    apiLevel = 27
                    systemImageSource = "aosp"
                }
                create("pixel2api28") {
                    device = "Pixel 2"
                    apiLevel = 28
                    systemImageSource = "aosp"
                }
                create("pixel3api29") {
                    device = "Pixel 3"
                    apiLevel = 29
                    systemImageSource = "aosp"
                }
                create("pixel4api30") {
                    device = "Pixel 4"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
                create("pixel5api31") {
                    device = "Pixel 5"
                    apiLevel = 31
                    systemImageSource = "aosp-atd"
                }
                create("pixel6api32") {
                    device = "Pixel 6"
                    apiLevel = 32
                    systemImageSource = "aosp-atd"
                }
                create("pixel7api33") {
                    device = "Pixel 7"
                    apiLevel = 33
                    systemImageSource = "aosp-atd"
                }
                create("pixel8api34") {
                    device = "Pixel 8"
                    apiLevel = 34
                    systemImageSource = "aosp-atd"
                }
                create("pixel9api35") {
                    device = "Pixel 9"
                    apiLevel = 35
                    systemImageSource = "aosp-atd"
                }
                create("pixel10api36") {
                    device = "Pixel 10"
                    apiLevel = 36
                    systemImageSource = "aosp-atd"
                }
                create("nexus1api30") {
                    device = "Nexus One"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
            }

            groups {
                register("quick") {
                    targetDevices.add(localDevices.getByName("pixel7api33"))
                }
                register("ci") {
                    targetDevices.add(localDevices.getByName("pixel1api27"))
                    targetDevices.add(localDevices.getByName("pixel4api30"))
                    targetDevices.add(localDevices.getByName("pixel7api33"))
                    targetDevices.add(localDevices.getByName("pixel9api35"))
                }
                register("complete") {
                    targetDevices.add(localDevices.getByName("pixel1api27"))
                    targetDevices.add(localDevices.getByName("pixel2api28"))
                    targetDevices.add(localDevices.getByName("pixel3api29"))
                    targetDevices.add(localDevices.getByName("pixel4api30"))
                    targetDevices.add(localDevices.getByName("pixel5api31"))
                    targetDevices.add(localDevices.getByName("pixel6api32"))
                    targetDevices.add(localDevices.getByName("pixel7api33"))
                    targetDevices.add(localDevices.getByName("pixel8api34"))
                    targetDevices.add(localDevices.getByName("pixel9api35"))
                    targetDevices.add(localDevices.getByName("nexus1api30"))
                }
            }
        }
    }
}
