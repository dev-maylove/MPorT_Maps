package id.mport.maps.domain.measurement

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

object DistanceCalculator {

    fun haversine(a: MeasurementPoint, b: MeasurementPoint): Double {
        return SphericalUtil.computeDistanceBetween(
            LatLng(a.latitude, a.longitude),
            LatLng(b.latitude, b.longitude)
        )
    }

    fun pathLength(points: List<MeasurementPoint>): Double {
        if (points.size < 2) return 0.0
        return points.zipWithNext().sumOf { haversine(it.first, it.second) }
    }
}
