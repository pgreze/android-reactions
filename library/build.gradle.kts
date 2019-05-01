import com.android.build.gradle.LibraryExtension

plugins {
    id("com.android.library")
    kotlin("android")
}
apply(from = "../gradle/publish.gradle")

configure<LibraryExtension> {
    compileSdkVersion(Config.targetSdk)
    defaultConfig {
        minSdkVersion(Config.minSdk)
        targetSdkVersion(Config.targetSdk)
    }
    resourcePrefix("reactions_")
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    "api"(Libs.kotlin)
    "api"(Libs.support)
}

apply(from = "../gradle/install.gradle")
apply(from = "../gradle/bintray.gradle")
