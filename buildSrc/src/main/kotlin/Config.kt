object Config {
    const val minSdk = 16
    const val targetSdk = 28
}

object Publish {
    const val group = "com.github.pgreze"
    const val artifactId = "android-reactions"
    val tagVersion: String? = System.getenv("BITRISE_GIT_TAG")?.trimStart('v')
    val version: String = tagVersion ?: "WIP"
    const val githubUrl = "https://github.com/pgreze/android-reactions"
}
