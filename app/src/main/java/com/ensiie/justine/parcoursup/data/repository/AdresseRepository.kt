package com.ensiie.justine.parcoursup.data.repository

import com.ensiie.justine.parcoursup.data.model.City
import com.ensiie.justine.parcoursup.data.model.toCity
import com.ensiie.justine.parcoursup.data.network.AdresseApiService

class AdresseRepository(
    private val apiService: AdresseApiService
) {

    suspend fun searchCities(query: String): Result<List<City>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val response = apiService.searchCity(query = query, limit = 10)
            val cities = response.features.map { it.toCity() }
            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reverse(latitude: Double, longitude: Double): Result<City> {
        return try {
            val response = apiService.reverseGeocode(
                longitude = longitude,
                latitude = latitude,
                index = "address"
            )
            if (response.features.isEmpty()) {
                Result.failure(Exception("Aucune adresse trouvée pour ces coordonnées"))
            } else {
                Result.success(response.features.first().toCity())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
