package ee.ut.cs.HEALTH.domain.model.remote


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

/**
 * Defines the network API for fetching exercise data using Retrofit.
 *
 * This interface declares the available endpoints, their HTTP methods,
 * parameters, and expected response types.
 */
interface ExerciseApi {

    /**
     * Fetches a list of exercises that match a given search query.
     * This endpoint is designed for searching exercises by name.
     *
     * Example URL: `.../api/v1/exercises/search?search=push%20up`
     *
     * @param name The search term to find matching exercises.
     * @return A Retrofit [Response] wrapping an [ExerciseListResponse].
     */
    @GET("exercises/search")
    suspend fun searchExercisesByName(
        @Query("search") name: String
    ): Response<ExerciseListResponse>

    /**
     * Fetches the full details for a single exercise using its unique ID.
     *
     * Example URL: `.../api/v1/exercises/some-exercise-id`
     *
     * @param exerciseId The unique identifier of the exercise to retrieve.
     * @return A Retrofit [Response] wrapping a [SingleExerciseResponse].
     */
    @GET("exercises/{id}")
    suspend fun getExercisesById(
        @Path("id") exerciseId: String
    ): Response<SingleExerciseResponse>
}
