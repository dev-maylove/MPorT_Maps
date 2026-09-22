package id.mport.maps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.mport.maps.ui.SurveyApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from splash theme to normal before composing content
        setTheme(R.style.Theme_MPorTSurvey)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SurveyApp()
        }
    }
}
