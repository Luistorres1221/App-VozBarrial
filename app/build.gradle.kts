import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.vozbarrial"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.vozbarrial"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        resValue("string", "mapbox_access_token", localProperties.getProperty("MAPBOX_ACCESS_TOKEN", ""))

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildFeatures {
        compose = true
        resValues = true
    }

}

dependencies {
    implementation("androidx.compose.ui:ui")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.fragment:fragment-ktx:1.9.1")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.mapbox.maps:android-ndk27:11.22.0")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.22.0")
    implementation("androidx.biometric:biometric:1.1.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}


