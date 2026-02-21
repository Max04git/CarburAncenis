package com.max04.carburancenis.data.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface PrixCarburantsApi {
    @Headers("Accept: application/json")
    @GET("stations/around/{latLon}")
    suspend fun getStationsAround(
        @Path("latLon") latLon: String,
        @Header("Range") range: String,
    ): List<StationAroundDto>

    @Headers("Accept: application/json")
    @GET("station/{id}")
    suspend fun getStationDetails(
        @Path("id") id: Long,
    ): StationDetailsDto
}
