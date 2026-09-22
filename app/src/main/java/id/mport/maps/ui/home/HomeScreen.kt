package id.mport.maps.ui.home

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import id.mport.maps.BuildConfig
import id.mport.maps.R
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
    val context = LocalContext.current
    val ui by vm.ui.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val markers by vm.markers.collectAsStateWithLifecycle()

    var showSave by remember { mutableStateOf(false) }
    var showMapMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showMarkerDialog by remember { mutableStateOf<LatLng?>(null) }

    // Persist last camera so returning to Survey reuses view + nearby cached tiles
    var savedLat by rememberSaveable { mutableStateOf(-6.1754) }
    var savedLng by rememberSaveable { mutableStateOf(106.8272) }
    var savedZoom by rememberSaveable { mutableFloatStateOf(11f) }
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(savedLat, savedLng), savedZoom)
    }
    LaunchedEffect(camera.isMoving) {
        if (!camera.isMoving) {
            val pos = camera.position
            savedLat = pos.target.latitude
            savedLng = pos.target.longitude
            savedZoom = pos.zoom
        }
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
                isMyLocationEnabled = permissionGranted,
                isBuildingEnabled = false,
                isIndoorEnabled = false,
                isTrafficEnabled = false,
            ),
            uiSettings = MapUiSettings(
                compassEnabled = true,
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = false,
                indoorLevelPickerEnabled = false,
                scrollGesturesEnabledDuringRotateOrZoom = true
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
                        title = stringResource(R.string.point_n, i + 1)
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
                                contentDescription = stringResource(R.string.cd_map_type),
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
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.cd_undo))
                    }
                    IconButton(onClick = { vm.redo() }, enabled = ui.canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.cd_redo))
                    }
                    IconButton(
                        onClick = { showSave = true },
                        enabled = ui.points.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
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
                                text = { Text(stringResource(R.string.clear_all_points)) },
                                onClick = {
                                    vm.clearPoints()
                                    showMoreMenu = false
                                },
                                enabled = ui.points.isNotEmpty(),
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null) }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.unit_label, unit.label))
                                },
                                onClick = {
                                    // Ubah satuan lewat Setelan → Units
                                    showMoreMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Straighten, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.mode_distance_label)) },
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
                                text = { Text(stringResource(R.string.mode_area_label)) },
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
                        label = { Text(stringResource(R.string.mode_distance), fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                    FilterChip(
                        selected = ui.mode == MeasurementMode.AREA,
                        onClick = { vm.setMode(MeasurementMode.AREA) },
                        label = { Text(stringResource(R.string.mode_area), fontSize = 12.sp) },
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
                            stringResource(R.string.points_short, ui.points.size),
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
                        stringResource(R.string.map_not_configured),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Text(
                        stringResource(R.string.map_key_needed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )
                    Text(
                        stringResource(R.string.map_key_howto),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                    val keyLabel = if (BuildConfig.MAPS_API_KEY.isBlank()) {
                        stringResource(R.string.key_empty)
                    } else {
                        BuildConfig.MAPS_API_KEY.take(8) + "…"
                    }
                    Text(
                        text = stringResource(R.string.map_key_current, keyLabel),
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
                        stringResource(R.string.map_loading),
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
                contentDescription = stringResource(R.string.cd_gps),
                onClick = {
                    if (permissionGranted) vm.locate() else requestPermission()
                },
                loading = ui.gpsLoading,
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF424242)
            )
            ToolFab(
                icon = Icons.Default.Share,
                contentDescription = stringResource(R.string.cd_share),
                onClick = {
                    val pt = ui.points.lastOrNull()
                    if (pt == null) {
                        android.widget.Toast.makeText(
                            context,
                            context.getString(R.string.share_no_location),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val body = context.getString(
                            R.string.share_location_text,
                            pt.latitude,
                            pt.longitude
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, body)
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_location))
                        }
                        context.startActivity(
                            Intent.createChooser(intent, context.getString(R.string.share_location))
                        )
                    }
                },
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF1565C0)
            )
            ToolFab(
                icon = Icons.Default.PushPin,
                contentDescription = stringResource(R.string.cd_marker),
                onClick = { /* hint: long-press map */ },
                containerColor = Color(0xFFFFEB3B),
                contentColor = Color(0xFF212121)
            )
            ToolFab(
                icon = Icons.Default.Edit,
                contentDescription = stringResource(R.string.cd_measure_mode),
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
                contentDescription = stringResource(R.string.cd_clear_points),
                onClick = { vm.clearPoints() },
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color(0xFF424242)
            )
        }

        // Bottom hint (only when empty)

        // ── Undo / Redo above zoom controls (+) ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolFab(
                icon = Icons.AutoMirrored.Filled.Undo,
                contentDescription = stringResource(R.string.cd_undo),
                onClick = { if (ui.canUndo) vm.undo() },
                containerColor = if (ui.canUndo) Color(0xFFF5F5F5) else Color(0xFFE0E0E0),
                contentColor = if (ui.canUndo) Color(0xFF424242) else Color(0xFF9E9E9E)
            )
            ToolFab(
                icon = Icons.AutoMirrored.Filled.Redo,
                contentDescription = stringResource(R.string.cd_redo),
                onClick = { if (ui.canRedo) vm.redo() },
                containerColor = if (ui.canRedo) Color(0xFFF5F5F5) else Color(0xFFE0E0E0),
                contentColor = if (ui.canRedo) Color(0xFF424242) else Color(0xFF9E9E9E)
            )
        }


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
                    stringResource(R.string.map_hint),
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
                    TextButton(onClick = { vm.clearMessage() }) { Text(stringResource(R.string.ok)) }
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
        title = { Text(stringResource(R.string.save_survey_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, notes) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
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
        title = { Text(stringResource(R.string.add_marker_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("%.6f, %.6f".format(latLng.latitude, latLng.longitude))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title, notes) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
