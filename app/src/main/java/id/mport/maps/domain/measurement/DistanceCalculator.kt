package id.mport.maps.domain.measurement

import kotlin.math.*

object DistanceCalculator {
    private const val EARTH_RADIUS = 6_371_008.8

    fun haversine(a: MeasurementPoint, b: MeasurementPoint): Double {
        val p1 = Math.toRadians(a.latitude)
        val p2 = Math.toRadians(b.latitude)
        val dLat = p2 - p1
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(p1) * cos(p2) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    fun pathLength(points: List<MeasurementPoint>): Double {
        if (points.size < 2) return 0.0
        return points.zipWithNext().sumOf { haversine(it.first, it.second) }
    }
}
