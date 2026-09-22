package id.mport.maps.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.mport.maps.domain.unit.UnitMode
import id.mport.maps.map.MapTypeHelper
import id.mport.maps.viewmodel.SurveyViewModel

@Composable
fun SettingsScreen(padding: PaddingValues, vm: SurveyViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var mapMenu by remember { mutableStateOf(false) }
    var unitMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pengaturan", style = MaterialTheme.typography.headlineSmall)
        Text("Pengaturan disimpan lokal di perangkat (DataStore).")

        Text("Mode Peta", style = MaterialTheme.typography.titleMedium)
        Box {
            OutlinedButton(onClick = { mapMenu = true }) {
                Text(MapTypeHelper.label(settings.mapType))
            }
            DropdownMenu(expanded = mapMenu, onDismissRequest = { mapMenu = false }) {
                listOf(1 to "Normal", 2 to "Satellite", 3 to "Terrain", 4 to "Hybrid")
                    .forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                vm.setMapType(id)
                                mapMenu = false
                            }
                        )
                    }
            }
        }

        Text("Satuan Pengukuran", style = MaterialTheme.typography.titleMedium)
        Box {
            OutlinedButton(onClick = { unitMenu = true }) {
                Text(
                    if (settings.unit == UnitMode.METRIC)
                        "Metric (m / km / m² / ha)"
                    else
                        "Imperial (ft / mi / ft²)"
                )
            }
            DropdownMenu(expanded = unitMenu, onDismissRequest = { unitMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Metric — meter / kilometer / m² / hektare") },
                    onClick = {
                        vm.setUnit(UnitMode.METRIC)
                        unitMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Imperial — feet / mile / ft²") },
                    onClick = {
                        vm.setUnit(UnitMode.IMPERIAL)
                        unitMenu = false
                    }
                )
            }
        }

        Text("Tema", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = settings.darkTheme,
                onClick = { vm.setDarkTheme(true) },
                label = { Text("Neon Dark") }
            )
            FilterChip(
                selected = !settings.darkTheme,
                onClick = { vm.setDarkTheme(false) },
                label = { Text("Light") }
            )
        }

        HorizontalDivider()
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Google Maps", style = MaterialTheme.typography.titleSmall)
        Text(
            if (BuildConfig.HAS_MAPS_KEY)
                "API key terpasang (${BuildConfig.MAPS_API_KEY.take(8)}…)"
            else
                "API key belum diisi — peta akan kosong. Isi MAPS_API_KEY di gradle.properties lalu rebuild.",
            style = MaterialTheme.typography.bodySmall
        )

        Text("MPorT Maps v1.1.1", style = MaterialTheme.typography.bodySmall)
        Text("Offline survey • GPS • Map measurement • Export", style = MaterialTheme.typography.bodySmall)
    }
}
