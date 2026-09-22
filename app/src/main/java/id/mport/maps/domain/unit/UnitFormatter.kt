package id.mport.maps.domain.unit

object UnitFormatter {

    private const val M_TO_FT = 3.280839895
    private const val M_TO_YD = 1.093613298
    private const val M_TO_MI = 0.000621371192
    private const val M_TO_NMI = 0.000539956803
    private const val M2_TO_FT2 = 10.7639104167
    private const val M2_TO_YD2 = 1.1959900463
    private const val M2_TO_MI2 = 3.861021585e-7
    private const val M2_TO_NMI2 = 2.915533496e-7

    fun distance(meters: Double, unit: UnitMode): String = when (unit) {
        UnitMode.METER -> "%.1f m".format(meters)
        UnitMode.KILOMETER -> "%.3f km".format(meters / 1000.0)
        UnitMode.MILE -> "%.3f mi".format(meters * M_TO_MI)
        UnitMode.FEET -> "%.1f ft".format(meters * M_TO_FT)
        UnitMode.NAUTICAL_MILE -> "%.3f nmi".format(meters * M_TO_NMI)
        UnitMode.YARD -> "%.1f yd".format(meters * M_TO_YD)
    }

    fun area(sqMeters: Double, unit: UnitMode): String = when (unit) {
        UnitMode.METER ->
            if (sqMeters < 10_000) "%.1f m²".format(sqMeters)
            else "%.2f ha".format(sqMeters / 10_000.0)
        UnitMode.KILOMETER -> "%.4f km²".format(sqMeters / 1_000_000.0)
        UnitMode.MILE -> "%.4f mi²".format(sqMeters * M2_TO_MI2)
        UnitMode.FEET -> "%.1f ft²".format(sqMeters * M2_TO_FT2)
        UnitMode.NAUTICAL_MILE -> "%.4f nmi²".format(sqMeters * M2_TO_NMI2)
        UnitMode.YARD -> "%.1f yd²".format(sqMeters * M2_TO_YD2)
    }

    fun labelOf(unit: UnitMode): String = unit.label
}
