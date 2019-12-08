object Config {
    const val minSdk = 16
    const val targetSdk = 28
}

object Publish {
    const val group = "com.github.pgreze"
    const val artifactId = "android-reactions"
    const val version = "1.1"
    const val url = "https://github.com/pgreze/android-reactions"
}

object Versions {
    const val kotlin = "1.3.41"
}

object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val core = "androidx.core:core:1.1.0"
    const val appcompat = "androidx.appcompat:appcompat:1.1.0"
}
