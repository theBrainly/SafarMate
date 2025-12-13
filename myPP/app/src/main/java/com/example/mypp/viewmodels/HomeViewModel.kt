package com.example.mypp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mypp.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the home screen with real API integration
 */
class HomeViewModel : ViewModel() {
    
    private val TAG = "HomeViewModel"
    private val repository = SafarMateRepository()
    
    // Store a list of bus stops
    private val _busStops = MutableStateFlow<List<Stop>>(emptyList())
    val busStops: StateFlow<List<Stop>> = _busStops.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Selected stop
    private val _selectedStop = MutableStateFlow<Stop?>(null)
    val selectedStop: StateFlow<Stop?> = _selectedStop.asStateFlow()
    
    // Buses for selected stop
    private val _busesForStop = MutableStateFlow<List<String>>(emptyList())
    val busesForStop: StateFlow<List<String>> = _busesForStop.asStateFlow()
    
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
                        _busStops.value = result.data.data.stops
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
        fetchBusesForStop(stop.stop_id)
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
     * Clear the selected stop
     */
    fun clearSelectedStop() {
        _selectedStop.value = null
        _busesForStop.value = emptyList()
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        fetchBusStops()
        val currentStop = _selectedStop.value
        if (currentStop != null) {
            fetchBusesForStop(currentStop.stop_id)
        }
    }
}
