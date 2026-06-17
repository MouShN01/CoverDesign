package com.example.coverdesign.di

import com.example.coverdesign.data.local.DatabaseDriverFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    startKoin {
        modules(
            appModules +
                    module {
                        single { DatabaseDriverFactory() }  // no Context needed on iOS
                    }
        )
    }
}