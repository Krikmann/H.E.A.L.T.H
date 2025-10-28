package ee.ut.cs.HEALTH.domain.model.remote

import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExerciseApi {
    /**
     * Fetches a list of exercises matching a given name.
     * The API is expected to return an array of exercises, even if only one matches.
     *
     * This will generate a URL like: /exercises?name=some_exercise
     */
    @GET("exercises")
    suspend fun searchExercisesByName(
        @Query("name") name: String
    ): Response<List<ExerciseDetailDto>>

    /**
     * Fetches the details of a single exercise by its specific ID.
     * This is useful for getting detailed information for a detail screen.
     *
     * This will generate a URL like: /exercises/exercise/1301
     */
    @GET("exercises/exercise/{id}") //
    suspend fun getExerciseById(
        @Query("id") exerciseId: String
    ): Response<ExerciseDetailDto>

}

