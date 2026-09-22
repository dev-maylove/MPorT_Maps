package id.mport.maps.domain.measurement

object PointCodec {
    fun encode(points: List<MeasurementPoint>): String =
        points.joinToString(";") { "${it.latitude},${it.longitude}" }

    fun decode(value: String): List<MeasurementPoint> =
        value.split(';')
            .mapNotNull { part ->
                val p = part.split(',')
                if (p.size < 2) null
                else runCatching {
                    MeasurementPoint(
                        latitude = p[0].toDouble(),
                        longitude = p[1].toDouble()
                    )
                }.getOrNull()
            }
}
