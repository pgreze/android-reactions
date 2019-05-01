import com.android.build.gradle.LibraryExtension
import groovy.util.Node
import org.gradle.api.publish.maven.MavenPom

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
}

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

// Publishing

group = Publish.groupId
version = Publish.version

val androidSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications.invoke {
            create("maven", MavenPublication::class) {
                artifactId = Publish.artifactId

                artifact(tasks.getByName("bundleReleaseAar"))
                artifact(androidSourcesJar.get())

                pom.addDependencies()
            }
        }
    }
}

fun MavenPom.addDependencies() = withXml {
    asNode().appendNode("dependencies").let { deps ->
        // List all "compile" dependencies (for old Gradle)
        configurations.compile.get().dependencies.addDependencies(deps, "compile")
        // List all "api" dependencies (for new Gradle) as "compile" dependencies
        configurations.api.get().dependencies.addDependencies(deps, "compile")
        // List all "implementation" dependencies (for new Gradle) as "runtime" dependencies
        configurations.implementation.get().dependencies.addDependencies(deps, "runtime")
    }
}

fun DependencySet.addDependencies(node: Node, scope: String) = forEach {
    node.appendNode("dependency").apply {
        appendNode("groupId", it.group)
        appendNode("artifactId", it.name)
        appendNode("version", it.version)
        appendNode("scope", scope)
    }
}
