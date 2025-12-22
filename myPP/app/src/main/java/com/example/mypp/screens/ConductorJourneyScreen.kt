package com.example.mypp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mypp.ui.theme.MyPPTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BusJourney(
    val id: String,
    val routeNumber: String,
    val source: String,
    val destination: String,
    val startTime: Date = Date(),
    var endTime: Date? = null,
    var isActive: Boolean = false,
    var passengerCount: Int = 0,
    var currentLocation: String = "",
    var totalStops: Int = 8,
    var currentStopIndex: Int = 1,
    var estimatedTimeOfArrival: String = "--:--",
    var distance: String = "--",
    var totalFare: Double = 0.0
)

/**
 * Screen for conductor to manage bus journey
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConductorJourneyScreen(
    onLogoutClicked: () -> Unit
) {
    var activeJourney by remember { mutableStateOf<BusJourney?>(null) }
    var allJourneys by remember { mutableStateOf(listOf(
        BusJourney(
            id = "J1001",
            routeNumber = "Route 101",
            source = "Central Station",
            destination = "Tech Park",
            startTime = Calendar.getInstance().apply { add(Calendar.HOUR, -3) }.time,
            endTime = Calendar.getInstance().apply { add(Calendar.HOUR, -1) }.time,
            isActive = false,
            passengerCount = 32,
            currentLocation = "Destination reached"
        ),
        BusJourney(
            id = "J1002",
            routeNumber = "Route 205",
            source = "Airport Terminal",
            destination = "City Center",
            startTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
            endTime = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1); add(Calendar.HOUR, 2) }.time,
            isActive = false,
            passengerCount = 45,
            currentLocation = "Destination reached"
        )
    )) }

    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Conductor Dashboard",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Manage your bus journeys",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Status indicator
                    if (activeJourney != null) {
                        AssistChip(
                            onClick = { /* Status info */ },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            border = null,
                            modifier = Modifier.padding(end = 8.dp),
                            label = { Text("Active Journey") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                        )
                    }

                    IconButton(onClick = onLogoutClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Active journey section with enhanced styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeJourney != null)
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsBus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                                .padding(8.dp)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (activeJourney != null) "Active Journey" else "Start New Journey",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (activeJourney != null) {
                        // Enhanced active journey details
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeJourney!!.routeNumber,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${activeJourney!!.source} ",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = activeJourney!!.destination,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                    // End journey button
                                    FilledTonalButton(
                                        onClick = {
                                            // End the journey
                                            val updatedJourney = activeJourney!!.copy(
                                                isActive = false,
                                                endTime = Date()
                                            )

                                            val updatedJourneys = allJourneys.map {
                                                if (it.id == activeJourney!!.id) updatedJourney else it
                                            }

                                            allJourneys = updatedJourneys
                                            activeJourney = null
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "End Journey",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "End",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                // Journey stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    JourneyStatCard(
                                        icon = Icons.Outlined.Person,
                                        value = "${activeJourney!!.passengerCount}",
                                        label = "Passengers",
                                        modifier = Modifier.weight(1f)
                                    )

                                    JourneyStatCard(
                                        icon = Icons.Default.Schedule,
                                        value = "${dateFormat.format(activeJourney!!.startTime)}",
                                        label = "Started",
                                        modifier = Modifier.weight(1f)
                                    )

                                    JourneyStatCard(
                                        icon = Icons.Default.LocationOn,
                                        value = "${activeJourney!!.currentStopIndex}/${activeJourney!!.totalStops}",
                                        label = "Stop",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Progress tracker
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Journey Progress",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = "${(activeJourney!!.currentStopIndex.toFloat() / activeJourney!!.totalStops.toFloat() * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Animated progress indicator
                                    val progress by animateFloatAsState(targetValue = activeJourney!!.currentStopIndex.toFloat() / activeJourney!!.totalStops.toFloat(), label = "")
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Current location: ${activeJourney!!.currentLocation}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Passenger counter controls with better styling
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Passenger Management",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Decrease passenger button
                                    OutlinedIconButton(
                                        onClick = {
                                            if (activeJourney!!.passengerCount > 0) {
                                                val updatedJourney = activeJourney!!.copy(
                                                    passengerCount = activeJourney!!.passengerCount - 1
                                                )

                                                activeJourney = updatedJourney

                                                allJourneys = allJourneys.map {
                                                    if (it.id == updatedJourney.id) updatedJourney else it
                                                }
                                            }
                                        },
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Passenger count display
                                    Text(
                                        text = "${activeJourney!!.passengerCount}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // Increase passenger button
                                    FilledIconButton(
                                        onClick = {
                                            val updatedJourney = activeJourney!!.copy(
                                                passengerCount = activeJourney!!.passengerCount + 1
                                            )

                                            activeJourney = updatedJourney

                                            allJourneys = allJourneys.map {
                                                if (it.id == updatedJourney.id) updatedJourney else it
                                            }
                                        },
                                        modifier = Modifier.size(48.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // No active journey - show enhanced start options
                        Text(
                            text = "You don't have any active journeys. Select a route to begin a new journey.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Available routes with better styling
                        val availableRoutes = listOf(
                            Route(
                                id = "R101",
                                number = "Route 101",
                                source = "Central Station",
                                destination = "Tech Park",
                                departureTime = "06:30",
                                distance = "12.5 km",
                                stops = 8
                            ),
                            Route(
                                id = "R205",
                                number = "Route 205",
                                source = "Airport Terminal",
                                destination = "City Center",
                                departureTime = "07:15",
                                distance = "18.2 km",
                                stops = 12
                            ),
                            Route(
                                id = "R305",
                                number = "Route 305",
                                source = "North Campus",
                                destination = "South Market",
                                departureTime = "08:00",
                                distance = "9.7 km",
                                stops = 6
                            )
                        )

                        Text(
                            text = "Available Routes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            availableRoutes.forEach { route ->
                                RouteCard(route = route) {
                                    // Create and start a new journey
                                    val newJourney = BusJourney(
                                        id = "J${1003 + allJourneys.size}",
                                        routeNumber = route.number,
                                        source = route.source,
                                        destination = route.destination,
                                        startTime = Date(),
                                        isActive = true,
                                        passengerCount = 0,
                                        currentLocation = route.source,
                                        totalStops = route.stops,
                                        distance = route.distance
                                    )

                                    activeJourney = newJourney
                                    allJourneys = allJourneys + newJourney

                                    // Simulate location updates and stop progression
                                    coroutineScope.launch {
                                        val locations = listOf(
                                            "Departing ${route.source}",
                                            "Approaching first stop",
                                            "Downtown crossing",
                                            "Midway point",
                                            "University road",
                                            "Final approach",
                                            "Arriving at ${route.destination}"
                                        )

                                        for ((index, location) in locations.withIndex()) {
                                            delay(30000) // 30 seconds between updates in real app
                                            activeJourney?.let {
                                                val updatedStopIndex = (index + 1).coerceAtMost(it.totalStops)
                                                val updatedJourney = it.copy(
                                                    currentLocation = location,
                                                    currentStopIndex = updatedStopIndex
                                                )

                                                activeJourney = updatedJourney

                                                allJourneys = allJourneys.map { journey ->
                                                    if (journey.id == updatedJourney.id) updatedJourney else journey
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Journey history section with better styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Journey History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Display message if no completed journeys
                    val completedJourneys = allJourneys.filter { !it.isActive }
                    if (completedJourneys.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No completed journeys yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(completedJourneys) { journey ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = journey.routeNumber,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            journey.endTime?.let {
                                                val journeyDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(it)
                                                AssistChip(
                                                    onClick = { /* Journey details */ },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                    ),
                                                    border = null,
                                                    label = { Text(journeyDate) }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Journey route
                                        Text(
                                            text = "${journey.source} to ${journey.destination}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                                        // Journey stats
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            StatItem(
                                                label = "Duration",
                                                value = journey.endTime?.let {
                                                    val durationMs = it.time - journey.startTime.time
                                                    val minutes = durationMs / 60000
                                                    "${minutes} min"
                                                } ?: "--"
                                            )

                                            StatItem(
                                                label = "Passengers",
                                                value = "${journey.passengerCount}"
                                            )

                                            StatItem(
                                                label = "Started",
                                                value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(journey.startTime)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class for route info
data class Route(
    val id: String,
    val number: String,
    val source: String,
    val destination: String,
    val departureTime: String,
    val distance: String,
    val stops: Int
)

@Composable
fun JourneyStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RouteCard(
    route: Route,
    onStartClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.number,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${route.source} to ${route.destination}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onStartClicked,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Journey",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Start")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Departure", value = route.departureTime)
                StatItem(label = "Distance", value = route.distance)
                StatItem(label = "Stops", value = "${route.stops}")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConductorJourneyScreenPreview() {
    MyPPTheme {
        ConductorJourneyScreen(
            onLogoutClicked = {}
        )
    }
}