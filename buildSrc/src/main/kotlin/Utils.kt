import java.io.File
import java.util.*

fun File.toProperties(): Properties =
    Properties().also { it.load(reader()) }

fun Properties.propOrEnv(name: String, envName: String): String? =
    getProperty(name) ?: System.getenv(envName)
