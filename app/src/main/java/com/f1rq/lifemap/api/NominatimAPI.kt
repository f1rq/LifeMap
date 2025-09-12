package com.f1rq.lifemap.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimAPI {
    @GET("search")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 8,
        @Query("accept-language") acceptLanguage: String = "en"
    ): List<NominatimPlace>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("accept-language") acceptLanguage: String = "en"
    ): NominatimPlace
}

data class NominatimPlace(
    val display_name: String,
    val lat: String,
    val lon: String,
    val type: String? = null,
    val category: String? = null,
    val importance: Double? = null
)