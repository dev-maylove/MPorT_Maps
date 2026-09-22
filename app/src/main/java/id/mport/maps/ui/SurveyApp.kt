package id.mport.maps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.mport.maps.ui.history.HistoryScreen
import id.mport.maps.ui.home.HomeScreen
import id.mport.maps.ui.markers.MarkersScreen
import id.mport.maps.ui.notes.NotesScreen
import id.mport.maps.ui.settings.SettingsScreen
import id.mport.maps.ui.theme.MPorTSurveyTheme
import id.mport.maps.viewmodel.SurveyViewModel

@Composable
fun SurveyApp(vm: SurveyViewModel = viewModel()) {
    var tab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted =
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Apply theme from DataStore settings
    MPorTSurveyTheme(darkTheme = settings.darkTheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Survey") },
                        label = { Text("Survey") }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat") },
                        label = { Text("Riwayat") }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Place, contentDescription = "Marker") },
                        label = { Text("Marker") }
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        icon = { Icon(Icons.Default.StickyNote2, contentDescription = "Catatan") },
                        label = { Text("Catatan") }
                    )
                    NavigationBarItem(
                        selected = tab == 4,
                        onClick = { tab = 4 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = { Text("Setelan") }
                    )
                }
            }
        ) { padding ->
            when (tab) {
                0 -> HomeScreen(
                    padding = padding,
                    vm = vm,
                    permissionGranted = permissionGranted,
                    requestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
                1 -> HistoryScreen(
                    padding = padding,
                    vm = vm,
                    onOpenSurvey = { tab = 0 }
                )
                2 -> MarkersScreen(padding = padding, vm = vm)
                3 -> NotesScreen(padding = padding, vm = vm)
                4 -> SettingsScreen(
                    padding = padding,
                    vm = vm,
                    onOpenHistory = { tab = 1 }
                )
            }
        }
    }
}
