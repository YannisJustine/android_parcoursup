package com.ensiie.justine.parcoursup.di

import androidx.room.Room
import com.ensiie.justine.parcoursup.data.local.ParcoursupDatabase
import com.ensiie.justine.parcoursup.data.network.AdresseApiService
import com.ensiie.justine.parcoursup.data.network.EtablissementApiService
import com.ensiie.justine.parcoursup.data.network.ParcoursupApiService
import com.ensiie.justine.parcoursup.data.repository.AdresseRepository
import com.ensiie.justine.parcoursup.data.repository.EtablissementRepository
import com.ensiie.justine.parcoursup.data.repository.FormationRepository
import com.ensiie.justine.parcoursup.ui.detail.FormationDetailViewModel
import com.ensiie.justine.parcoursup.ui.favorites.FavoritesViewModel
import com.ensiie.justine.parcoursup.ui.filters.SharedFilterViewModel
import com.ensiie.justine.parcoursup.ui.formations.FormationsViewModel
import com.ensiie.justine.parcoursup.ui.map.MapViewModel
import com.ensiie.justine.parcoursup.utils.LocationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL =
    "https://data.enseignementsup-recherche.gouv.fr/api/explore/v2.1/catalog/datasets/"
private const val ADRESSE_BASE_URL = "https://api-adresse.data.gouv.fr/"
private const val ETABLISSEMENT_SUP_BASE_URL = "https://data.enseignementsup-recherche.gouv.fr/"
private const val ETABLISSEMENT_EDU_BASE_URL = "https://data.education.gouv.fr/"

val appModule = module {

    // Moshi
    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // OkHttpClient
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<Retrofit>(qualifier = named("adresse")) {
        Retrofit.Builder()
            .baseUrl(ADRESSE_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<Retrofit>(qualifier = named("etablissementSup")) {
        Retrofit.Builder()
            .baseUrl(ETABLISSEMENT_SUP_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<Retrofit>(qualifier = named("etablissementEdu")) {
        Retrofit.Builder()
            .baseUrl(ETABLISSEMENT_EDU_BASE_URL)
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    // API Services
    single {
        get<Retrofit>().create(ParcoursupApiService::class.java)
    }

    single {
        get<Retrofit>(qualifier = named("adresse"))
            .create(AdresseApiService::class.java)
    }

    single(qualifier = named("etablissementSup")) {
        get<Retrofit>(qualifier = named("etablissementSup"))
            .create(EtablissementApiService::class.java)
    }

    single(qualifier = named("etablissementEdu")) {
        get<Retrofit>(qualifier = named("etablissementEdu"))
            .create(EtablissementApiService::class.java)
    }

    // Room Database
    single {
        Room.databaseBuilder(
            get(),
            ParcoursupDatabase::class.java,
            "parcoursup_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAO
    single { get<ParcoursupDatabase>().favoriteFormationDao() }

    // Repositories
    singleOf(::FormationRepository)
    singleOf(::AdresseRepository)
    single {
        EtablissementRepository(
            apiServiceSup = get(qualifier = named("etablissementSup")),
            apiServiceEducation = get(qualifier = named("etablissementEdu"))
        )
    }

    // Helpers    
    single { LocationHelper(get()) }

    // ViewModels
    single { SharedFilterViewModel() } // Shared singleton instance across screens
    viewModelOf(::FormationsViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::MapViewModel)
    viewModel { (recordId: String) ->
        FormationDetailViewModel(recordId, get(), get())
    }
}
