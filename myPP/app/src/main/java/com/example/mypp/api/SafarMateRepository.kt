package com.example.mypp.api

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * Repository to handle all API calls and error handling
 * Updated to match the SafarMate API deployed on AWS
 */
class SafarMateRepository {
    private val apiService = RetrofitClient.instance
    private val TAG = "SafarMateRepository"
    
    /**
     * Get bus location with network result handling
     */
    suspend fun getBusLocation(busId: String): Flow<NetworkResult<BusLocationResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getBusLocation(busId)
            emit(handleApiResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bus location: ${e.message}")
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }
    
    /**
     * Get all bus stops
     */
    suspend fun getAllStops(): Flow<NetworkResult<BusStopsResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAllStops()
            emit(handleApiResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bus stops: ${e.message}")
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }
    
    /**
     * Get buses on route for a specific stop
     */
    suspend fun getBusesOnRoute(stopId: String): Flow<NetworkResult<BusesOnRouteResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getBusesOnRoute(stopId)
            emit(handleApiResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting buses on route: ${e.message}")
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }
    
    /**
     * Get route information for a specific stop
     */
    suspend fun getRouteForStop(stopId: String): Flow<NetworkResult<RouteForStopResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getRouteForStop(stopId)
            emit(handleApiResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting route for stop: ${e.message}")
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }
    
    /**
     * Get ETA from bus to stop
     */
    suspend fun getBusETA(busId: String, stopId: String): Flow<NetworkResult<ETAResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getBusETA(busId, stopId)
            emit(handleApiResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bus ETA: ${e.message}")
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }
    
    /**
     * Get ETA between two points
     */
    suspend fun getEtaBetweenPoints(
        origLat: Double,
        origLng: Double,
        destLat: Double,
        destLng: Double
    ): Flow<NetworkResult<ETAResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getEtaBetweenPoints(origLat, origLng, destLat, destLng)
            emit(handleApiResponse(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ETA between points: ${e.message}")
            emit(NetworkResult.Error("Network error: ${e.message}"))
        }
    }
    
    /**
     * Handle API response and convert to NetworkResult
     */
    private fun <T> handleApiResponse(response: Response<T>): NetworkResult<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Response body is empty")
            }
        } else {
            val errorMessage = when (response.code()) {
                400 -> "Bad request"
                401 -> "Unauthorized"
                403 -> "Forbidden"
                404 -> "Not found"
                500 -> "Server error"
                502 -> "Bad gateway - OSRM service may be unavailable"
                else -> "Unknown error: ${response.message()}"
            }
            NetworkResult.Error(errorMessage, response.code())
        }
    }
}