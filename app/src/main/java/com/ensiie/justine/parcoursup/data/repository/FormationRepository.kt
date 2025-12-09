package com.ensiie.justine.parcoursup.data.repository

import com.ensiie.justine.parcoursup.data.local.FavoriteFormationDao
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.data.model.FormationStats
import com.ensiie.justine.parcoursup.data.network.ParcoursupApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormationRepository @Inject constructor(
    private val apiService: ParcoursupApiService,
    private val favoriteDao: FavoriteFormationDao
) {
    
    suspend fun searchFormations(
        name: String? = null,
        filiere: String? = null,
        contratEtab: String? = null,
        offset: Int = 0,
        limit: Int = 50
    ): Result<List<Formation>> = try {
        val whereConditions = mutableListOf("session=2024")
        
        name?.takeIf { it.isNotBlank() }?.let { 
            whereConditions.add("g_ea_lib_vx LIKE \"$it\"")
        }
        filiere?.let { whereConditions.add("fili=\"$it\"") }
        contratEtab?.let {
            whereConditions.add("contrat_etab=\"$it\"")
        }
        
        val whereClause = whereConditions.joinToString(" AND ")
        
        val response = apiService.searchFormations(where = whereClause, offset = offset, limit = limit)
        Result.success(response.results)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getAllFormationsForMap(): Result<List<FormationMap>> = try {
        val response = apiService.getFormationsMap()
        Result.success(response.results)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getFormationById(id: String): Result<Formation> = try {
        val whereClause = "cod_aff_form=\"$id\""
        val response = apiService.getFormationById(where = whereClause, limit = 1)
        val formation = response.results.firstOrNull()
            ?: throw Exception("Formation not found")
        Result.success(formation)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun getFilieres(): Result<List<String>> = try {
        val response = apiService.getFilieres()
        val filieres = response.results
            .map { it.tf }
            .distinct()
            .sorted()
        Result.success(filieres)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getContratsEtab(): Result<List<String>> = try {
        val response = apiService.getContratsEtab()
        val contrats = response.results
            .map { it.ce }
            .distinct()
            .sorted()
        Result.success(contrats)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun calculateStats(formations: List<Formation>): FormationStats {
        if (formations.isEmpty()) {
            return FormationStats(0, 0.0, 0.0, 0.0)
        }
        
        val capaciteMoyenne = formations.mapNotNull { it.capacite }.average()
        val tauxAccesMoyen = formations.mapNotNull { it.tauxAcces }.average()
        
        val moyenneDesMoyennes = formations.mapNotNull { formation ->
            val totalAdmis = formation.admis ?: 0
            if (totalAdmis == 0) return@mapNotNull null
            
            val sm = (formation.mentionSans ?: 0) * 11.0
            val ab = (formation.mentionAB ?: 0) * 13.0
            val b = (formation.mentionB ?: 0) * 15.0
            val tb = (formation.mentionTB ?: 0) * 17.0
            val tbf = (formation.mentionTBF ?: 0.0) * 19.0
            
            val total = sm + ab + b + tb + tbf
            val count = (formation.mentionSans ?: 0) + (formation.mentionAB ?: 0) + 
                       (formation.mentionB ?: 0) + (formation.mentionTB ?: 0) + 
                       (formation.mentionTBF?.toInt() ?: 0)
            
            if (count > 0) total / count else null
        }.average()
        
        return FormationStats(
            nombreEtablissements = formations.size,
            capaciteMoyenne = capaciteMoyenne,
            tauxAccesMoyen = tauxAccesMoyen,
            moyenneDesMoyennes = moyenneDesMoyennes
        )
    }
    
    fun getAllFavorites(): Flow<List<FavoriteFormation>> = 
        favoriteDao.getAllFavorites()
    
    fun isFavorite(id: String): Flow<Boolean> = 
        favoriteDao.isFavorite(id)
    
    suspend fun addFavorite(formation: Formation) {
        val coords = formation.coordonnees
        val favoriteId = formation.id
        val favorite = FavoriteFormation(
            id = favoriteId,
            etablissement = formation.etablissement ?: "",
            ville = formation.ville ?: "",
            departement = formation.departement ?: "",
            filiere = formation.filiereString ?: "",
            formationLib = formation.formationLibString ?: "",
            formationDetaillee = formation.formationDetailleeString ?: "",
            latitude = coords?.getOrNull(0),
            longitude = coords?.getOrNull(1)
        )
        favoriteDao.insertFavorite(favorite)
    }
    
    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }
}
