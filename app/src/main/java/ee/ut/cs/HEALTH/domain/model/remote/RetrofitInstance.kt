package ee.ut.cs.HEALTH.domain.model.remote

import ee.ut.cs.HEALTH.BuildConfig

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



/**
 * A singleton object to manage the Retrofit instance and provide a centralized
 * API service for the entire application.
 *
 * This setup includes an OkHttp Interceptor to automatically add the required
 * authentication headers to every outgoing request for the RapidAPI service.
 */
object RetrofitInstance {

    /**
     * The base URL for the ExerciseDB API service. All API endpoints will be
     * relative to this URL.
     */
    private const val BASE_URL = "https://exercisedb-api1.p.rapidapi.com/"

    /**
     * Configures the OkHttpClient, which acts as the underlying HTTP client for Retrofit.
     *
     * An interceptor is added to this client. Its job is to "intercept" every
     * outgoing request and modify it before it's sent to the server. Here, we use
     * it to inject the necessary API key and host headers required by RapidAPI.
     */
    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        // Get the original request that was about to be sent.
        val originalRequest = chain.request()

        // Build a new request from the original, but add custom headers.
        val newRequest = originalRequest.newBuilder()
            .header("X-RapidAPI-Key", BuildConfig.RAPIDAPI_KEY) // Your unique API key.
            .header("X-RapidAPI-Host", "exercisedb-api1.p.rapidapi.com") // The host server for the API.
            .build()

        // Proceed with sending the modified request.
        chain.proceed(newRequest)
    }.build()

    /**
     * A lazy-initialized instance of the ExerciseApi interface.
     *
     * 'lazy' ensures that the Retrofit object is created only once, the very first
     * time it is accessed, making it efficient.
     *
     * This instance is configured with:
     * - The base URL for the API.
     * - The custom OkHttpClient that includes our authentication headers.
     * - A Gson converter to automatically parse JSON responses into Kotlin data classes.
     */
    val api: ExerciseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // This is crucial for using our custom interceptor.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApi::class.java)
    }
}
