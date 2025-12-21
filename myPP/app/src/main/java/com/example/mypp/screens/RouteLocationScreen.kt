package com.example.mypp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.preference.PreferenceManager
import com.example.mypp.ui.theme.MyPPTheme // Using your theme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteLocationScreen(
    onNavigateUp: () -> Unit
) {
    val busLocation = GeoPoint(19.0760, 72.8777)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bus Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            OsmDroidMap(
                modifier = Modifier.fillMaxSize(),
                startPoint = busLocation
            )
        }
    }
}

@Composable
fun OsmDroidMap(modifier: Modifier = Modifier, startPoint: GeoPoint) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(startPoint)

            val marker = Marker(this).apply {
                position = startPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Bus Location"
            }
            overlays.add(marker)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun RouteLocationScreenPreview() {
    // Corrected to use your theme name
    MyPPTheme {
        RouteLocationScreen(onNavigateUp = {})
    }
}