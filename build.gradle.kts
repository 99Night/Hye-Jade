plugins {
    id("com.android.application") version "8.5.0" apply false   // AGP 예시
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false

}
buildscript {
    val objectboxVersion by extra("4.3.1")

    // For Android projects
    val _compileSdkVersion by extra(35) /* Android 15 */
    val _targetSdkVersion by extra(33) /* Android 13 (TIRAMISU) */

    dependencies {
        // Find compatible versions at https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
        classpath("com.android.tools.build:gradle:8.10.1") // For Android projects
        // Note: when updating make sure to update coroutines dependency to match.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0") // For Kotlin projects
        classpath("io.objectbox:objectbox-gradle-plugin:$objectboxVersion")
    }

    repositories {
        mavenCentral() // ObjectBox artifacts are available on Maven Central.
        google() // For Android projects
    }
}