package id.mport.maps.domain.measurement

import kotlin.math.*

object AreaCalculator {
    private const val EARTH_RADIUS = 6_371_008.8

    /** Spherical excess / planar projection hybrid suitable for small survey areas. */
    fun polygonArea(points: List<MeasurementPoint>): Double {
        if (points.size < 3) return 0.0
        val avgLat = Math.toRadians(points.map { it.latitude }.average())
        var sum = 0.0
        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            val x1 = Math.toRadians(p1.longitude) * EARTH_RADIUS * cos(avgLat)
            val y1 = Math.toRadians(p1.latitude) * EARTH_RADIUS
            val x2 = Math.toRadians(p2.longitude) * EARTH_RADIUS * cos(avgLat)
            val y2 = Math.toRadians(p2.latitude) * EARTH_RADIUS
            sum += x1 * y2 - x2 * y1
        }
        return abs(sum) / 2.0
    }

    fun perimeter(points: List<MeasurementPoint>): Double {
        if (points.size < 2) return 0.0
        val open = DistanceCalculator.pathLength(points)
        return if (points.size >= 3) {
            open + DistanceCalculator.haversine(points.last(), points.first())
        } else open
    }
}
