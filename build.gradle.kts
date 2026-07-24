plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
//    alias(libs.plugins.devtoolsKsp) apply false
//    alias(libs.plugins.skydovesNavGraph) apply false
    //id("com.github.skydoves.navgraph") version "0.2.0"
}