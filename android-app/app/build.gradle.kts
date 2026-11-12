import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

fun buildConfigValue(name: String): String =
    localProperties.getProperty(name, "").replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.mahak.portfoliomanager"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val keystorePath = localProperties.getProperty("RELEASE_STORE_FILE")
                ?: error("Add RELEASE_STORE_FILE to android-app/local.properties.")
            storeFile = rootProject.file(keystorePath)
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                ?: error("Add RELEASE_STORE_PASSWORD to android-app/local.properties.")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                ?: error("Add RELEASE_KEY_ALIAS to android-app/local.properties.")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
                ?: error("Add RELEASE_KEY_PASSWORD to android-app/local.properties.")
        }
    }

    defaultConfig {
        applicationId = "com.mahak.portfoliomanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SUPABASE_URL", "\"${buildConfigValue("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${buildConfigValue("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "SUPABASE_ADMIN_USER_ID", "\"${buildConfigValue("SUPABASE_ADMIN_USER_ID")}\"")
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
