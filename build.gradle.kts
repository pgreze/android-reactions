buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:_")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
    }
}

plugins {
    id("io.codearte.nexus-staging")
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    group = Publish.group
    version = Publish.version
}

val local = project.rootProject.file("local.properties")
    .takeIf(File::exists)
    ?.toProperties()
    ?: java.util.Properties() // Optional with CI

// Inject config
mapOf(
    // Signing
    "signing.keyId" to "SIGNING_KEY_ID",
    "signing.password" to "SIGNING_PASSWORD",
    "signing.secretKeyRingFile" to "SIGNING_SECRET_KEY_RING_FILE",
    "ossrh.username" to "OSSRH_USERNAME",
    "ossrh.password" to "OSSRH_PASSWORD"
).forEach {  (key, envName) ->
    val value = local.propOrEnv(key, envName)
        ?.let {
            if (key.contains("File")) {
                rootProject.file(it).absolutePath
            } else it
        }
    rootProject.ext.set(key, value)
}

// https://github.com/Codearte/gradle-nexus-staging-plugin
nexusStaging {
    packageGroup = Publish.group
    stagingProfileId = local.propOrEnv("sonatype.staging.profile.id", "SONATYPE_STAGING_PROFILE_ID")
    username = "${rootProject.ext.get("ossrh.username")}"
    password = "${rootProject.ext.get("ossrh.password")}"
}
