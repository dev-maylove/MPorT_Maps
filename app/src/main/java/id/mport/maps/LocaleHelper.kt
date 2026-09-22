package id.mport.maps

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import id.mport.maps.data.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun apply(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.INDONESIAN -> Locale("in")
            AppLanguage.SYSTEM -> {
                // Use device default — return context as-is
                return context
            }
        }
        return updateResources(context, locale)
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    fun recreateIfNeeded(activity: Activity, language: AppLanguage) {
        // Force activity recreate so all stringResources refresh
        activity.recreate()
    }
}
