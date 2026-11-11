buildscript {
    dependencies {
        // AGP 9 kompiluje Kotlina wbudowanym KGP (dla 9.5.1 to 2.2.10). Nowszy KGP
        // na classpath buildu ma pierwszeństwo — patrz komentarz przy `kotlin`
        // w libs.versions.toml.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mannodermaus.junit5) apply false
    alias(libs.plugins.navigation.safeargs) apply false
}
