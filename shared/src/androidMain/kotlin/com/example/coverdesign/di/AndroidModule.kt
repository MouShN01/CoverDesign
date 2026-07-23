package com.example.coverdesign.di

import com.example.coverdesign.data.local.DatabaseDriverFactory
import com.example.coverdesign.data.remote.GoogleAuthenticator
import com.example.coverdesign.data.repoimpl.FirebaseCloudConfigRepositoryImpl
import com.example.coverdesign.data.repoimpl.FirebaseStorageRepositoryImpl
import com.example.coverdesign.domain.repo.CloudConfigRepository
import com.example.coverdesign.domain.repo.StorageRepository
import com.example.coverdesign.shared.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// androidMain/di/AndroidModule.kt
val androidModule = module {
    single { DatabaseDriverFactory(context = androidContext()) }
    
    single { FirebaseStorage.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single<StorageRepository> {
        FirebaseStorageRepositoryImpl(storage = get())
    }
    single {
        GoogleAuthenticator(
            webClientId= androidContext().getString(R.string.web_id),

        )
    }
    single<CloudConfigRepository> { FirebaseCloudConfigRepositoryImpl(firestore = get()) }
}