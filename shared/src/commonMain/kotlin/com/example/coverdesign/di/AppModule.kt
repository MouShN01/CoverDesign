package com.example.coverdesign.di

// shared/commonMain/di/AppModule.kt

import com.example.coverdesign.data.local.DatabaseDriverFactory
import com.example.coverdesign.data.local.DatabaseSeeder
import com.example.coverdesign.data.remote.client.createHttpClient
import com.example.coverdesign.data.remote.interceptor.InMemoryTokenStorage
import com.example.coverdesign.data.remote.interceptor.TokenStorage
import com.example.coverdesign.data.remote.service.AuthApiService
import com.example.coverdesign.data.remote.service.Model3DApiService
import com.example.coverdesign.data.remote.service.OrderApiService
import com.example.coverdesign.data.remote.service.ProductApiService
import com.example.coverdesign.data.repoimpl.FireAuthRepositoryImpl
import com.example.coverdesign.data.repoimpl.Model3DRepositoryImpl
import com.example.coverdesign.data.repoimpl.OrderRepositoryImpl
import com.example.coverdesign.data.repoimpl.ProductRepositoryImpl
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.repo.FireAuthRepository
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.usecase.CalculatePriceUseCase
import com.example.coverdesign.domain.usecase.ConfigureProductUseCase
import com.example.coverdesign.domain.usecase.CreateConfigFromProductUseCase
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.usecase.GetCatalogUseCase
import com.example.coverdesign.domain.usecase.GetOrderSummaryUseCase
import com.example.coverdesign.domain.usecase.LinkOrderToAdaptedModelUseCase
import com.example.coverdesign.domain.usecase.PollModelStatusUseCase
import com.example.coverdesign.domain.usecase.SaveOrderLocallyUseCase
import com.example.coverdesign.domain.usecase.SubmitOrderUseCase
import com.example.coverdesign.domain.usecase.UpdateConfigUseCase
import com.example.coverdesign.domain.usecase.UploadReferencePhotoUseCase
import com.example.coverdesign.domain.usecase.UploadTextureImageUseCase
import com.example.coverdesign.domain.usecase.VirtualTryOnUseCase
import com.example.coverdesign.domain.validator.MeasurementValidator
import com.example.coverdesign.ui.viewmodel.ArTryOnViewModel
import com.example.coverdesign.ui.viewmodel.AuthViewModel
import com.example.coverdesign.ui.viewmodel.CatalogViewModel
import com.example.coverdesign.ui.viewmodel.HomeViewModel
import com.example.coverdesign.ui.viewmodel.ModelPreviewViewModel
import com.example.coverdesign.ui.viewmodel.OrderSummaryViewModel
import com.example.coverdesign.ui.viewmodel.ProductConfigViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val databaseModule = module {
    // DatabaseDriverFactory is expect/actual — platform provides Context on Android
    single { get<DatabaseDriverFactory>().createDriver() }
    single { AppDatabase(driver = get()) }

    // SQLDelight query objects — generated from .sq files
    single { get<AppDatabase>().productConfigQueries }
    single { get<AppDatabase>().orderQueries }
    single { get<AppDatabase>().catalogQueries }
    single { get<AppDatabase>().model3DQueries }
    single { DatabaseSeeder(db = get()) }  
}

val networkModule = module {
    // TokenStorage — placeholder impl for now, replaced with
    // EncryptedStorage in the security step
    single<TokenStorage> { InMemoryTokenStorage() }

    // Ktor HttpClient — single instance shared across all services
    single { createHttpClient(tokenStorage = get()) }

    // API services
    singleOf(::ProductApiService)
    singleOf(::Model3DApiService)
    singleOf(::OrderApiService)
    singleOf(::AuthApiService)

}

val repositoryModule = module {
    single<ProductRepository> { ProductRepositoryImpl(api = get(), db = get()) }
    single<Model3DRepository> { Model3DRepositoryImpl(api = get(), db = get()) }
    single<OrderRepository> { OrderRepositoryImpl(api = get(), db = get()) }
//    single<AuthRepository> {
//        AuthRepositoryImplTest(tokenStorage = get())
//    }
    single<FireAuthRepository> { FireAuthRepositoryImpl() }


//single<ArSessionRepository>{ ArSessionRepositoryImpl() }
}

val useCaseModule = module {
    // FR1
    factoryOf(::GetCatalogUseCase)

    // FR2, FR3
    factory { ConfigureProductUseCase(repo = get(), validator = get()) }

    // FR4
    factory { GenerateModelUseCase(get ()) }
    factory { PollModelStatusUseCase(get ()) }
    factory { SaveOrderLocallyUseCase(get()) }


    // FR5
    //factoryOf(::GenerateModelUseCase)

    // FR6
    factory { VirtualTryOnUseCase(model3DRepo = get()) }
    factory { LinkOrderToAdaptedModelUseCase(orderRepo = get()) }

    // FR7
    factory { UpdateConfigUseCase(repo = get(), validator = get()) }
    factory { UploadTextureImageUseCase(storageRepo = get()) }
    factory { UploadReferencePhotoUseCase(storageRepo = get()) }

    factoryOf(::CreateConfigFromProductUseCase)

    // FR8
    factoryOf(::CalculatePriceUseCase)

    // FR9
    factoryOf(::SubmitOrderUseCase)

    // FR10
    factoryOf(::GetOrderSummaryUseCase)

    // shared validator
    singleOf(::MeasurementValidator)
}
val viewModelModule = module {

    viewModel {
        ProductConfigViewModel(
            productRepo = get(),
            generateModel = get(),
            validator = get(),
            uploadTextureImage = get(),
            uploadUserImage = get(),
            saveOrderLocally = get(),
        )
    }
    viewModel { CatalogViewModel(getCatalog = get()) }

    viewModel {
        ModelPreviewViewModel(
            pollModelStatus = get(),
            model3DRepo     = get(),
            linkOrderToAdaptedModel = get()
        )
    }
    viewModel {
        ArTryOnViewModel(
            virtualTryOn = get(),
            //updateConfig = get(),
            //getModelPreview = get()
        )
    }
    viewModel {
        OrderSummaryViewModel(
            submitOrderUseCase = get(),
            getOrderSummary = get()
        )
    }
    viewModel { HomeViewModel(getOrderSummary = get(), authRepo = get()) }
    viewModel { AuthViewModel(get(), get ()) }

}

val appModules = listOf(
    databaseModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
