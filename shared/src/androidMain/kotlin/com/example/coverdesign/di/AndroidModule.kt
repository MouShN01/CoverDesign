package com.example.coverdesign.di

import com.example.coverdesign.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// androidMain/di/AndroidModule.kt
val androidModule = module {
//    single<ArSessionRepository> {
//        ArSessionRepositoryImpl(context = androidContext())
//    }
    single { DatabaseDriverFactory(context = androidContext()) }
}