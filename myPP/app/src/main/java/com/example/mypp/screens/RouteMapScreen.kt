package com.example.mypp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypp.api.BusLocationResponse
import com.example.mypp.api.ETAResponse
import com.example.mypp.api.NetworkResult
import com.example.mypp.api.Stop
import com.example.mypp.map.OSMMap
import com.example.mypp.map.OSMMapUtils
import com.example.mypp.ui.theme.MyPPTheme
import com.example.mypp.viewmodels.MapViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    routeId: String? = null,
    mapViewModel: MapViewModel = viewModel(),
    onBackClicked: () -> Unit,
    onBookTicketClicked: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    // Initialize OSMDroid configuration
    Configuration.getInstance().userAgentValue = context.packageName

    // Collect states from ViewModel
    val stops by mapViewModel.stops.collectAsState()
    val selectedBusLocation by mapViewModel.selectedBusLocation.collectAsState()
    val selectedStop by mapViewModel.selectedStop.collectAsState()
    val busesForStop by mapViewModel.busesForStop.collectAsState()
    val eta by mapViewModel.eta.collectAsState()
    val isLoading by mapViewModel.isLoading.collectAsState()
    val error by mapViewModel.error.collectAsState()

    // State for map view
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // State for selected bus
    var selectedBusId by remember { mutableStateOf<String?>(null) }

    // Effect to load route and buses when routeId changes
    LaunchedEffect(routeId) {
        if (routeId != null) {
            // Assuming routeId corresponds to a stopId in your new logic
            stops.find { it.stop_id == routeId }?.let { stop ->
                mapViewModel.selectStop(stop)
            }
        }
    }

    // Effect to update markers when bus is selected
    LaunchedEffect(selectedBusId) {
        selectedBusId?.let { mapViewModel.selectBus(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OSMMap(
            modifier = Modifier.fillMaxSize(),
            context = context,
            onMapReady = { map ->
                mapView = map
                updateMapOverlays(
                    context = context,
                    mapView = map,
                    stops = stops,
                    selectedStop = selectedStop,
                    busesForStop = busesForStop,
                    selectedBusLocation = selectedBusLocation,
                    onBusSelected = { busId -> selectedBusId = busId }
                )
            }
        )

        TopAppBar(
            title = { Text("Route Map") },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ),
            modifier = Modifier.statusBarsPadding()
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RouteInfoCard(
                selectedStop = selectedStop,
                eta = eta,
                onRefreshClicked = { mapViewModel.refreshData() },
                onBookTicketClicked = onBookTicketClicked
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        error?.let {
            Snackbar(action = { Button(onClick = { mapViewModel.refreshData() }) { Text("Retry") } }) {
                Text(it)
            }
        }
    }
}

private fun updateMapOverlays(
    context: Context,
    mapView: MapView,
    stops: List<Stop>,
    selectedStop: Stop?,
    busesForStop: List<String>,
    selectedBusLocation: NetworkResult<BusLocationResponse>,
    onBusSelected: (String) -> Unit
) {
    OSMMapUtils.clearMap(mapView)

    // Add stop markers
    stops.forEach { stop ->
        val stopPoint = GeoPoint(stop.lat.toDouble(), stop.lng.toDouble())
        OSMMapUtils.addMarker(
            mapView = mapView,
            position = stopPoint,
            title = stop.name,
            snippet = "Stop ID: ${stop.stop_id}"
        )
    }

    // Add bus markers for the selected stop
    if (selectedBusLocation is NetworkResult.Success) {
        busesForStop.forEach { busId ->
            val busData = selectedBusLocation.data.data
            if (busId == busData.busId) { // Swapped operands to force re-evaluation
                val busPoint = GeoPoint(busData.lat.toDouble(), busData.lng.toDouble())
                val busMarker = OSMMapUtils.addMarker(
                    mapView = mapView,
                    position = busPoint,
                    title = "Bus $busId",
                    snippet = "Speed: ${busData.speed ?: "N/A"} km/h"
                )
                busMarker.setOnMarkerClickListener { _, _ ->
                    onBusSelected(busId)
                    true
                }
            }
        }
    }

    // Center map on selected stop
    selectedStop?.let {
        val stopPoint = GeoPoint(it.lat.toDouble(), it.lng.toDouble())
        OSMMapUtils.animateTo(mapView, stopPoint, 15.0)
    }
}

@Composable
fun RouteInfoCard(
    selectedStop: Stop?,
    eta: NetworkResult<ETAResponse>,
    onRefreshClicked: () -> Unit,
    onBookTicketClicked: (routeId: String, amount: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (selectedStop != null) {
                Text(
                    text = selectedStop.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (eta is NetworkResult.Success) {
                    val etaMinutes = (eta.data.data.duration_seconds / 60).toInt()
                    Text(
                        text = "ETA: $etaMinutes minutes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onRefreshClicked,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Refresh")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            onBookTicketClicked(selectedStop.stop_id, "10") // Assuming a default fare
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Book Ticket")
                    }
                }
            } else {
                Text("Select a route to see details.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouteMapScreenPreview() {
    MyPPTheme {
        RouteMapScreen(onBackClicked = {})
    }
}
