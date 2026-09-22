package id.mport.maps.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import id.mport.maps.BuildConfig
import id.mport.maps.domain.measurement.MeasurementMode
import id.mport.maps.domain.unit.UnitFormatter
import id.mport.maps.domain.unit.UnitMode
import id.mport.maps.map.MapTypeHelper
import id.mport.maps.viewmodel.SurveyViewModel

/**
 * Map-first survey screen — layout inspired by classic map measurement apps:
 * full-bleed map, light top icon toolbar, right-side tool FABs, zoom controls on.
 * Bottom navigation is provided by parent SurveyApp (unchanged).
 */
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
    var showMoreMenu by remember { mutableStateOf(false) }
    var showMarkerDialog by remember { mutableStateOf<LatLng?>(null) }

    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-6.1754, 106.8272), 11f)
    }

    // Animate camera when GPS / restore sets focus
    LaunchedEffect(ui.focusLatLng) {
        val focus = ui.focusLatLng ?: return@LaunchedEffect
        val target = LatLng(focus.first, focus.second)
        camera.animate(CameraUpdateFactory.newLatLngZoom(target, maxOf(camera.position.zoom, 16f)))
        vm.consumeFocus()
    }

    val mapType = MapTypeHelper.fromId(settings.mapType)
    val unit = settings.unit
    var mapLoaded by remember { mutableStateOf(false) }
    val missingMapsKey = !BuildConfig.HAS_MAPS_KEY


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // ── Full-bleed map ──
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = permissionGranted
            ),
            uiSettings = MapUiSettings(
                compassEnabled = true,
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true
            ),
            onMapClick = { latLng ->
                vm.addLatLng(latLng.latitude, latLng.longitude)
            },
            onMapLongClick = { latLng ->
                showMarkerDialog = latLng
            },
            onMapLoaded = { mapLoaded = true }
        ) {
            ui.points.forEachIndexed { i, p ->
                key("pt-$i-${p.latitude}-${p.longitude}") {
                    Marker(
                        state = rememberMarkerState(position = LatLng(p.latitude, p.longitude)),
                        title = "Titik ${i + 1}"
                    )
                }
            }
            if (ui.points.size >= 2) {
                Polyline(
                    points = ui.points.map { LatLng(it.latitude, it.longitude) },
                    color = if (ui.mode == MeasurementMode.AREA) Color(0xFF00C853) else Color(0xFF2979FF),
                    width = 7f
                )
            }
            if (ui.mode == MeasurementMode.AREA && ui.points.size >= 3) {
                Polygon(
                    points = ui.points.map { LatLng(it.latitude, it.longitude) },
                    fillColor = Color(0x3300C853),
                    strokeColor = Color(0xFF00C853),
                    strokeWidth = 5f
                )
            }
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

        // ── Top toolbar (light bar like reference) ──
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            color = Color(0xF2FFFFFF),
            shadowElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Map type
                    Box {
                        IconButton(onClick = { showMapMenu = true }) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = "Tipe peta",
                                tint = Color(0xFF1565C0)
                            )
                        }
                        DropdownMenu(
                            expanded = showMapMenu,
                            onDismissRequest = { showMapMenu = false }
                        ) {
                            listOf(
                                1 to "Normal",
                                2 to "Satellite",
                                3 to "Terrain",
                                4 to "Hybrid"
                            ).forEach { (id, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        vm.setMapType(id)
                                        showMapMenu = false
                                    },
                                    leadingIcon = {
                                        if (settings.mapType == id) {
                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = { vm.undo() }, enabled = ui.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { vm.redo() }, enabled = ui.canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(
                        onClick = { showSave = true },
                        enabled = ui.points.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Simpan")
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Hapus semua titik") },
                                onClick = {
                                    vm.clearPoints()
                                    showMoreMenu = false
                                },
                                enabled = ui.points.isNotEmpty(),
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (unit == UnitMode.METRIC) "Satuan: Metric (m)"
                                        else "Satuan: Imperial (ft)"
                                    )
                                },
                                onClick = {
                                    vm.setUnit(
                                        if (unit == UnitMode.METRIC) UnitMode.IMPERIAL
                                        else UnitMode.METRIC
                                    )
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Straighten, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Mode: Jarak") },
                                onClick = {
                                    vm.setMode(MeasurementMode.DISTANCE)
                                    showMoreMenu = false
                                },
                                leadingIcon = {
                                    if (ui.mode == MeasurementMode.DISTANCE)
                                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mode: Area") },
                                onClick = {
                                    vm.setMode(MeasurementMode.AREA)
                                    showMoreMenu = false
                                },
                                leadingIcon = {
                                    if (ui.mode == MeasurementMode.AREA)
                                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }

                // Compact measurement strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = ui.mode == MeasurementMode.DISTANCE,
                        onClick = { vm.setMode(MeasurementMode.DISTANCE) },
                        label = { Text("Jarak", fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                    FilterChip(
                        selected = ui.mode == MeasurementMode.AREA,
                        onClick = { vm.setMode(MeasurementMode.AREA) },
                        label = { Text("Area", fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (ui.mode == MeasurementMode.AREA)
                                UnitFormatter.area(ui.areaSquareMeters, unit)
                            else
                                UnitFormatter.distance(ui.distanceMeters, unit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (ui.mode == MeasurementMode.AREA)
                                Color(0xFF00C853) else Color(0xFF2979FF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${ui.points.size} titik",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }


        // ── Maps key / load status overlay ──
        if (missingMapsKey) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(0.92f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF2FFFFFF),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Peta belum dikonfigurasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Text(
                        "Google Maps memerlukan API key. Tanpa key, layar akan berwarna beige/kosong.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )
                    Text(
                        "Cara cepat:\n" +
                        "1. Buka Google Cloud Console → Maps SDK for Android\n" +
                        "2. Buat API key, batasi ke package id.mport.maps\n" +
                        "3. Isi di gradle.properties:\n" +
                        "   MAPS_API_KEY=AIza...\n" +
                        "4. Rebuild aplikasi (Sync + Run)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                    Text(
                        "Key saat ini: ${if (BuildConfig.MAPS_API_KEY.isBlank()) \"(kosong)\" else BuildConfig.MAPS_API_KEY.take(8) + \"…\"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        } else if (!mapLoaded) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xE6FFFFFF)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Text(
                        "Memuat peta…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )
                }
            }
        }


        // ── Right tool column (like reference) ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolFab(
                icon = Icons.Default.MyLocation,
                contentDescription = "Lokasi GPS",
                onClick = {
                    if (permissionGranted) vm.locate() else requestPermission()
                },
                loading = ui.gpsLoading,
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF424242)
            )
            ToolFab(
                icon = Icons.Default.PushPin,
                contentDescription = "Tambah marker (long-press peta)",
                onClick = { /* hint: long-press map */ },
                containerColor = Color(0xFFFFEB3B),
                contentColor = Color(0xFF212121)
            )
            ToolFab(
                icon = Icons.Default.Edit,
                contentDescription = "Mode ukur",
                onClick = {
                    vm.setMode(
                        if (ui.mode == MeasurementMode.DISTANCE) MeasurementMode.AREA
                        else MeasurementMode.DISTANCE
                    )
                },
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF424242)
            )
            ToolFab(
                icon = Icons.Default.Timeline,
                contentDescription = "Clear titik",
                onClick = { vm.clearPoints() },
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF424242)
            )
        }

        // Bottom hint (only when empty)
        if (ui.points.isEmpty() && ui.message == null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xE6FFFFFF),
                shadowElevation = 2.dp
            ) {
                Text(
                    "Ketuk peta = titik ukur  ·  Long-press = marker",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161)
                )
            }
        }

        ui.message?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 40.dp),
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
private fun ToolFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    loading: Boolean = false,
    containerColor: Color = Color(0xFFF5F5F5),
    contentColor: Color = Color(0xFF424242)
) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
        }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title, notes) }) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
