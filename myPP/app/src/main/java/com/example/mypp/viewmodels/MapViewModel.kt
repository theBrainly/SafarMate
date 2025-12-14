package com.example.mypp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypp.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.lang.Math.round

/**
 * ViewModel to handle map-related data and operations
 * Updated to work with the SafarMate backend API
 */
class MapViewModel : ViewModel() {
    private val TAG = "MapViewModel"
    private val repository = SafarMateRepository()
    
    // Bus stops
    private val _stops = MutableStateFlow<List<Stop>>(emptyList())
    val stops: StateFlow<List<Stop>> = _stops
    
    // Selected bus location
    private val _selectedBusLocation = MutableStateFlow<NetworkResult<BusLocationResponse>>(NetworkResult.Loading)
    val selectedBusLocation: StateFlow<NetworkResult<BusLocationResponse>> = _selectedBusLocation
    
    // Selected stop
    private val _selectedStop = MutableStateFlow<Stop?>(null)
    val selectedStop: StateFlow<Stop?> = _selectedStop
    
    // Selected bus ID
    private val _selectedBusId = MutableStateFlow<String?>(null)
    val selectedBusId: StateFlow<String?> = _selectedBusId
    
    // Buses for selected stop
    private val _busesForStop = MutableStateFlow<List<String>>(emptyList())
    val busesForStop: StateFlow<List<String>> = _busesForStop
    
    // ETA data
    private val _eta = MutableStateFlow<NetworkResult<ETAResponse>>(NetworkResult.Loading)
    val eta: StateFlow<NetworkResult<ETAResponse>> = _eta
    
    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Bus location polling job
    private var busLocationPollingJob: Job? = null
    
    init {
        fetchBusStops()
    }
    
    /**
     * Fetch all bus stops from the API
     */
    fun fetchBusStops() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getAllStops().collect { result ->
                _isLoading.value = false
                
                when (result) {
                    is NetworkResult.Success -> {
                        _stops.value = result.data.data.stops
                        Log.d(TAG, "Loaded ${result.data.data.stops.size} bus stops")
                    }
                    is NetworkResult.Error -> {
                        _error.value = result.message
                        Log.e(TAG, "Error loading bus stops: ${result.message}")
                    }
                    is NetworkResult.Loading -> {
                        // Already handled by setting _isLoading above
                    }
                }
            }
        }
    }
    
    /**
     * Select a stop and fetch buses for that stop
     */
    fun selectStop(stop: Stop) {
        _selectedStop.value = stop
        // Cancel any existing bus location polling when a new stop is selected
        busLocationPollingJob?.cancel()
        _selectedBusId.value = null
        fetchBusesForStop(stop.stop_id)
    }
    
    /**
     * Select a bus for tracking
     */
    fun selectBus(busId: String) {
        _selectedBusId.value = busId
        // Start polling for location updates
        startBusLocationPolling(busId)
        
        // If a stop is selected, calculate ETA
        val stop = _selectedStop.value
        if (stop != null) {
            calculateETA(busId, stop.stop_id)
        }
    }
    
    /**
     * Fetch buses for a selected stop
     */
    private fun fetchBusesForStop(stopId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getBusesOnRoute(stopId).collect { result ->
                _isLoading.value = false
                
                when (result) {
                    is NetworkResult.Success -> {
                        _busesForStop.value = result.data.data.buses
                        Log.d(TAG, "Loaded ${result.data.data.buses.size} buses for stop $stopId")
                    }
                    is NetworkResult.Error -> {
                        _error.value = result.message
                        Log.e(TAG, "Error loading buses for stop $stopId: ${result.message}")
                    }
                    is NetworkResult.Loading -> {
                        // Already handled by setting _isLoading above
                    }
                }
            }
        }
    }
    
    /**
     * Start polling for bus location updates
     */
    private fun startBusLocationPolling(busId: String) {
        // Cancel any existing polling job
        busLocationPollingJob?.cancel()
        
        busLocationPollingJob = viewModelScope.launch {
            while (true) {
                repository.getBusLocation(busId).collect { result ->
                    _selectedBusLocation.value = result
                    
                    // If we have location data and a selected stop, recalculate ETA
                    if (result is NetworkResult.Success && _selectedStop.value != null) {
                        calculateETA(busId, _selectedStop.value!!.stop_id)
                    }
                }
                // Poll every 5 seconds
                delay(5000)
            }
        }
    }
    
    /**
     * Calculate ETA from bus to stop using the API
     */
    private fun calculateETA(busId: String, stopId: String) {
        viewModelScope.launch {
            repository.getBusETA(busId, stopId).collect { result ->
                _eta.value = result
                if (result is NetworkResult.Success) {
                    Log.d(TAG, "ETA calculated: ${result.data.data.duration_seconds} seconds")
                }
            }
        }
    }
    
    /**
     * Calculate ETA between two points
     */
    fun calculateEtaBetweenPoints(origLat: Double, origLng: Double, destLat: Double, destLng: Double) {
        viewModelScope.launch {
            repository.getEtaBetweenPoints(origLat, origLng, destLat, destLng).collect { result ->
                _eta.value = result
            }
        }
    }
    
    /**
     * Get ETA minutes (converted from seconds)
     */
    fun getEtaMinutes(): Int? {
        val etaResult = _eta.value
        if (etaResult is NetworkResult.Success) {
            val seconds = etaResult.data.data.duration_seconds
            return kotlin.math.round(seconds / 60).toInt()
        }
        return null
    }
    
    /**
     * Clear selections
     */
    fun clearSelections() {
        busLocationPollingJob?.cancel()
        _selectedStop.value = null
        _selectedBusId.value = null
        _busesForStop.value = emptyList()
        _eta.value = NetworkResult.Loading
    }
    
    /**
     * Refresh all data
     */
    fun refreshData() {
        fetchBusStops()
        val currentStop = _selectedStop.value
        if (currentStop != null) {
            fetchBusesForStop(currentStop.stop_id)
            
            val currentBus = _selectedBusId.value
            if (currentBus != null) {
                // Restart location polling to get fresh data
                startBusLocationPolling(currentBus)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        busLocationPollingJob?.cancel()
    }
}