object Config {
    const val minSdk = 16
    const val targetSdk = 28
}

object Publish {
    const val group = "com.github.pgreze"
    const val artifactId = "android-reactions"
    const val githubUrl = "https://github.com/pgreze/android-reactions"

    val tagVersion: String? = System.getenv("GITHUB_REF")?.split('/')?.last()
    val version: String = tagVersion?.trimStart('v') ?: "WIP"
}
