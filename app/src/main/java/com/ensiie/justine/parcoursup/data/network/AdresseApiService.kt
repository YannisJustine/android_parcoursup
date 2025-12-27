package com.ensiie.justine.parcoursup.data.network

import com.ensiie.justine.parcoursup.data.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AdresseApiService {

    @GET("search/")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): CitySearchResponse
}
