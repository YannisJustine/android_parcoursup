package com.ensiie.justine.parcoursup.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "results") val results: List<Formation>
)

@JsonClass(generateAdapter = true)
data class ApiMapResponse(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "results") val results: List<FormationMap>
)

@JsonClass(generateAdapter = true)
data class FiliereGroupResponse(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "results") val results: List<FiliereGroup>
)

@JsonClass(generateAdapter = true)
data class ContratEtabResponse(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "results") val results: List<ContratEtab>
)

@JsonClass(generateAdapter = true)
data class FiliereGroup(
    @Json(name = "fili") val tf: String
)

@JsonClass(generateAdapter = true)
data class ContratEtab(
    @Json(name = "contrat_etab") val ce: String
)

@JsonClass(generateAdapter = true)
data class FormationMap(
    @Json(name = "cod_aff_form") val id: String,
    @Json(name = "g_olocalisation_des_formations") val etabGps: Coordonnees,
    @Json(name = "g_ea_lib_vx") val etablissement: String?,
    @Json(name = "ville_etab") val ville: String?,
    @Json(name = "fil_lib_voe_acc") val filiDetailString: String?,
)


@JsonClass(generateAdapter = true)
data class AggregationResponse(
    @Json(name = "aggregations") val aggregations: List<Aggregation>
)

@JsonClass(generateAdapter = true)
data class Aggregation(
    @Json(name = "tf") val tf: List<String>? = null
)
