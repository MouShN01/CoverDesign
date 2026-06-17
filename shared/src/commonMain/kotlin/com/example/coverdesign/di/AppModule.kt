package com.example.coverdesign.di

// shared/commonMain/di/AppModule.kt
import com.example.coverdesign.data.local.DatabaseDriverFactory
import com.example.coverdesign.data.remote.client.createHttpClient
import com.example.coverdesign.data.remote.interceptor.InMemoryTokenStorage
import com.example.coverdesign.data.remote.interceptor.TokenStorage
import com.example.coverdesign.data.remote.service.AuthApiService
import com.example.coverdesign.data.remote.service.Model3DApiService
import com.example.coverdesign.data.remote.service.OrderApiService
import com.example.coverdesign.data.remote.service.ProductApiService
import com.example.coverdesign.data.repoimpl.ArSessionRepositoryImpl
import com.example.coverdesign.data.repoimpl.AuthRepositoryImpl
import com.example.coverdesign.data.repoimpl.Model3DRepositoryImpl
import com.example.coverdesign.data.repoimpl.OrderRepositoryImpl
import com.example.coverdesign.data.repoimpl.ProductRepositoryImpl
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.repo.AuthRepository
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.usecase.CalculatePriceUseCase
import com.example.coverdesign.domain.usecase.ConfigureProductUseCase
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.usecase.GetCatalogUseCase
import com.example.coverdesign.domain.usecase.GetModelPreviewUseCase
import com.example.coverdesign.domain.usecase.GetOrderSummaryUseCase
import com.example.coverdesign.domain.usecase.SubmitOrderUseCase
import com.example.coverdesign.domain.usecase.UpdateConfigUseCase
import com.example.coverdesign.domain.usecase.VirtualTryOnUseCase
import com.example.coverdesign.domain.validator.MeasurementValidator
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val databaseModule = module {
    // DatabaseDriverFactory is expect/actual — platform provides Context on Android
    single { get<DatabaseDriverFactory>().createDriver() }
    single { AppDatabase(driver = get()) }

    // SQLDelight query objects — generated from .sq files
    single { get<AppDatabase>().productConfigQueries }
    single { get<AppDatabase>().orderQueries }
    single { get<AppDatabase>().catalogQueries }
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
    single<ProductRepository>  { ProductRepositoryImpl(api = get(), db = get()) }
    single<Model3DRepository>  { Model3DRepositoryImpl(api = get()) }
    single<OrderRepository>    { OrderRepositoryImpl(api = get(), db = get()) }
    single<AuthRepository>     { AuthRepositoryImpl(api = get(), tokenStorage = get()) }
    single<ArSessionRepository>{ ArSessionRepositoryImpl() }
}

val useCaseModule = module {
    // FR1
    factoryOf(::GetCatalogUseCase)

    // FR2, FR3
    factory { ConfigureProductUseCase(repo = get(), validator = get()) }

    // FR4
    factoryOf(::GenerateModelUseCase)

    // FR5
    factoryOf(::GetModelPreviewUseCase)

    // FR6
    factory { VirtualTryOnUseCase(arRepo = get(), model3DRepo = get()) }

    // FR7
    factory { UpdateConfigUseCase(repo = get(), arRepo = get(), validator = get()) }

    // FR8
    factoryOf(::CalculatePriceUseCase)

    // FR9
    factoryOf(::SubmitOrderUseCase)

    // FR10
    factoryOf(::GetOrderSummaryUseCase)

    // shared validator
    singleOf(::MeasurementValidator)
}

val appModules = listOf(
    databaseModule,
    networkModule,
    repositoryModule,
    useCaseModule
)
