package id.mport.maps.map

import android.content.Context
import com.google.maps.android.compose.MapType
import id.mport.maps.R

object MapTypeHelper {
    fun fromId(id: Int): MapType = when (id) {
        2 -> MapType.SATELLITE
        3 -> MapType.TERRAIN
        4 -> MapType.HYBRID
        else -> MapType.NORMAL
    }

    /** Localized label for the given map type id. */
    fun label(context: Context, id: Int): String = context.getString(labelRes(id))

    /** String resource id for the given map type (usable from Compose with stringResource). */
    fun labelRes(id: Int): Int = when (id) {
        2 -> R.string.map_satellite
        3 -> R.string.map_terrain
        4 -> R.string.map_hybrid
        else -> R.string.map_normal
    }
}
