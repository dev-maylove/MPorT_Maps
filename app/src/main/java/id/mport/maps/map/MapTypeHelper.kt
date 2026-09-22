package id.mport.maps.map

import com.google.maps.android.compose.MapType

object MapTypeHelper {
    fun fromId(id: Int): MapType = when (id) {
        2 -> MapType.SATELLITE
        3 -> MapType.TERRAIN
        4 -> MapType.HYBRID
        else -> MapType.NORMAL
    }

    fun label(id: Int): String = when (id) {
        2 -> "Satellite"
        3 -> "Terrain"
        4 -> "Hybrid"
        else -> "Normal"
    }
}
