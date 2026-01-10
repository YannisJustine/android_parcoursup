package com.ensiie.justine.parcoursup.data.repository

import com.ensiie.justine.parcoursup.data.local.FavoriteFormationDao
import com.ensiie.justine.parcoursup.data.model.FavoriteFormation
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.data.model.FormationMap
import com.ensiie.justine.parcoursup.data.model.toFavoriteFormation
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

    fun getAllFavorites(): Flow<List<FavoriteFormation>> =
        favoriteDao.getAllFavorites()

    fun isFavorite(id: String): Flow<Boolean> =
        favoriteDao.isFavorite(id)

    suspend fun addFavorite(formation: Formation) {
        favoriteDao.insertFavorite(formation.toFavoriteFormation())
    }

    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }

    suspend fun getSimilarFormations(
        currentFormationId: String,
        latitude: Double?,
        longitude: Double?,
        limit: Int = 10
    ): Result<List<Formation>> = try {
        val currentFormation = getFormationById(currentFormationId).getOrNull()
            ?: return Result.success(emptyList())

        val whereConditions = mutableListOf("session=2024")

        whereConditions.add("cod_aff_form!='$currentFormationId'")

        if (latitude != null && longitude != null) {
            val radiusMeters = 100000
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

        // 1. Filiere similarity: up to 30 points
        // Exact match: 30 points, partial match: proportional
        if (!current.filiere.isNullOrBlank() && !candidate.filiere.isNullOrBlank()) {
            if (current.filiere == candidate.filiere) {
                score += 30.0
            } else {
                // Check for partial word matches in filiere
                val filiereSimilarity = calculateTextSimilarity(current.filiere, candidate.filiere)
                score += filiereSimilarity * 15.0
            }
        }

        // 2. Formation detail similarity: up to 30 points
        // Compare all formation fields with weighted scores
        var formationScore = 0.0
        var formationFields = 0

        if (!current.filiereDetail.isNullOrBlank() && !candidate.filiereDetail.isNullOrBlank()) {
            if (current.filiereDetail == candidate.filiereDetail) {
                formationScore += 10.0
            } else {
                formationScore += calculateTextSimilarity(
                    current.filiereDetail,
                    candidate.filiereDetail
                ) * 5.0
            }
            formationFields++
        }

        if (!current.formationLibelle.isNullOrBlank() && !candidate.formationLibelle.isNullOrBlank()) {
            if (current.formationLibelle == candidate.formationLibelle) {
                formationScore += 10.0
            } else {
                formationScore += calculateTextSimilarity(
                    current.formationLibelle,
                    candidate.formationLibelle
                ) * 5.0
            }
            formationFields++
        }

        if (!current.formationComplete.isNullOrBlank() && !candidate.formationComplete.isNullOrBlank()) {
            if (current.formationComplete == candidate.formationComplete) {
                formationScore += 10.0
            } else {
                formationScore += calculateTextSimilarity(
                    current.formationComplete,
                    candidate.formationComplete
                ) * 5.0
            }
            formationFields++
        }

        if (!current.formationDetail.isNullOrBlank() && !candidate.formationDetail.isNullOrBlank()) {
            if (current.formationDetail == candidate.formationDetail) {
                formationScore += 10.0
            } else {
                formationScore += calculateTextSimilarity(
                    current.formationDetail,
                    candidate.formationDetail
                ) * 5.0
            }
            formationFields++
        }

        // Normalize formation score to max 30 points
        if (formationFields > 0) {
            score += kotlin.math.min(30.0, formationScore)
        }

        // 3. School name similarity: up to 20 points
        if (!current.etablissement.isNullOrBlank() && !candidate.etablissement.isNullOrBlank()) {
            if (current.etablissement == candidate.etablissement) {
                // Same school: 20 points
                score += 20.0
            } else {
                // Similar school name: proportional score
                val schoolSimilarity =
                    calculateTextSimilarity(current.etablissement, candidate.etablissement)
                score += schoolSimilarity * 15.0
            }
        }

        // 4. Same selectivity: 10 points
        if (current.selectivite == candidate.selectivite && !current.selectivite.isNullOrBlank()) {
            score += 10.0
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
            val percentDiff =
                kotlin.math.abs(currentCapacity - candidateCapacity) * 100.0 / currentCapacity
            score += when {
                percentDiff <= 20 -> 10.0
                percentDiff <= 40 -> 6.0
                percentDiff <= 60 -> 3.0
                else -> 0.0
            }
        }

        // 7. Similar bac profile: up to 10 points
        score += calculateBacProfileSimilarity(current, candidate) * 10.0

        // 8. Geographic proximity: up to 20 points
        if (currentLat != null && currentLon != null) {
            val candidateCoords = candidate.coordonnees
            if (candidateCoords != null && candidateCoords.size >= 2) {
                val distance = calculateDistance(
                    currentLat,
                    currentLon,
                    candidateCoords[0],
                    candidateCoords[1]
                )
                score += when {
                    distance <= 10 -> 20.0
                    distance <= 25 -> 15.0
                    distance <= 50 -> 10.0
                    distance <= 100 -> 5.0
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
        val currentGeneral = current.pourcentageGenerale ?: 0.0
        val currentTech = current.pourcentageTechnologique ?: 0.0
        val currentPro = current.pourcentageProfessionnel ?: 0.0

        val candidateGeneral = candidate.pourcentageGenerale ?: 0.0
        val candidateTech = candidate.pourcentageTechnologique ?: 0.0
        val candidatePro = candidate.pourcentageProfessionnel ?: 0.0

        // If no data available, return neutral score
        if (currentGeneral + currentTech + currentPro == 0.0 ||
            candidateGeneral + candidateTech + candidatePro == 0.0
        ) {
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
