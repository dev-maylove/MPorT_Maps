package id.mport.maps.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.mport.maps.BuildConfig
import id.mport.maps.domain.unit.UnitMode
import id.mport.maps.map.MapTypeHelper
import id.mport.maps.viewmodel.SurveyViewModel

private enum class SettingsPage { MAIN, ABOUT, PRIVACY }

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    vm: SurveyViewModel,
    onOpenHistory: (() -> Unit)? = null
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var page by remember { mutableStateOf(SettingsPage.MAIN) }
    var showMapTypeDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    when (page) {
        SettingsPage.ABOUT -> AboutPage(
            padding = padding,
            onBack = { page = SettingsPage.MAIN }
        )
        SettingsPage.PRIVACY -> PrivacyPage(
            padding = padding,
            onBack = { page = SettingsPage.MAIN }
        )
        SettingsPage.MAIN -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                SettingsItem(
                    icon = Icons.Default.Bookmark,
                    title = "Saved List",
                    onClick = { onOpenHistory?.invoke() }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Map,
                    title = "Map Type",
                    subtitle = MapTypeHelper.label(settings.mapType),
                    onClick = { showMapTypeDialog = true }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Straighten,
                    title = "Units",
                    subtitle = settings.unit.label,
                    onClick = { showUnitDialog = true }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.MyLocation,
                    title = "GPS Settings",
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    onClick = { page = SettingsPage.ABOUT }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    onClick = { page = SettingsPage.PRIVACY }
                )
                HorizontalDivider()

                // Theme toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(
                        checked = settings.darkTheme,
                        onCheckedChange = { vm.setDarkTheme(it) }
                    )
                }
                HorizontalDivider()

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "MPorT Maps v1.2.0",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = if (BuildConfig.HAS_MAPS_KEY)
                        "Google Maps API key terpasang"
                    else
                        "API key belum diisi — isi MAPS_API_KEY di gradle.properties",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showMapTypeDialog) {
        MapTypeDialog(
            current = settings.mapType,
            onSelect = {
                vm.setMapType(it)
                showMapTypeDialog = false
            },
            onDismiss = { showMapTypeDialog = false }
        )
    }

    if (showUnitDialog) {
        UnitSelectDialog(
            current = settings.unit,
            onSelect = {
                vm.setUnit(it)
                showUnitDialog = false
            },
            onDismiss = { showUnitDialog = false }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MapTypeDialog(
    current: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        1 to "Normal",
        2 to "Satellite",
        3 to "Terrain",
        4 to "Hybrid"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Please Select") },
        text = {
            Column {
                options.forEach { (id, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(id) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == id,
                            onClick = { onSelect(id) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
private fun UnitSelectDialog(
    current: UnitMode,
    onSelect: (UnitMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Please Select") },
        text = {
            Column {
                UnitMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == mode,
                            onClick = { onSelect(mode) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(mode.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

@Composable
private fun AboutPage(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("MPorT Maps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Version 1.2.0")
            Text(
                "Aplikasi survei dan pengukuran jarak/luas berbasis peta offline-capable. " +
                    "Gunakan GPS, marker, catatan, dan ekspor hasil survei ke CSV / JSON / KML."
            )
            HorizontalDivider()
            Text("Fitur utama:", fontWeight = FontWeight.SemiBold)
            Text("• Ukur jarak (polyline) & luas (polygon)")
            Text("• Undo / Redo & clear points")
            Text("• GPS → tambah titik otomatis")
            Text("• Long-press peta → Marker")
            Text("• Tipe peta: Normal / Satellite / Terrain / Hybrid")
            Text("• Satuan: m, km, mi, ft, nmi, yd")
            Text("• Riwayat survei + buka kembali")
            Text("• Export CSV / JSON / KML")
            Text("• Marker & Catatan lokal")
            Text("• Tema Dark / Light")
            HorizontalDivider()
            Text("Developer: MPorT")
            Text("Offline survey • GPS • Map measurement • Export")
            Text(
                "Data disimpan lokal di perangkat Anda (Room + DataStore). " +
                    "Tidak ada akun atau sinkronisasi cloud."
            )
        }
    }
}

@Composable
private fun PrivacyPage(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Privacy Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Privacy Policy — MPorT Maps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Last updated: September 2026")
            Text(
                "MPorT Maps menghormati privasi Anda. Aplikasi ini dirancang untuk bekerja " +
                    "secara lokal di perangkat tanpa mengirim data pribadi ke server kami."
            )
            Text("1. Data yang dikumpulkan", fontWeight = FontWeight.SemiBold)
            Text(
                "• Lokasi GPS: digunakan hanya saat Anda meminta titik GPS atau mengaktifkan " +
                    "\"My Location\" di peta. Lokasi tidak dikirim ke server pihak ketiga oleh aplikasi ini " +
                    "(kecuali melalui layanan Google Maps SDK yang tunduk pada kebijakan Google)."
            )
            Text(
                "• Survei, marker, dan catatan: disimpan di database lokal (Room) di perangkat Anda. " +
                    "Tidak ada backup otomatis ke cloud."
            )
            Text(
                "• Pengaturan (satuan, tipe peta, tema): disimpan di DataStore lokal."
            )
            Text("2. Izin yang digunakan", fontWeight = FontWeight.SemiBold)
            Text(
                "• ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION — untuk menampilkan posisi " +
                    "dan menambah titik dari GPS.\n" +
                    "• INTERNET — untuk memuat tile peta Google Maps (jika API key tersedia)."
            )
            Text("3. Berbagi data", fontWeight = FontWeight.SemiBold)
            Text(
                "Kami tidak menjual, menyewakan, atau membagikan data pribadi Anda. " +
                    "Export file (CSV/JSON/KML) hanya dibuat atas permintaan Anda dan disimpan " +
                    "di penyimpanan yang Anda pilih."
            )
            Text("4. Google Maps", fontWeight = FontWeight.SemiBold)
            Text(
                "Peta disediakan oleh Google Maps SDK. Penggunaan layanan Google tunduk pada " +
                    "Google Privacy Policy dan Terms of Service."
            )
            Text("5. Anak-anak", fontWeight = FontWeight.SemiBold)
            Text(
                "Aplikasi ini tidak ditujukan untuk anak di bawah 13 tahun dan tidak " +
                    "sengaja mengumpulkan data dari anak-anak."
            )
            Text("6. Perubahan kebijakan", fontWeight = FontWeight.SemiBold)
            Text(
                "Kami dapat memperbarui kebijakan ini. Versi terbaru akan ditampilkan di halaman ini."
            )
            Text("7. Kontak", fontWeight = FontWeight.SemiBold)
            Text("Jika ada pertanyaan mengenai privasi, hubungi pengembang MPorT Maps.")
        }
    }
}
