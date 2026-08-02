import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.mannodermaus.junit5)
}

// Sekrety trzymane w ~/.gradle/gradle.properties. Gdy ich brak (czysta maszyna,
// CI), build debug nadal działa — na oficjalnych testowych identyfikatorach AdMob.
fun secret(name: String, fallback: String): String =
    providers.gradleProperty(name).getOrElse(fallback)

val keystorePath: String? = providers.gradleProperty("wgs_keystore").orNull
val keystoreFile = keystorePath?.let { rootProject.file(it) }?.takeIf { it.exists() }

android {
    namespace = "eu.adamgiergun.wontgoshoppingpl"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "eu.adamgiergun.wontgoshoppingpl"
        minSdk = 24
        targetSdk = 37
        versionCode = 132
        versionName = "3.8.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue(
            "string", "ad_mob_app_id",
            secret("WGS_AD_MOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
        )
        resValue(
            "string", "main_banner_ad_id",
            secret("WGS_MAIN_BANNER_AD_ID", "ca-app-pub-3940256099942544/6300978111")
        )
        resValue(
            "string", "interstitial_ad_id",
            secret("WGS_INTERSTITIAL_AD_ID", "ca-app-pub-3940256099942544/1033173712")
        )
    }

    if (keystoreFile != null) {
        signingConfigs {
            create("release") {
                keyAlias = providers.gradleProperty("wgs_alias").get()
                keyPassword = providers.gradleProperty("wgs_password").get()
                storeFile = keystoreFile
                storePassword = providers.gradleProperty("wgs_password").get()
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
            resValue("bool", "GOOGLE_ANALYTICS_ADID_ENABLED", "true")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_DEACTIVATED", "false")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_ENABLED", "true")
            resValue("bool", "FIREBASE_CRASHLYTICS_COLLECTION_ENABLED", "true")
        }
        debug {
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            resValue("bool", "GOOGLE_ANALYTICS_ADID_ENABLED", "false")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_DEACTIVATED", "true")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_ENABLED", "false")
            resValue("bool", "FIREBASE_CRASHLYTICS_COLLECTION_ENABLED", "false")
        }
    }

    buildFeatures {
        viewBinding = true
        resValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.my.kotlin.library)
    implementation(libs.my.resources)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.savedstate.ktx)
    implementation(libs.androidx.slidingpanelayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.transition.ktx)

    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.sqlite.framework)

    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.google.material)

    testImplementation(libs.junit4)
    testImplementation(libs.hamcrest.all)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.mockito.core)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
