package id.mport.maps.domain.unit

import id.mport.maps.R

/**
 * Supported distance display units.
 * Stored as enum name in DataStore / SurveyEntity.
 */
enum class UnitMode(val label: String, val shortLabel: String, val labelRes: Int) {
    METER("Meter (m)", "m", R.string.unit_meter),
    KILOMETER("Kilometer (km)", "km", R.string.unit_kilometer),
    MILE("Mile (mi)", "mi", R.string.unit_mile),
    FEET("Feet (ft)", "ft", R.string.unit_feet),
    NAUTICAL_MILE("Nautical Mile (nmi)", "nmi", R.string.unit_nautical_mile),
    YARD("Yard (yd)", "yd", R.string.unit_yard);

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
