package ee.ut.cs.HEALTH.domain.model.remote


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface ExerciseApi {
    /**
     * Fetches a list of exercises matching a given name.
     * The API is expected to return an array of exercises, even if only one matches.
     *
     * This will generate a URL like:  /exercises/search?search=some_exercise
     */
    //good name search, no data after id, name and url
    @GET("exercises/search")
    suspend fun searchExercisesByName(
        @Query("search") name: String
    ): Response<ExerciseListResponse>

    // full info, one response data
    @GET("exercises/{id}")
    suspend fun getExercisesById(
        @Path("id") exerciseId: String
    ): Response<SingleExerciseResponse>

    //do not want this, bad search
    @GET("exercises")
    suspend fun searchExercisesByBestName(
        @Query("name") name: String
    ): Response<ApiResponse>


}