package id.mport.maps.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.mport.maps.domain.unit.UnitMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("mport_maps_settings")

data class AppSettings(
    val mapType: Int = 1,          // 1=Normal 2=Satellite 3=Terrain 4=Hybrid
    val unit: UnitMode = UnitMode.METRIC,
    val darkTheme: Boolean = true
)

class SettingsStore(private val context: Context) {
    private val KEY_MAP = intPreferencesKey("mapType")
    private val KEY_UNIT = stringPreferencesKey("unit")
    private val KEY_THEME = stringPreferencesKey("theme")

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            mapType = prefs[KEY_MAP] ?: 1,
            unit = runCatching { UnitMode.valueOf(prefs[KEY_UNIT] ?: "METRIC") }
                .getOrDefault(UnitMode.METRIC),
            darkTheme = (prefs[KEY_THEME] ?: "DARK") == "DARK"
        )
    }

    suspend fun setMapType(v: Int) {
        context.dataStore.edit { it[KEY_MAP] = v }
    }

    suspend fun setUnit(v: UnitMode) {
        context.dataStore.edit { it[KEY_UNIT] = v.name }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[KEY_THEME] = if (dark) "DARK" else "LIGHT" }
    }
}
