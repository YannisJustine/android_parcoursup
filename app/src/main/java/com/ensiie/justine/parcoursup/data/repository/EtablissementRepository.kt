package com.ensiie.justine.parcoursup.data.repository

import com.ensiie.justine.parcoursup.data.model.EtablissementContactInfo
import com.ensiie.justine.parcoursup.data.network.EtablissementApiService

class EtablissementRepository(
    private val apiServiceSup: EtablissementApiService,
    private val apiServiceEducation: EtablissementApiService
) {
    
    suspend fun getEtablissementContactInfo(uai: String): Result<EtablissementContactInfo> {
        return try {
            val responseSup = apiServiceSup.getEtablissementSup(
                where = "uai='$uai'"
            )
            
            val etabSup = responseSup.results?.firstOrNull()
            if (etabSup != null) {
                return Result.success(
                    EtablissementContactInfo(
                        uai = etabSup.uai ?: uai,
                        nom = etabSup.nom ?: "",
                        telephone = etabSup.num,
                        mail = null,
                        siteWeb = etabSup.siteWeb,
                        adresse = etabSup.adresse,
                        codePostal = etabSup.codePostal,
                        commune = etabSup.commune,
                        latitude = etabSup.coordonnees?.lat,
                        longitude = etabSup.coordonnees?.lon,
                        type = etabSup.type?.firstOrNull()
                    )
                )
            }
            
            val responseEdu = apiServiceEducation.getEtablissementEducation(
                where = "identifiant_de_l_etablissement='$uai'"
            )
            
            val etabEdu = responseEdu.results?.firstOrNull()
            if (etabEdu != null) {
                return Result.success(
                    EtablissementContactInfo(
                        uai = etabEdu.uai ?: uai,
                        nom = etabEdu.nom ?: "",
                        telephone = etabEdu.telephone,
                        mail = etabEdu.mail,
                        siteWeb = etabEdu.siteWeb,
                        adresse = etabEdu.adresse,
                        codePostal = etabEdu.codePostal,
                        commune = etabEdu.commune,
                        latitude = etabEdu.latitude,
                        longitude = etabEdu.longitude,
                        type = etabEdu.type
                    )
                )
            }
            
            Result.success(
                EtablissementContactInfo(
                    uai = uai,
                    nom = "",
                    telephone = null,
                    mail = null,
                    siteWeb = null,
                    adresse = null,
                    codePostal = null,
                    commune = null,
                    latitude = null,
                    longitude = null,
                    type = null
                )
            )
            
        } catch (e: Exception) {
            Result.success(
                EtablissementContactInfo(
                    uai = uai,
                    nom = "",
                    telephone = null,
                    mail = null,
                    siteWeb = null,
                    adresse = null,
                    codePostal = null,
                    commune = null,
                    latitude = null,
                    longitude = null,
                    type = null
                )
            )
        }
    }
}
