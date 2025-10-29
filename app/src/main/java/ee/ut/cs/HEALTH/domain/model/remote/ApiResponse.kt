package ee.ut.cs.HEALTH.domain.model.remote

import com.google.gson.annotations.SerializedName
import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto

/**
 * Represents the top-level structure of the API response.
 *
 * This data class is designed to match the JSON object returned by the API,
 * which wraps the actual list of exercises within a parent object.
 */
data class ApiResponse(
    /**
     * The list of exercises returned by the API.
     * The @SerializedName("data") annotation maps this property to the "data" key
     * in the JSON response, allowing Gson to correctly parse the nested list.
     */
    @SerializedName("data")
    val exercises: List<ExerciseDetailDto>
)