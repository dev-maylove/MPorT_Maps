package id.mport.maps.domain.measurement

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.abs

object AreaCalculator {

    /**
     * Geodesic polygon area on the WGS84 sphere (via Maps Utils SphericalUtil).
     * Accurate for survey-scale areas and handles dateline correctly better than planar projection.
     */
    fun polygonArea(points: List<MeasurementPoint>): Double {
        if (points.size < 3) return 0.0
        val path = points.map { LatLng(it.latitude, it.longitude) }
        return abs(SphericalUtil.computeArea(path))
    }

    fun perimeter(points: List<MeasurementPoint>): Double {
        if (points.size < 2) return 0.0
        val open = DistanceCalculator.pathLength(points)
        return if (points.size >= 3) {
            open + DistanceCalculator.haversine(points.last(), points.first())
        } else open
    }
}
