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
    const val kotlinVersion = "1.3.41"
    const val supportLibraryVersion = "27.1.1"
}

object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlinVersion}"
    const val support = "com.android.support:support-compat:${Versions.supportLibraryVersion}"
    const val appcompat = "com.android.support:appcompat-v7:${Versions.supportLibraryVersion}"
}
