package by.bstu.makovei.greenscan.data.off

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OffNetworkModule {
    private const val BASE = "https://world.openfoodfacts.org/"

    fun api(): OpenFoodFactsApi {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "GreenScan/1.0 (https://github.com/student-coursework; contact@example.invalid)"
                    )
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(log)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}
