package com.ensiie.justine.parcoursup.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CitySearchResponse(
    val features: List<CityFeature>
)

@JsonClass(generateAdapter = true)
data class CityFeature(
    val properties: CityProperties,
    val geometry: CityGeometry
)

@JsonClass(generateAdapter = true)
data class CityProperties(
    val label: String,
    val name: String?,
    val city: String?,
    val context: String?,
    val citycode: String?,
    val postcode: String?,
    val street: String?
)

@JsonClass(generateAdapter = true)
data class CityGeometry(
    val coordinates: List<Double> // [longitude, latitude]
)

data class City(
    val address: String,
    val cityName: String,
    val fullLabel: String,
    val longitude: Double,
    val latitude: Double,
    val context: String,
    val postcode: String? = null,
    val street: String? = null
)

fun CityFeature.toCity(): City {
    return City(
        address = properties.name ?: properties.label,
        cityName = properties.city ?: properties.label,
        fullLabel = properties.label,
        longitude = geometry.coordinates[0],
        latitude = geometry.coordinates[1],
        context = properties.context ?: "",
        postcode = properties.postcode,
        street = properties.street
    )
}
