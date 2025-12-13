package ee.ut.cs.HEALTH.domain.model.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExerciseApi {
    /**
     * Fetches a list of exercises matching a given name.
     * The API is expected to return an array of exercises, even if only one matches.
     *
     * This will generate a URL like:  /exercises/search?search=some_exercise
     */
    @GET("exercises/search")
    suspend fun searchExercisesByName(
        @Query("search") name: String
    ): Response<ApiResponse>


}