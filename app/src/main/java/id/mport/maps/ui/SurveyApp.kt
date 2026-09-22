package id.mport.maps.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.mport.maps.R
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
    val activity = context as? ComponentActivity
    val settings by vm.settings.collectAsStateWithLifecycle()

    var settingsOnSubPage by remember { mutableStateOf(false) }
    var settingsGoBack by remember { mutableStateOf<(() -> Unit)?>(null) }

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

    var lastBackMs by remember { mutableLongStateOf(0L) }
    val exitHint = stringResource(R.string.press_back_again_exit)

    BackHandler(enabled = true) {
        when {
            tab == 4 && settingsOnSubPage -> settingsGoBack?.invoke()
            tab != 0 -> tab = 0
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackMs < 2000L) {
                    activity?.finish()
                } else {
                    lastBackMs = now
                    Toast.makeText(context, exitHint, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    MPorTSurveyTheme(darkTheme = settings.darkTheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Map, contentDescription = stringResource(R.string.nav_survey)) },
                        label = { Text(stringResource(R.string.nav_survey)) }
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.nav_history)) },
                        label = { Text(stringResource(R.string.nav_history)) }
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Place, contentDescription = stringResource(R.string.nav_marker)) },
                        label = { Text(stringResource(R.string.nav_marker)) }
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        icon = { Icon(Icons.Default.StickyNote2, contentDescription = stringResource(R.string.nav_notes)) },
                        label = { Text(stringResource(R.string.nav_notes)) }
                    )
                    NavigationBarItem(
                        selected = tab == 4,
                        onClick = { tab = 4 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings)) }
                    )
                }
            }
        ) { padding ->
            // Keep map composition alive under other tabs so Maps SDK tile cache / GL
            // context are reused (better offline tile reuse when returning to Survey).
            Box(Modifier.fillMaxSize()) {
                HomeScreen(
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

                if (tab != 0) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (tab) {
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
                                onOpenHistory = { tab = 1 },
                                onSubPageChanged = { onSub, goBack ->
                                    settingsOnSubPage = onSub
                                    settingsGoBack = goBack
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
