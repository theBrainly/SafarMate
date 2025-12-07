package com.example.mypp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service interface defining endpoints for the SafarMate app
 * based on the deployed backend API
 */
interface ApiService {
    // Bus related endpoints
    @GET("buses/{busId}/location")
    suspend fun getBusLocation(@Path("busId") busId: String): Response<BusLocationResponse>

    @GET("buses/{busId}/{stopId}")
    suspend fun getBusETA(@Path("busId") busId: String, @Path("stopId") stopId: String): Response<ETAResponse>
    
    @GET("buses/{stopId}")
    suspend fun getBusesOnRoute(@Path("stopId") stopId: String): Response<BusesOnRouteResponse>
    
    // Route related endpoints
    @GET("routes/stops")
    suspend fun getAllStops(): Response<BusStopsResponse>
    
    @GET("routes/stops/{stopId}")
    suspend fun getRouteForStop(@Path("stopId") stopId: String): Response<RouteForStopResponse>
    
    // ETA calculation endpoint
    @GET("routes")
    suspend fun getEtaBetweenPoints(
        @Query("orig_lat") origLat: Double,
        @Query("orig_lng") origLng: Double,
        @Query("dest_lat") destLat: Double,
        @Query("dest_lng") destLng: Double
    ): Response<ETAResponse>
}
