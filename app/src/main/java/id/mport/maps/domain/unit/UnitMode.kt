package id.mport.maps.domain.unit

/**
 * Supported distance display units.
 * Stored as enum name in DataStore / SurveyEntity.
 */
enum class UnitMode(val label: String, val shortLabel: String) {
    METER("Meter (m)", "m"),
    KILOMETER("Kilometer (km)", "km"),
    MILE("Mile (mi)", "mi"),
    FEET("Feet (f)", "ft"),
    NAUTICAL_MILE("Nautical Mile (nmi)", "nmi"),
    YARD("Yard (yd)", "yd");

    companion object {
        fun fromStored(name: String?): UnitMode {
            if (name.isNullOrBlank()) return METER
            // Backward compatibility with older METRIC / IMPERIAL
            return when (name.uppercase()) {
                "METRIC" -> METER
                "IMPERIAL" -> FEET
                else -> runCatching { valueOf(name.uppercase()) }.getOrDefault(METER)
            }
        }
    }
}
