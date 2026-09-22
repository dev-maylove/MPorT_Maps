package id.mport.maps.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import id.mport.maps.domain.measurement.MeasurementMode
import id.mport.maps.domain.unit.UnitFormatter
import id.mport.maps.map.MapTypeHelper
import id.mport.maps.viewmodel.SurveyViewModel

@Composable
fun HomeScreen(
    padding: PaddingValues,
    vm: SurveyViewModel,
    permissionGranted: Boolean,
    requestPermission: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val markers by vm.markers.collectAsStateWithLifecycle()

    var showSave by remember { mutableStateOf(false) }
    var showMapMenu by remember { mutableStateOf(false) }
    var showMarkerDialog by remember { mutableStateOf<LatLng?>(null) }

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-6.7475, 111.0381), 13f)
    }

    val mapType = MapTypeHelper.fromId(settings.mapType)
    val unit = settings.unit

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = permissionGranted
            ),
            uiSettings = MapUiSettings(
                compassEnabled = true,
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            onMapClick = { latLng ->
                vm.addLatLng(latLng.latitude, latLng.longitude)
            },
            onMapLongClick = { latLng ->
                showMarkerDialog = latLng
            }
        ) {
            // Survey points — key by index+coords so Compose tracks them correctly
            ui.points.forEachIndexed { i, p ->
                key("pt-${i}-${p.latitude}-${p.longitude}") {
                    Marker(
                        state = rememberMarkerState(position = LatLng(p.latitude, p.longitude)),
                        title = "Titik ${i + 1}"
                    )
                }
            }
            if (ui.points.size >= 2) {
                Polyline(
                    points = ui.points.map { LatLng(it.latitude, it.longitude) },
                    color = if (ui.mode == MeasurementMode.AREA) Color(0xFF00E676) else Color(0xFF00E5FF),
                    width = 6f
                )
            }
            if (ui.mode == MeasurementMode.AREA && ui.points.size >= 3) {
                Polygon(
                    points = ui.points.map { LatLng(it.latitude, it.longitude) },
                    fillColor = Color(0x3300E676),
                    strokeColor = Color(0xFF00E676),
                    strokeWidth = 5f
                )
            }
            // Saved markers
            markers.forEach { m ->
                key("mk-${m.id}") {
                    Marker(
                        state = rememberMarkerState(position = LatLng(m.latitude, m.longitude)),
                        title = m.title,
                        snippet = m.notes
                    )
                }
            }
        }

        // Top measurement card
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("MPorT Maps", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = ui.mode == MeasurementMode.DISTANCE,
                        onClick = { vm.setMode(MeasurementMode.DISTANCE) },
                        label = { Text("Jarak") }
                    )
                    FilterChip(
                        selected = ui.mode == MeasurementMode.AREA,
                        onClick = { vm.setMode(MeasurementMode.AREA) },
                        label = { Text("Area") }
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (ui.mode == MeasurementMode.AREA) {
                    Text(
                        "Luas: ${UnitFormatter.area(ui.areaSquareMeters, unit)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("Keliling: ${UnitFormatter.distance(ui.perimeterMeters, unit)}")
                } else {
                    Text(
                        "Jarak: ${UnitFormatter.distance(ui.distanceMeters, unit)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text("${ui.points.size} titik • ketuk peta menambah • long-press marker")
            }
        }

        // Bottom action bar
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { vm.undo() },
                    enabled = ui.canUndo,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Undo, null, modifier = Modifier.size(18.dp))
                    Text("Undo")
                }
                OutlinedButton(
                    onClick = { vm.redo() },
                    enabled = ui.canRedo,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Redo, null, modifier = Modifier.size(18.dp))
                    Text("Redo")
                }
                OutlinedButton(
                    onClick = { vm.clearPoints() },
                    enabled = ui.points.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                    Text("Clear")
                }
                Button(
                    onClick = { showSave = true },
                    enabled = ui.points.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Text("Simpan")
                }
            }
        }

        // Right side FABs
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    if (permissionGranted) vm.locate() else requestPermission()
                }
            ) {
                if (ui.gpsLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = "GPS")
                }
            }
            Box {
                SmallFloatingActionButton(onClick = { showMapMenu = true }) {
                    Icon(Icons.Default.Layers, contentDescription = "Map type")
                }
                DropdownMenu(expanded = showMapMenu, onDismissRequest = { showMapMenu = false }) {
                    listOf(1 to "Normal", 2 to "Satellite", 3 to "Terrain", 4 to "Hybrid")
                        .forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    vm.setMapType(id)
                                    showMapMenu = false
                                }
                            )
                        }
                }
            }
            SmallFloatingActionButton(onClick = {
                val next = if (settings.unit.name == "METRIC")
                    id.mport.maps.domain.unit.UnitMode.IMPERIAL
                else id.mport.maps.domain.unit.UnitMode.METRIC
                vm.setUnit(next)
            }) {
                Text(if (settings.unit.name == "METRIC") "m" else "ft")
            }
        }

        // Message snackbar
        ui.message?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 140.dp, start = 16.dp, end = 16.dp),
                action = {
                    TextButton(onClick = { vm.clearMessage() }) { Text("OK") }
                }
            ) { Text(msg) }
        }
    }

    if (showSave) {
        SaveDialog(
            onDismiss = { showSave = false },
            onSave = { name, notes ->
                vm.saveSurvey(name, notes)
                showSave = false
            }
        )
    }

    showMarkerDialog?.let { latLng ->
        MarkerDialog(
            latLng = latLng,
            onDismiss = { showMarkerDialog = null },
            onSave = { title, notes ->
                vm.saveMarker(title, latLng.latitude, latLng.longitude, notes)
                showMarkerDialog = null
            }
        )
    }
}

@Composable
private fun SaveDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simpan Survey") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan") }, minLines = 3)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, notes) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun MarkerDialog(
    latLng: LatLng,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Marker") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("%.6f, %.6f".format(latLng.latitude, latLng.longitude))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan") })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title, notes) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
