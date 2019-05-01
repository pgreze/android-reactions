/**
 * See:
 * https://github.com/codepath/android_guides/wiki/Building-your-own-Android-library
 *
 * For gradle config:
 * https://inthecheesefactory.com/blog/how-to-upload-library-to-jcenter-maven-central-as-dependency/en
 */
object Publish {
    const val publishedGroupId = "com.github.pgreze"
    const val libraryName = "Android Reactions"
    const val artifact = "android-reactions"

    const val siteUrl = "https://github.com/pgreze/android-reactions"
    const val gitUrl = "$siteUrl.git"

    const val libraryVersion = "1.0"

    const val developerId = "pgreze"
    const val developerName = "Pierrick Greze"

    const val licenseName = "The Apache Software License, Version 2.0"
    const val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
}
