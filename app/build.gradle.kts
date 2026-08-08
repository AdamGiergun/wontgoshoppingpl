import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.gradle.api.plugins.ExtensionAware
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

// Oficjalne testowe identyfikatory AdMob — zawsze w buildzie debug i jako zapas,
// gdy sekretów brak (czysta maszyna, CI). Nigdy nie wolno odpytywać produkcyjnych
// jednostek z urządzenia deweloperskiego: Google liczy to jako invalid traffic.
val testAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
val testBannerAdId = "ca-app-pub-3940256099942544/6300978111"
val testInterstitialAdId = "ca-app-pub-3940256099942544/1033173712"

// Sekrety trzymane w ~/.gradle/gradle.properties, poza repozytorium.
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
            // Jawnie i z rzutowaniem na ExtensionAware — patrz komentarz przy buildzie debug.
            (this as ExtensionAware).configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }
            signingConfig = signingConfigs.findByName("release")
            resValue("string", "ad_mob_app_id", secret("WGS_AD_MOB_APP_ID", testAdMobAppId))
            resValue("string", "main_banner_ad_id", secret("WGS_MAIN_BANNER_AD_ID", testBannerAdId))
            resValue("string", "interstitial_ad_id", secret("WGS_INTERSTITIAL_AD_ID", testInterstitialAdId))
            resValue("bool", "GOOGLE_ANALYTICS_ADID_ENABLED", "true")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_DEACTIVATED", "false")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_ENABLED", "true")
            resValue("bool", "FIREBASE_CRASHLYTICS_COLLECTION_ENABLED", "true")
        }
        debug {
            // Rzutowanie na ExtensionAware jest konieczne: bez niego `configure` wiąże się
            // z rozszerzeniem projektu, a nie tego typu builda, i ustawienie wycieka na
            // wszystkie warianty. Objawia się to zerowym mapping_file_id w buildzie
            // release, czyli nieczytelnymi stack trace'ami z produkcji.
            (this as ExtensionAware).configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            resValue("string", "ad_mob_app_id", testAdMobAppId)
            resValue("string", "main_banner_ad_id", testBannerAdId)
            resValue("string", "interstitial_ad_id", testInterstitialAdId)
            resValue("bool", "GOOGLE_ANALYTICS_ADID_ENABLED", "false")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_DEACTIVATED", "true")
            resValue("bool", "FIREBASE_ANALYTICS_COLLECTION_ENABLED", "false")
            resValue("bool", "FIREBASE_CRASHLYTICS_COLLECTION_ENABLED", "false")
        }
    }

    buildFeatures {
        viewBinding = true
        resValues = true
        // Potrzebne dla BuildConfig.DEBUG w DaysRepository — w buildzie debug
        // Remote Config pobiera dane bez throttlingu, żeby dało się je testować.
        buildConfig = true
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
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.savedstate.ktx)

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
    implementation(libs.firebase.config)

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
