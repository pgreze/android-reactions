import com.android.build.gradle.LibraryExtension
import groovy.util.Node
import org.gradle.api.publish.maven.MavenPom

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka")
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
    api(Kotlin.stdlib.jdk7)
    api(AndroidX.core)
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

// https://github.com/Kotlin/dokka
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
    dokkaSourceSets {
        named("main") {
            moduleName.set(Publish.artifactId)
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                // URL showing where the source code can be accessed through the web browser
                remoteUrl.set(uri("${Publish.githubUrl}/tree/${Publish.tagVersion ?: "master"}/").toURL())
                // Suffix which is used to append the line number to the URL. Use #L for GitHub
                remoteLineSuffix.set("#L")
            }
        }
    }
}
// CSS trick + Github support from https://pgreze.dev/posts/2020-05-28-static-doc-netlify/
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
        val dokkaTask = tasks.dokkaHtml.get()
        val dokkaOutputDirectory = dokkaTask.outputDirectory.get()
        val dokkaSingleModuleFolder = dokkaOutputDirectory.resolve(dokkaTask.moduleName.get())
        dokkaSingleModuleFolder.recursivelyRewriteStyleLocations()
        dokkaOutputDirectory.resolve("style.css").also {
            it.renameTo(dokkaSingleModuleFolder.resolve(it.name))
        }
    }
}
tasks.dokkaHtml { finalizedBy(moveCss) }
