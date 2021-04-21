import com.android.build.gradle.AppExtension

plugins {
    id("com.android.application")
    kotlin("android")
}

configure<AppExtension> {
    compileSdkVersion(Config.targetSdk)

    defaultConfig {
        applicationId = "${Publish.group}.fbreactions"
        minSdkVersion(Config.minSdk)
        targetSdkVersion(Config.targetSdk)
        versionCode = 1
        versionName = Publish.version
    }

    signingConfigs {
        getByName("debug") {
            storeFile = project.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        isIgnoreWarnings = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:_")

    // Enable with -PremoteArtifacts
    // See https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties
    val remoteArtifacts: Boolean? by project
    if (remoteArtifacts == true) {
        implementation(group = Publish.group, name = Publish.artifactId, version = Publish.version)
    } else {
        implementation(project(":library"))
    }
}
