import java.io.File
import java.util.*

fun File.toProperties(): Properties =
    Properties().also { it.load(reader()) }

fun Properties.propOrEnv(name: String, envName: String): String? =
    getProperty(name) ?: System.getenv(envName)

fun getLibraryVersion(default: String = "WIP"): String =
    System.getenv("BITRISE_GIT_TAG")
        ?.trimStart('v')
        ?: default
