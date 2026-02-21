package com.max04.carburancenis.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1,
        @Query("addressdetails") addressDetails: Int = 0,
        @Query("countrycodes") countryCodes: String = "fr",
    ): List<NominatimSearchItemDto>
}
