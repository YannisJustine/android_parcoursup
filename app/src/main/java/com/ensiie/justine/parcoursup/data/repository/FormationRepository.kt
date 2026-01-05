package com.ensiie.justine.parcoursup.data.repository

import com.ensiie.justine.parcoursup.data.local.FavoriteFormationDao
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.data.model.FormationStats
import com.ensiie.justine.parcoursup.data.network.ParcoursupApiService
import com.ensiie.justine.parcoursup.utils.LocationFilter
import kotlinx.coroutines.flow.Flow

class FormationRepository(
    private val apiService: ParcoursupApiService,
    private val favoriteDao: FavoriteFormationDao
) {

    suspend fun searchFormations(
        name: String? = null,
        filieres: Set<String> = emptySet(),
        contratsEtab: Set<String> = emptySet(),
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Int? = null,
        offset: Int = 0,
        limit: Int = 50
    ): Result<List<Formation>> = try {
        val whereConditions = mutableListOf("session=2024")

        name?.takeIf { it.isNotBlank() }?.let {
            whereConditions.add("search(g_ea_lib_vx,\"%$it%\")")
        }

        if (filieres.isNotEmpty()) {
            val orConditions = filieres.joinToString(" OR ") { "fili=\"$it\"" }
            whereConditions.add("($orConditions)")
        }

        if (contratsEtab.isNotEmpty()) {
            val orConditions = contratsEtab.joinToString(" OR ") { "contrat_etab=\"$it\"" }
            whereConditions.add("($orConditions)")
        }

        if (latitude != null && longitude != null && radiusKm != null) {
            val radiusMeters = radiusKm * 1000
            whereConditions.add("within_distance(g_olocalisation_des_formations, geom'POINT($longitude $latitude)', ${radiusMeters}m)")
        }

        val whereClause = whereConditions.joinToString(" AND ")
        val response =
            apiService.searchFormations(where = whereClause, offset = offset, limit = limit)

        Result.success(response.results)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getFormationsForMap(
        location: LocationFilter? = null,
    ): Result<List<FormationMap>> = try {
        val whereConditions =
            mutableListOf("session=2024 and g_olocalisation_des_formations IS NOT NULL")

        location?.let {
            val radiusMeters = it.radiusKm * 1000
            whereConditions.add("within_distance(g_olocalisation_des_formations, geom'POINT(${it.longitude} ${it.latitude})', ${radiusMeters}m)")
        }

        val whereClause = whereConditions.joinToString(" AND ")
        val response = apiService.getFormationsMap(where = whereClause)

        Result.success(response)
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
            formationDetaillee = formation.filiDetailString ?: "",
            latitude = coords?.getOrNull(0),
            longitude = coords?.getOrNull(1)
        )
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }

    suspend fun getSimilarFormations(
        currentFormationId: String,
        filiere: String?,
        latitude: Double?,
        longitude: Double?,
        limit: Int = 10
    ): Result<List<Formation>> = try {
        // Get the current formation to compare against
        val currentFormation = getFormationById(currentFormationId).getOrNull()
            ?: return Result.success(emptyList())
        
        val whereConditions = mutableListOf("session=2024")
        
        // Exclude current formation
        whereConditions.add("cod_aff_form!='$currentFormationId'")
        
        // Prioritize same filiere but don't require it
        if (latitude != null && longitude != null) {
            val radiusMeters = 100000 // 100km radius for broader search
            whereConditions.add("within_distance(g_olocalisation_des_formations, geom'POINT($longitude $latitude)', ${radiusMeters}m)")
        }
        
        val whereClause = whereConditions.joinToString(" AND ")

        val response = apiService.searchFormations(where = whereClause, offset = 0, limit = 50)
        
        val scoredFormations = response.results.map { formation ->
            formation to calculateSimilarityScore(currentFormation, formation, latitude, longitude)
        }
        
        val similarFormations = scoredFormations
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
        
        Result.success(similarFormations)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    private fun calculateSimilarityScore(
        current: Formation,
        candidate: Formation,
        currentLat: Double?,
        currentLon: Double?
    ): Double {
        var score = 0.0
        
        // 1. Same filiere: 40 points
        if (current.filiDetailString == candidate.filiDetailString && !current.filiDetailString.isNullOrBlank()) {
            score += 10.0
        }
        
        // 2. Same specialty: 40 points
        if (current.formationLibString == candidate.formationLibString &&
            !current.formationLibString.isNullOrBlank()) {
            score += 10.0
        }

        if (current.formationComplete == candidate.formationComplete &&
            !current.formationComplete.isNullOrBlank()) {
            score += 10.0
        }

        if(current.formationDetail == candidate.formationDetail && !current.formationDetail.isNullOrBlank()) {
            score += 10.0
        }
        
        // 3. Partial match on name: up to 30 points based on text similarity
        if (!current.etablissement.isNullOrBlank() && !candidate.etablissement.isNullOrBlank()) {
            val textSimilarity = calculateTextSimilarity(current.etablissement, candidate.etablissement)
            score += textSimilarity * 20.0
        }

        // 4. Same selectivity: 15 points
        if (current.selectForm == candidate.selectForm && !current.selectForm.isNullOrBlank()) {
            score += 15.0
        }
           
        // 5. Similar access rate (±15%): up to 10 points
        val currentAccess = current.tauxAcces
        val candidateAccess = candidate.tauxAcces
        if (currentAccess != null && candidateAccess != null) {
            val difference = kotlin.math.abs(currentAccess - candidateAccess)
            score += when {
                difference <= 5 -> 10.0
                difference <= 10 -> 7.0
                difference <= 15 -> 4.0
                else -> 0.0
            }
        }
        
        // 6. Similar capacity (±40%): up to 10 points
        val currentCapacity = current.capacite
        val candidateCapacity = candidate.capacite
        if (currentCapacity != null && candidateCapacity != null && currentCapacity > 0) {
            val percentDiff = kotlin.math.abs(currentCapacity - candidateCapacity) * 100.0 / currentCapacity
            score += when {
                percentDiff <= 20 -> 10.0
                percentDiff <= 40 -> 6.0
                percentDiff <= 60 -> 3.0
                else -> 0.0
            }
        }
        
        // 7. Similar bac profile: up to 10 points
        score += calculateBacProfileSimilarity(current, candidate) * 10.0
        
        // 8. Geographic proximity: up to 40 points
        if (currentLat != null && currentLon != null) {
            val candidateCoords = candidate.coordonnees
            if (candidateCoords != null && candidateCoords.size >= 2) {
                val distance = calculateDistance(currentLat, currentLon, candidateCoords[0], candidateCoords[1])
                score += when {
                    distance <= 10 -> 20.0
                    distance <= 25 -> 10.0
                    distance <= 50 -> 5.0
                    distance <= 100 -> 2.50
                    else -> 0.0
                }
            }
        }
        
        return score
    }
    
    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        val words1 = text1.lowercase().split("\\s+".toRegex()).toSet()
        val words2 = text2.lowercase().split("\\s+".toRegex()).toSet()
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        // Jaccard similarity
        return if (union > 0) intersection.toDouble() / union else 0.0
    }
    
    private fun calculateBacProfileSimilarity(current: Formation, candidate: Formation): Double {
        val currentGeneral = current.pctBacGeneral ?: 0.0
        val currentTech = current.pctBacTech ?: 0.0
        val currentPro = current.pctBacPro ?: 0.0
        
        val candidateGeneral = candidate.pctBacGeneral ?: 0.0
        val candidateTech = candidate.pctBacTech ?: 0.0
        val candidatePro = candidate.pctBacPro ?: 0.0
        
        // If no data available, return neutral score
        if (currentGeneral + currentTech + currentPro == 0.0 ||
            candidateGeneral + candidateTech + candidatePro == 0.0) {
            return 0.5
        }
        
        // Calculate average difference across all bac types
        val diffGeneral = kotlin.math.abs(currentGeneral - candidateGeneral)
        val diffTech = kotlin.math.abs(currentTech - candidateTech)
        val diffPro = kotlin.math.abs(currentPro - candidatePro)
        
        val avgDiff = (diffGeneral + diffTech + diffPro) / 3.0
        
        // Convert to similarity score (0-1)
        return kotlin.math.max(0.0, 1.0 - (avgDiff / 100.0))
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
}
