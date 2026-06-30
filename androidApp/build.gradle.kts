import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleGmsGoogleServices)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    //implementation(libs.coil.compose)       // AsyncImage
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network)

    implementation(libs.accompanist.flowlayout) // FlowRow
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.animation)
    debugImplementation(libs.compose.uiTooling)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.sceneview)     // 3D model viewer
    implementation(libs.arsceneview)   // AR camera + model overlay
    implementation(libs.arcore)        // ARCore tracking
    implementation(libs.accompanist.permissions)

    //implementation(libs.firebase.storage.ktx)
    implementation(platform(libs.firebase.bom))
//    implementation(libs.firebase.storage)
//    implementation(libs.firebase.firestore)
    //implementation(libs.firebase.firestore)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}

android {
    namespace = "com.example.coverdesign"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.coverdesign"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}