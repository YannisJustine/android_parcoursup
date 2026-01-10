package com.ensiie.justine.parcoursup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Formation(
    @Json(name = "cod_aff_form") val id: String,
    @Json(name = "cod_uai") val etablissementUai: String,
    @Json(name = "g_ea_lib_vx") val etablissement: String?,
    @Json(name = "ville_etab") val ville: String?,
    @Json(name = "dep_lib") val departement: String?,
    @Json(name = "fili") val filiere: String?,
    @Json(name = "form_lib_voe_acc") val formationLibelle: String?,
    @Json(name = "fil_lib_voe_acc") val filiereDetail: String?,
    @Json(name = "lib_for_voe_ins") val formationComplete: String?,
    @Json(name = "detail_forma") val formationDetail: String?,
    @Json(name = "select_form") val selectivite: String?,
    @Json(name = "g_olocalisation_des_formations") val coordonneesEtablissement: Coordonnees?,
    @Json(name = "session") val session: String?,
    @Json(name = "capa_fin") val capacite: Int?,
    @Json(name = "voe_tot") val voeux: Int?,
    @Json(name = "acc_tot") val admis: Int?,
    @Json(name = "taux_acces_ens") val tauxAcces: Double?,
    @Json(name = "acc_mention_nonrenseignee") val mentionNonRenseignee: Int?,
    @Json(name = "acc_sansmention") val mentionSans: Int?,
    @Json(name = "acc_ab") val mentionAB: Int?,
    @Json(name = "acc_b") val mentionB: Int?,
    @Json(name = "acc_tb") val mentionTB: Int?,
    @Json(name = "acc_tbf") val mentionTBF: Double?,
    @Json(name = "acc_bg") val admisGenerale: Int?,
    @Json(name = "acc_bt") val admisTechnologique: Int?,
    @Json(name = "acc_bp") val admisProfessionnel: Int?,
    @Json(name = "pct_bg") val pourcentageGenerale: Double?,
    @Json(name = "pct_bt") val pourcentageTechnologique: Double?,
    @Json(name = "pct_bp") val pourcentageProfessionnel: Double?,
    @Json(name = "acc_tot_f") val admisFemmes: Int?,
    @Json(name = "pct_f") val pourcentageFemmes: Double?
) {
    val coordonnees: List<Double>?
        get() = coordonneesEtablissement?.let { listOf(it.lat, it.lon) }
}

@JsonClass(generateAdapter = true)
data class Coordonnees(
    @Json(name = "lon") val lon: Double,
    @Json(name = "lat") val lat: Double
)

@Entity(tableName = "favorite_formations")
data class FavoriteFormation(
    @PrimaryKey val id: String,
    val etablissementUai: String,
    val etablissement: String?,
    val ville: String?,
    val departement: String?,
    val filiere: String?,
    val formationLibelle: String?,
    val filiereDetail: String?,
    val formationComplete: String?,
    val formationDetail: String?,
    val selectivite: String?,
    val latitude: Double?,
    val longitude: Double?,
    val session: String?,
    val capacite: Int?,
    val voeux: Int?,
    val admis: Int?,
    val tauxAcces: Double?,
    val mentionNonRenseignee: Int?,
    val mentionSans: Int?,
    val mentionAB: Int?,
    val mentionB: Int?,
    val mentionTB: Int?,
    val mentionTBF: Double?,
    val admisGenerale: Int?,
    val admisTechnologique: Int?,
    val admisProfessionnel: Int?,
    val pourcentageGenerale: Double?,
    val pourcentageTechnologique: Double?,
    val pourcentageProfessionnel: Double?,
    val admisFemmes: Int?,
    val pourcentageFemmes: Double?,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toFormation(): Formation {
        return Formation(
            id = id,
            etablissementUai = etablissementUai,
            etablissement = etablissement,
            ville = ville,
            departement = departement,
            filiere = filiere,
            formationLibelle = formationLibelle,
            filiereDetail = filiereDetail,
            formationComplete = formationComplete,
            formationDetail = formationDetail,
            selectivite = selectivite,
            coordonneesEtablissement = if (latitude != null && longitude != null) {
                Coordonnees(lon = longitude, lat = latitude)
            } else null,
            session = session,
            capacite = capacite,
            voeux = voeux,
            admis = admis,
            tauxAcces = tauxAcces,
            mentionNonRenseignee = mentionNonRenseignee,
            mentionSans = mentionSans,
            mentionAB = mentionAB,
            mentionB = mentionB,
            mentionTB = mentionTB,
            mentionTBF = mentionTBF,
            admisGenerale = admisGenerale,
            admisTechnologique = admisTechnologique,
            admisProfessionnel = admisProfessionnel,
            pourcentageGenerale = pourcentageGenerale,
            pourcentageTechnologique = pourcentageTechnologique,
            pourcentageProfessionnel = pourcentageProfessionnel,
            admisFemmes = admisFemmes,
            pourcentageFemmes = pourcentageFemmes
        )
    }
}

fun Formation.toFavoriteFormation(): FavoriteFormation {
    val coords = coordonnees
    return FavoriteFormation(
        id = id,
        etablissementUai = etablissementUai,
        etablissement = etablissement,
        ville = ville,
        departement = departement,
        filiere = filiere,
        formationLibelle = formationLibelle,
        filiereDetail = filiereDetail,
        formationComplete = formationComplete,
        formationDetail = formationDetail,
        selectivite = selectivite,
        latitude = coords?.getOrNull(0),
        longitude = coords?.getOrNull(1),
        session = session,
        capacite = capacite,
        voeux = voeux,
        admis = admis,
        tauxAcces = tauxAcces,
        mentionNonRenseignee = mentionNonRenseignee,
        mentionSans = mentionSans,
        mentionAB = mentionAB,
        mentionB = mentionB,
        mentionTB = mentionTB,
        mentionTBF = mentionTBF,
        admisGenerale = admisGenerale,
        admisTechnologique = admisTechnologique,
        admisProfessionnel = admisProfessionnel,
        pourcentageGenerale = pourcentageGenerale,
        pourcentageTechnologique = pourcentageTechnologique,
        pourcentageProfessionnel = pourcentageProfessionnel,
        admisFemmes = admisFemmes,
        pourcentageFemmes = pourcentageFemmes
    )
}
