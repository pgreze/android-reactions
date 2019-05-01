buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Config.kotlinVersion}")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0") // generate a POM file
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/pgreze/maven")
    }
}

// Bintray Publishing

plugins {
    id("com.jfrog.bintray") version "1.8.4"
}

val local = project.rootProject.file("local.properties").toProperties()

bintray {
    user = local.propOrEnv("bintray.user", "BINTRAY_USER")
    key = local.propOrEnv("bintray.apikey", "BINTRAY_API_KEY")

    setPublications("maven")
    setConfigurations("archives")

    pkg.apply {
        repo = "maven"
        name = "android-reactions"
        desc = "A Facebook like reactions picker for Android"
        websiteUrl = Publish.siteUrl
        vcsUrl = Publish.gitUrl
        setLicenses("Apache-2.0")
        publish = true
        publicDownloadNumbers = true
        version.apply {
            desc = pkg.desc
            gpg.apply {
                // Determines whether to GPG sign the files (default: false)
                sign = true
                // The passphrase for GPG signing (optional)
                passphrase = local.propOrEnv("bintray.gpg.password", "BINTRAY_GPG_PASSWORD")
            }
        }
    }
}
