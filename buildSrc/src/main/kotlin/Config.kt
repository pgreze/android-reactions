object Config {
    // Android
    const val minSdk = 16
    const val targetSdk = 28

    // Dependencies
    const val kotlinVersion = "1.3.21"
    const val supportLibraryVersion = "27.1.1"
}

object Libs {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Config.kotlinVersion}"
    const val support = "com.android.support:support-compat:${Config.supportLibraryVersion}"
    const val appcompat = "com.android.support:appcompat-v7:${Config.supportLibraryVersion}"
}
