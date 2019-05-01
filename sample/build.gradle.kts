import com.android.build.gradle.AppExtension

plugins {
    id("com.android.application")
    kotlin("android")
}

configure<AppExtension> {
    compileSdkVersion(Config.targetSdk)
    defaultConfig {
        applicationId = "com.github.pgreze.fbreactions"
        minSdkVersion(Config.minSdk)
        targetSdkVersion(Config.targetSdk)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    flavorDimensions("provider")
    productFlavors {
        create("local") { setDimension("provider") }
        create("remote") { setDimension("provider") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    "implementation"(Libs.appcompat)

    "localImplementation"(project(path = ":library"))
    "remoteImplementation"("com.github.pgreze:android-reactions:1.0")
}
