package com.max04.carburancenis.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object NetworkModule {
    private const val BASE_URL = "https://api.prix-carburants.2aaz.fr/"
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun createApi(): PrixCarburantsApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(PrixCarburantsApi::class.java)
    }

    fun createNominatimApi(): NominatimApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val withHeaders: Request = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Accept-Language", "fr")
                    .header("User-Agent", "CarburAncenis/1.0 (Android)")
                    .build()
                chain.proceed(withHeaders)
            }
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(NominatimApi::class.java)
    }
}
