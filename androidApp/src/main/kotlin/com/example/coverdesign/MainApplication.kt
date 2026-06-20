package com.example.coverdesign

import android.app.Application
import com.example.coverdesign.di.androidModule
import com.example.coverdesign.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

// androidApp/src/main/kotlin/MainApplication.kt
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModules + androidModule)
        }
        //get<DatabaseSeeder>().seedIfEmpty()
    }
}