package com.ensiie.justine.parcoursup.data.network

import com.ensiie.justine.parcoursup.data.model.EtablissementEducationResponse
import com.ensiie.justine.parcoursup.data.model.EtablissementSupResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EtablissementApiService {
    
    // API Établissements de l'Enseignement Supérieur
    @GET("api/explore/v2.1/catalog/datasets/fr-esr-principaux-etablissements-enseignement-superieur/records")
    suspend fun getEtablissementSup(
        @Query("where") where: String,
        @Query("limit") limit: Int = 1
    ): EtablissementSupResponse
    
    // API Annuaire de l'Éducation
    @GET("api/explore/v2.1/catalog/datasets/fr-en-annuaire-education/records")
    suspend fun getEtablissementEducation(
        @Query("where") where: String,
        @Query("limit") limit: Int = 1
    ): EtablissementEducationResponse
}
