package id.mport.maps.domain.unit

object UnitFormatter {
    fun distance(meters: Double, unit: UnitMode): String = when (unit) {
        UnitMode.METRIC -> if (meters < 1000) "%.1f m".format(meters)
        else "%.2f km".format(meters / 1000)
        UnitMode.IMPERIAL -> {
            val ft = meters * 3.280839895
            if (ft < 5280) "%.1f ft".format(ft) else "%.2f mi".format(ft / 5280)
        }
    }

    fun area(sqMeters: Double, unit: UnitMode): String = when (unit) {
        UnitMode.METRIC -> if (sqMeters < 10_000) "%.1f m²".format(sqMeters)
        else "%.2f ha".format(sqMeters / 10_000)
        UnitMode.IMPERIAL -> "%.2f ft²".format(sqMeters * 10.7639104)
    }
}
