package ee.ut.cs.HEALTH.domain.model.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://exercisedb-api1.p.rapidapi.com/"

    private val client = OkHttpClient.Builder().build()

    val api: ExerciseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApi::class.java)
    }
}


