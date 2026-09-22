package id.mport.maps.domain.measurement

data class MeasurementPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
