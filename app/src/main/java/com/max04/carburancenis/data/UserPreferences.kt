package com.max04.carburancenis.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.max04.carburancenis.data.model.FavoriteCity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

class UserPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    val customCityLabel: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_CITY_LABEL] ?: DEFAULT_CUSTOM_CITY_LABEL
    }

    val customCityLatitude: Flow<Double> = dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_CITY_LAT] ?: DEFAULT_CUSTOM_CITY_LAT
    }

    val customCityLongitude: Flow<Double> = dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_CITY_LON] ?: DEFAULT_CUSTOM_CITY_LON
    }

    val selectedFuelName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_FUEL] ?: DEFAULT_FUEL_NAME
    }

    val selectedDistanceKm: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_DISTANCE_KM] ?: DEFAULT_DISTANCE_KM
    }

    val selectedMaxAgeDays: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_MAX_AGE_DAYS] ?: DEFAULT_MAX_AGE_DAYS
    }

    val favoriteCities: Flow<List<FavoriteCity>> = dataStore.data.map { prefs ->
        val raw = prefs[KEY_FAVORITE_CITIES] ?: "[]"
        try {
            json.decodeFromString(raw)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun setSelectedFuelName(value: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_FUEL] = value
        }
    }

    suspend fun setCustomCity(
        label: String,
        latitude: Double,
        longitude: Double,
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_CITY_LABEL] = label
            prefs[KEY_CUSTOM_CITY_LAT] = latitude
            prefs[KEY_CUSTOM_CITY_LON] = longitude
        }
    }

    suspend fun setSelectedDistanceKm(value: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_DISTANCE_KM] = value
        }
    }

    suspend fun setSelectedMaxAgeDays(value: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_MAX_AGE_DAYS] = value
        }
    }

    suspend fun setFavoriteCities(value: List<FavoriteCity>) {
        dataStore.edit { prefs ->
            prefs[KEY_FAVORITE_CITIES] = json.encodeToString(value)
        }
    }

    companion object {
        private val KEY_CUSTOM_CITY_LABEL = stringPreferencesKey("custom_city_label")
        private val KEY_CUSTOM_CITY_LAT = doublePreferencesKey("custom_city_lat")
        private val KEY_CUSTOM_CITY_LON = doublePreferencesKey("custom_city_lon")
        private val KEY_SELECTED_FUEL = stringPreferencesKey("selected_fuel")
        private val KEY_SELECTED_DISTANCE_KM = intPreferencesKey("selected_distance_km")
        private val KEY_SELECTED_MAX_AGE_DAYS = longPreferencesKey("selected_max_age_days")
        private val KEY_FAVORITE_CITIES = stringPreferencesKey("favorite_cities")

        const val DEFAULT_CUSTOM_CITY_LABEL: String = "Ville"
        const val DEFAULT_CUSTOM_CITY_LAT: Double = 47.3648141
        const val DEFAULT_CUSTOM_CITY_LON: Double = -1.1816088
        const val DEFAULT_FUEL_NAME: String = "DIESEL"
        const val DEFAULT_DISTANCE_KM: Int = 5
        const val DEFAULT_MAX_AGE_DAYS: Long = 2L

        private val json = Json {
            ignoreUnknownKeys = true
        }

        fun create(context: Context): UserPreferences {
            return UserPreferences(context.userPreferencesDataStore)
        }
    }
}
