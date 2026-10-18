package id.mport.maps

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.mport.maps.data.AppLanguage
import id.mport.maps.data.SettingsStore
import id.mport.maps.ui.SurveyApp
import id.mport.maps.viewmodel.SurveyViewModel

class MainActivity : ComponentActivity() {

    private var currentLang: AppLanguage = AppLanguage.SYSTEM

    override fun attachBaseContext(newBase: Context) {
        // SharedPreferences cache — no runBlocking / no DataStore I/O on main thread
        val lang = SettingsStore.readCachedLanguage(newBase)
        currentLang = lang
        val localized = LocaleHelper.apply(newBase, lang)
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MPorTSurvey)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SurveyViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()

            // Keep locale cache in sync + recreate when language changes
            LaunchedEffect(settings.language) {
                SettingsStore.cacheLanguage(this@MainActivity, settings.language)
                if (settings.language != currentLang) {
                    currentLang = settings.language
                    recreate()
                }
            }

            SurveyApp(vm = vm)
        }
    }
}
