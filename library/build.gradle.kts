import com.android.build.gradle.LibraryExtension
import groovy.util.Node
import org.gradle.api.publish.maven.MavenPom
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka") version "0.10.1"
    `maven-publish`
    signing
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
    api(kotlin("stdlib-jdk7"))
    api("androidx.core:core-ktx:1.3.2")
}

// Publishing

val androidSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create("maven", MavenPublication::class) {
                groupId = Publish.group
                artifactId = Publish.artifactId
                version = Publish.version

                artifact(tasks.getByName("bundleReleaseAar"))
                artifact(androidSourcesJar.get())

                pom {
                    name.set(Publish.artifactId)
                    description.set("A Facebook like reactions picker for Android")
                    url.set(Publish.githubUrl)
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm { url.set(Publish.githubUrl) }
                    developers {
                        developer {
                            id.set("pgreze")
                            name.set("Pierrick Greze")
                        }
                    }
                    addDependencies()
                }
            }
        }
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = "${rootProject.ext.get("ossrh.username")}"
                    password = "${rootProject.ext.get("ossrh.password")}"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
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

//
// Dokka
//
// See https://pgreze.dev/posts/2020-05-28-static-doc-netlify/ for the CSS trick + Github support
//

val moveCss by tasks.registering {
    description = "Move style.css in the module folder (distribution friendly)."
    fun File.rewriteStyleLocations() {
        readText().replace("../style.css", "style.css")
            .also { writeText(it) }
    }
    fun File.recursivelyRewriteStyleLocations() {
        list()?.map(this::resolve)?.forEach {
            if (it.isDirectory) it.recursivelyRewriteStyleLocations() else it.rewriteStyleLocations()
        }
    }
    doLast {
        val dokkaTask = tasks.dokka.get()
        val dokkaOutputDirectory = file(dokkaTask.outputDirectory)
        val dokkaSingleModuleFolder = dokkaOutputDirectory.resolve(dokkaTask.configuration.moduleName)
        dokkaSingleModuleFolder.recursivelyRewriteStyleLocations()
        dokkaOutputDirectory.resolve("style.css").also {
            it.renameTo(dokkaSingleModuleFolder.resolve(it.name))
        }
    }
}
tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/dokka"
    configuration {
        moduleName = Publish.artifactId
        sourceLink {
            // URL showing where the source code can be accessed through the web browser
            url = "${Publish.githubUrl}/tree/${Publish.tagVersion ?: "master"}/"
            // Suffix which is used to append the line number to the URL. Use #L for GitHub
            lineSuffix = "#L"
        }
    }
    finalizedBy(moveCss)
}
