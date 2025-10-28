package ee.ut.cs.HEALTH.domain.model.remote

import androidx.compose.ui.graphics.Path
import ee.ut.cs.HEALTH.data.local.dto.ExerciseDetailDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ExerciseApi {
    @Headers(
        "x-rapidapi-key: 88e9686b77msh96130061184f831p1e33b3jsn5ffd232cda34",
        "x-rapidapi-host: exercisedb-api1.p.rapidapi.com"
    )
    @GET("api/v1/exercises/search")
    suspend fun searchExercises(@Query("name") exerciseName: String): Response<List<ExerciseDetailDto>>


}

