package com.ensiie.justine.parcoursup.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EtablissementSupResponse(
    val results: List<EtablissementSup>?
)

@JsonClass(generateAdapter = true)
data class EtablissementSup(
    @Json(name = "uai") val uai: String?,
    @Json(name = "uo_lib") val nom: String?,
    @Json(name = "url") val siteWeb: String?,
    @Json(name = "adresse_uai") val adresse: String?,
    @Json(name = "code_postal_uai") val codePostal: String?,
    @Json(name = "commune") val commune: String?,
    @Json(name = "coordonnees") val coordonnees: CoordonneesGeo?,
    @Json(name = "type_d_etablissement") val type: Array<String>?,
    @Json(name = "sigle") val sigle: String?,
    @Json(name = "num_telephone_uai") val num_telephone_uai: String?
) {
    val num: String?
        get() = num_telephone_uai?.replace(Regex("""(\d{2})(?=\d)"""), "$1 ")?.trim()
}

@JsonClass(generateAdapter = true)
data class EtablissementEducationResponse(
    val results: List<EtablissementEducation>?
)

@JsonClass(generateAdapter = true)
data class EtablissementEducation(
    @Json(name = "identifiant_de_l_etablissement") val uai: String?,
    @Json(name = "nom_etablissement") val nom: String?,
    @Json(name = "telephone") val telephone: String?,
    @Json(name = "mail") val mail: String?,
    @Json(name = "web") val siteWeb: String?,
    @Json(name = "adresse_1") val adresse: String?,
    @Json(name = "code_postal") val codePostal: String?,
    @Json(name = "nom_commune") val commune: String?,
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "type_etablissement") val type: String?,
    @Json(name = "statut_public_prive") val statut: String?
)

@JsonClass(generateAdapter = true)
data class CoordonneesGeo(
    @Json(name = "lat") val lat: Double?,
    @Json(name = "lon") val lon: Double?
)

data class EtablissementContactInfo(
    val uai: String,
    val nom: String,
    val telephone: String?,
    val mail: String?,
    val siteWeb: String?,
    val adresse: String?,
    val codePostal: String?,
    val commune: String?,
    val latitude: Double?,
    val longitude: Double?,
    val type: String?
)
