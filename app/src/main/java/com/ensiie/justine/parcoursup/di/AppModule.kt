package com.ensiie.justine.parcoursup.di

import android.content.Context
import androidx.room.Room
import com.ensiie.justine.parcoursup.data.local.FavoriteFormationDao
import com.ensiie.justine.parcoursup.data.local.ParcoursupDatabase
import com.ensiie.justine.parcoursup.data.network.ParcoursupApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    private const val BASE_URL = "https://data.enseignementsup-recherche.gouv.fr/api/explore/v2.1/catalog/datasets/"
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    
    @Provides
    @Singleton
    fun provideParcoursupApiService(retrofit: Retrofit): ParcoursupApiService =
        retrofit.create(ParcoursupApiService::class.java)
    
    @Provides
    @Singleton
    fun provideParcoursupDatabase(
        @ApplicationContext context: Context
    ): ParcoursupDatabase = Room.databaseBuilder(
        context,
        ParcoursupDatabase::class.java,
        "parcoursup_database"
    ).build()
    
    @Provides
    @Singleton
    fun provideFavoriteFormationDao(database: ParcoursupDatabase): FavoriteFormationDao =
        database.favoriteFormationDao()
}
