package com.example.coverdesign

import android.app.Application
import com.example.coverdesign.data.local.DatabaseDriverFactory
import com.example.coverdesign.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

// androidApp/src/main/kotlin/MainApplication.kt
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                appModules +
                        // Android-specific: provides DatabaseDriverFactory with Context
                        module {
                            single { DatabaseDriverFactory(context = androidContext()) }
                        }
            )
        }
    }
}