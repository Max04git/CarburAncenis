package com.max04.carburancenis.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.max04.carburancenis.data.AppContainer
import com.max04.carburancenis.data.GeocodingRepository
import com.max04.carburancenis.data.PrixCarburantsRepository
import com.max04.carburancenis.data.UserPreferences
import com.max04.carburancenis.data.model.FavoriteCity
import com.max04.carburancenis.data.model.GeocodedCity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val stations: List<StationUi>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(
    private val repository: PrixCarburantsRepository = AppContainer.repository,
    private val userPreferences: UserPreferences = AppContainer.userPreferences,
    private val geocodingRepository: GeocodingRepository = AppContainer.geocodingRepository,
) : ViewModel() {

    private val _selectedCity = MutableStateFlow(
        CitySelection(
            label = UserPreferences.DEFAULT_CUSTOM_CITY_LABEL,
            latitude = UserPreferences.DEFAULT_CUSTOM_CITY_LAT,
            longitude = UserPreferences.DEFAULT_CUSTOM_CITY_LON,
        )
    )
    val selectedCity: StateFlow<CitySelection> = _selectedCity.asStateFlow()

    private val _citySuggestions =
        MutableStateFlow<List<GeocodedCity>>(emptyList())
    val citySuggestions: StateFlow<List<GeocodedCity>> =
        _citySuggestions.asStateFlow()

    private val _favoriteCities = MutableStateFlow<List<FavoriteCity>>(emptyList())
    val favoriteCities: StateFlow<List<FavoriteCity>> = _favoriteCities.asStateFlow()

    private var suggestionsJob: Job? = null

    private val _selectedFuel = MutableStateFlow(FuelUi.DIESEL)
    val selectedFuel: StateFlow<FuelUi> = _selectedFuel.asStateFlow()

    private val _selectedDistanceKm = MutableStateFlow(5)
    val selectedDistanceKm: StateFlow<Int> = _selectedDistanceKm.asStateFlow()

    private val _selectedMaxAgeDays = MutableStateFlow(2L)
    val selectedMaxAgeDays: StateFlow<Long> = _selectedMaxAgeDays.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.customCityLabel,
                userPreferences.customCityLatitude,
                userPreferences.customCityLongitude,
            ) { customLabel, customLat, customLon ->
                CitySelection(
                    label = customLabel,
                    latitude = customLat,
                    longitude = customLon,
                )
            }
                .distinctUntilChanged()
                .collectLatest { city ->
                    _selectedCity.update { city }
                }
        }

        viewModelScope.launch {
            userPreferences.selectedFuelName
                .distinctUntilChanged()
                .collectLatest { fuelName ->
                    val fuel = fuelName.toFuelUiOrNull() ?: FuelUi.DIESEL
                    _selectedFuel.update { fuel }
                }
        }

        viewModelScope.launch {
            userPreferences.favoriteCities
                .distinctUntilChanged()
                .collectLatest { favorites ->
                    _favoriteCities.value = favorites
                }
        }

        viewModelScope.launch {
            userPreferences.selectedDistanceKm
                .distinctUntilChanged()
                .collectLatest { km ->
                    _selectedDistanceKm.update { km }
                }
        }

        viewModelScope.launch {
            userPreferences.selectedMaxAgeDays
                .distinctUntilChanged()
                .collectLatest { days ->
                    _selectedMaxAgeDays.update { days }
                }
        }

        viewModelScope.launch {
            combine(
                selectedCity,
                selectedFuel,
                selectedDistanceKm,
                selectedMaxAgeDays
            ) { city, fuel, distanceKm, maxAgeDays ->
                Params(city, fuel, distanceKm, maxAgeDays)
            }.collectLatest { params ->
                refresh(
                    city = params.city,
                    fuel = params.fuel,
                    distanceKm = params.distanceKm,
                    maxAgeDays = params.maxAgeDays,
                )
            }
        }
    }


    fun toggleFavoriteFromSuggestion(city: GeocodedCity) {
        toggleFavorite(
            FavoriteCity(
                label = city.label.toFavoriteCityLabel(),
                latitude = city.latitude,
                longitude = city.longitude,
            )
        )
    }

    fun removeFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            val updated = _favoriteCities.value
                .filterNot { it.label.equals(city.label, ignoreCase = true) }
            userPreferences.setFavoriteCities(updated)
        }
    }

    fun toggleFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            val exists =
                _favoriteCities.value.any { it.label.equals(city.label, ignoreCase = true) }
            if (exists) {
                removeFavorite(city)
            } else {
                addFavorite(city)
            }
        }
    }

    fun selectFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            userPreferences.setCustomCity(
                label = city.label,
                latitude = city.latitude,
                longitude = city.longitude,
            )
        }
    }

    private fun addFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            val exists =
                _favoriteCities.value.any { it.label.equals(city.label, ignoreCase = true) }
            if (exists) return@launch

            val updated = _favoriteCities.value + city
            userPreferences.setFavoriteCities(updated)
        }
    }

    fun onCityQueryChanged(query: String) {
        suggestionsJob?.cancel()
        if (query.trim().length < 2) {
            _citySuggestions.value = emptyList()
            return
        }

        suggestionsJob = viewModelScope.launch {
            _citySuggestions.value = geocodingRepository.suggestCities(query)
        }
    }

    fun selectSuggestedCity(city: GeocodedCity) {
        viewModelScope.launch {
            userPreferences.setCustomCity(
                label = city.label,
                latitude = city.latitude,
                longitude = city.longitude,
            )
            _citySuggestions.value = emptyList()
        }
    }

    fun onFuelSelected(fuel: FuelUi) {
        _selectedFuel.update { fuel }
        viewModelScope.launch {
            userPreferences.setSelectedFuelName(fuel.name)
        }
    }

    fun onDistanceSelected(distanceKm: Int) {
        _selectedDistanceKm.update { distanceKm }
        viewModelScope.launch {
            userPreferences.setSelectedDistanceKm(distanceKm)
        }
    }

    fun onMaxAgeSelected(maxAgeDays: Long) {
        _selectedMaxAgeDays.update { maxAgeDays }
        viewModelScope.launch {
            userPreferences.setSelectedMaxAgeDays(maxAgeDays)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun refresh(
        city: CitySelection,
        fuel: FuelUi,
        distanceKm: Int,
        maxAgeDays: Long,
    ) {
        _uiState.value = HomeUiState.Loading
        try {
            val stations = repository.loadStationsAroundWithFuel(
                lat = city.latitude,
                lon = city.longitude,
                rangeMeters = distanceKm * 1000,
                fuelShortName = fuel.apiShortName,
                maxAgeDays = maxAgeDays,
            )
            _uiState.value = HomeUiState.Success(stations)
        } catch (t: Throwable) {
            _uiState.value = HomeUiState.Error(t.message ?: "Erreur réseau")
        }
    }
}

private data class Params(
    val city: CitySelection,
    val fuel: FuelUi,
    val distanceKm: Int,
    val maxAgeDays: Long,
)

data class CitySelection(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

private fun String.toFuelUiOrNull(): FuelUi? {
    return try {
        FuelUi.valueOf(this)
    } catch (_: Throwable) {
        null
    }
}

private fun String.toFavoriteCityLabel(): String {
    return this
        .trim()
        .substringBefore(',')
        .trim()
}

private val FuelUi.apiShortName: String
    get() = when (this) {
        FuelUi.DIESEL -> "Gazole"
        FuelUi.SP95 -> "SP95"
        FuelUi.SP98 -> "SP98"
        FuelUi.E10 -> "SP95-E10"
        FuelUi.E85 -> "E85"
    }
