package id.mport.maps

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

/**
 * Early Google Maps SDK init + prefer latest renderer for better caching / performance.
 */
class MPorTApp : Application(), OnMapsSdkInitializedCallback {
    override fun onCreate() {
        super.onCreate()
        runCatching {
            MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)
        }.onFailure {
            Log.w("MPorTApp", "MapsInitializer failed: ${it.message}")
        }
    }

    override fun onMapsSdkInitialized(renderer: Renderer) {
        Log.i("MPorTApp", "Maps SDK renderer: $renderer")
    }
}
