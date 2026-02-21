package com.max04.carburancenis.data

import android.content.Context
import com.max04.carburancenis.data.network.NetworkModule

object AppContainer {
    private var initialized = false

    private val api by lazy { NetworkModule.createApi() }
    private val nominatimApi by lazy { NetworkModule.createNominatimApi() }

    val repository: PrixCarburantsRepository by lazy {
        PrixCarburantsRepository(api)
    }

    val geocodingRepository: GeocodingRepository by lazy {
        GeocodingRepository(nominatimApi)
    }

    lateinit var userPreferences: UserPreferences
        private set

    fun init(context: Context) {
        if (initialized) return
        userPreferences = UserPreferences.create(context)
        initialized = true
    }
}
