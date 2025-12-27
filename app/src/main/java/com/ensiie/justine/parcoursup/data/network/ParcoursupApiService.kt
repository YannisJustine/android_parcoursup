package com.ensiie.justine.parcoursup.data.network

import com.ensiie.justine.parcoursup.data.model.ApiResponse
import com.ensiie.justine.parcoursup.data.model.ContratEtabResponse
import com.ensiie.justine.parcoursup.data.model.FiliereGroupResponse
import com.ensiie.justine.parcoursup.data.model.FormationMap
import retrofit2.http.GET
import retrofit2.http.Query

interface ParcoursupApiService {

    @GET("fr-esr-parcoursup/records")
    suspend fun getFormations(
        @Query("where") where: String = "session=2024",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): ApiResponse

    @GET("fr-esr-parcoursup/exports/json?select=cod_aff_form,g_olocalisation_des_formations,ville_etab,g_ea_lib_vx,fil_lib_voe_acc")
    suspend fun getFormationsMap(
        @Query("where") where: String,
    ): List<FormationMap>

    @GET("fr-esr-parcoursup/records")
    suspend fun searchFormations(
        @Query("where") where: String = "session=2024",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ApiResponse

    @GET("fr-esr-parcoursup/records")
    suspend fun getFormationById(
        @Query("where") where: String,
        @Query("limit") limit: Int = 1
    ): ApiResponse

    @GET("fr-esr-parcoursup/records")
    suspend fun getFilieres(
        @Query("select") select: String = "fili",
        @Query("where") where: String = "session=2024",
        @Query("group_by") groupBy: String = "fili",
        @Query("limit") limit: Int = 100
    ): FiliereGroupResponse

    @GET("fr-esr-parcoursup/records?select=contrat_etab&where=session=2024&group_by=contrat_etab&limit=100")
    suspend fun getContratsEtab(): ContratEtabResponse
}
