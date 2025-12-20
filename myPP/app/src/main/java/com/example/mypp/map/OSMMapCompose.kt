package com.example.mypp.map

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * OSMDroid map component for Jetpack Compose
 */
@Composable
fun OSMMap(
    modifier: Modifier = Modifier,
    context: Context,
    onMapReady: (MapView) -> Unit = {},
    initialLocation: GeoPoint = GeoPoint(21.0000, 78.0000), // Center of India by default
    initialZoomLevel: Double = 5.0
) {
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(initialZoomLevel)
                controller.setCenter(initialLocation)
            }
        },
        modifier = modifier,
        update = { mapView ->
            mapView.controller.setCenter(initialLocation)
            mapView.controller.setZoom(initialZoomLevel)
            onMapReady(mapView)
        }
    )
}

/**
 * Helper functions for map operations
 */
object OSMMapUtils {
    /**
     * Add a marker to the map
     */
    fun addMarker(
        mapView: MapView, 
        position: GeoPoint, 
        title: String? = null, 
        snippet: String? = null,
        icon: Drawable? = null,
        onMarkerClick: ((Marker) -> Boolean)? = null
    ): Marker {
        val marker = Marker(mapView)
        marker.position = position
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title?.let { marker.title = it }
        snippet?.let { marker.snippet = it }
        icon?.let { marker.icon = it }
        onMarkerClick?.let { callback ->
            marker.setOnMarkerClickListener { marker, _ ->
                callback(marker)
                true
            }
        }
        
        mapView.overlays.add(marker)
        mapView.invalidate()
        return marker
    }
    
    /**
     * Add a polyline (route) to the map
     */
    fun addRoute(
        mapView: MapView,
        points: List<GeoPoint>,
        color: Int,
        width: Float = 5f
    ): Polyline {
        val line = Polyline(mapView)
        line.setPoints(points)
        line.outlinePaint.color = color
        line.outlinePaint.strokeWidth = width
        
        mapView.overlays.add(line)
        mapView.invalidate()
        return line
    }
    
    /**
     * Clear all overlays from the map
     */
    fun clearMap(mapView: MapView) {
        mapView.overlays.clear()
        mapView.invalidate()
    }
    
    /**
     * Convert coordinates to GeoPoint
     */
    fun createGeoPoint(lat: Double, lng: Double): GeoPoint {
        return GeoPoint(lat, lng)
    }
    
    /**
     * Center the map on a location with animation
     */
    fun animateTo(mapView: MapView, point: GeoPoint, zoomLevel: Double? = null) {
        mapView.controller.animateTo(point)
        zoomLevel?.let { mapView.controller.setZoom(it) }
    }
}