package com.ensiie.justine.parcoursup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Formation(
    @Json(name = "cod_aff_form") val id: String,
    @Json(name = "cod_uai") val etabUai: String,
    @Json(name = "g_ea_lib_vx") val etablissement: String?,
    @Json(name = "ville_etab") val ville: String?,
    @Json(name = "dep_lib") val departement: String?,
    @Json(name = "fili") val filiereString: String?,
    @Json(name = "form_lib_voe_acc") val formationLibString: String?,
    @Json(name = "fil_lib_voe_acc") val filiDetailString: String?,
    @Json(name = "lib_for_voe_ins") val formationComplete: String?,
    @Json(name = "detail_forma") val formationDetail: String?,
    @Json(name = "select_form") val selectForm: String?,
    @Json(name = "g_olocalisation_des_formations") val etabGps: Coordonnees?,
    @Json(name = "session") val session: String?,
    @Json(name = "capa_fin") val capacite: Int?,
    @Json(name = "voe_tot") val voeux: Int?,
    @Json(name = "acc_tot") val admis: Int?,
    @Json(name = "taux_acces_ens") val tauxAcces: Double?,
    @Json(name = "acc_mention_nonrenseignee") val mentionNonRenseigne: Int?,
    @Json(name = "acc_sansmention") val mentionSans: Int?,
    @Json(name = "acc_ab") val mentionAB: Int?,
    @Json(name = "acc_b") val mentionB: Int?,
    @Json(name = "acc_tb") val mentionTB: Int?,
    @Json(name = "acc_tbf") val mentionTBF: Double?,
    @Json(name = "acc_bg") val accBacGeneral: Int?,
    @Json(name = "acc_bt") val accBacTech: Int?,
    @Json(name = "acc_bp") val accBacPro: Int?,
    @Json(name = "pct_bg") val pctBacGeneral: Double?,
    @Json(name = "pct_bt") val pctBacTech: Double?,
    @Json(name = "pct_bp") val pctBacPro: Double?,
    @Json(name = "acc_tot_f") val accFemmes: Int?,
    @Json(name = "pct_f") val pctFemmes: Double?
) {
    val coordonnees: List<Double>?
        get() = etabGps?.let { listOf(it.lat, it.lon) }
}

@JsonClass(generateAdapter = true)
data class Coordonnees(
    @Json(name = "lon") val lon: Double,
    @Json(name = "lat") val lat: Double
)

@Entity(tableName = "favorite_formations")
data class FavoriteFormation(
    @PrimaryKey val id: String,
    val etablissement: String,
    val ville: String,
    val departement: String,
    val filiere: String,
    val formationLib: String,
    val formationDetaillee: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis()
)

data class FormationStats(
    val nombreEtablissements: Int,
    val capaciteMoyenne: Double,
    val tauxAccesMoyen: Double,
    val moyenneDesMoyennes: Double
)
