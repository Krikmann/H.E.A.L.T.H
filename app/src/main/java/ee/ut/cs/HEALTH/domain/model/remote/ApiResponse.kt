package ee.ut.cs.HEALTH.domain.model.remote

import com.google.gson.annotations.SerializedName
import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto


/**
 * See klass esindab API vastuse kõige välimist struktuuri.
 * Näide: { "success": true, "meta": {...}, "data": [...] }
 */
data class ApiResponse(
    @SerializedName("data") // See seob muutuja 'exercises' JSON-i võtmega 'data'
    val exercises: List<ExerciseDetailDto>
    // 'success' ja 'meta' välju võime ignoreerida, kui me neid ei vaja
)