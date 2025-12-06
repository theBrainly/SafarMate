package com.example.mypp.api

/**
 * Data models aligned with the SafarMate API responses
 */

// Common response structure for all API calls
interface SafarMateResponse {
    val statusCode: Int
    val message: String
    val success: Boolean
}

// Bus Stops Response
data class BusStopsResponse(
    val data: StopsData,
    override val statusCode: Int,
    override val message: String,
    override val success: Boolean
) : SafarMateResponse

data class StopsData(
    val stops: List<Stop>
)

data class Stop(
    val stop_id: String,
    val route_id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val sequence: Int
)

// Buses on Route Response for a specific stop
data class BusesOnRouteResponse(
    val data: BusesOnRouteData,
    override val statusCode: Int,
    override val message: String,
    override val success: Boolean
) : SafarMateResponse

data class BusesOnRouteData(
    val route_id: String,
    val buses: List<String>
)

// Bus Location Response
data class BusLocationResponse(
    val data: BusLocationData,
    override val statusCode: Int,
    override val message: String,
    override val success: Boolean
) : SafarMateResponse

data class BusLocationData(
    val busId: String,
    val lat: String,
    val lng: String,
    val speed: String?,
    val heading: String?,
    val updated_at: String
)

// Route for Stop Response
data class RouteForStopResponse(
    val data: RouteForStopData,
    override val statusCode: Int,
    override val message: String,
    override val success: Boolean
) : SafarMateResponse

data class RouteForStopData(
    val stop_id: String,
    val route_id: String
)

// ETA Response for both bus-to-stop and point-to-point
data class ETAResponse(
    val data: ETAData,
    override val statusCode: Int,
    override val message: String,
    override val success: Boolean
) : SafarMateResponse

data class ETAData(
    val distance_meters: Double,
    val duration_seconds: Double,
    val provider_status: String
)

// Error Response
data class ErrorResponse(
    val data: Any?,
    override val statusCode: Int,
    override val message: String,
    override val success: Boolean,
    val error: List<String>
) : SafarMateResponse
