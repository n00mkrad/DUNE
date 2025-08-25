package org.jellyfin.androidtv.util

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import org.jellyfin.androidtv.preference.constant.AppLanguage
import java.util.Locale

object LocaleUtils {
    /**
     * Get the current locale from SharedPreferences
     */
    fun getCurrentLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences("org.jellyfin.androidtv.preferences", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", null)

        return when {
            !languageCode.isNullOrEmpty() && languageCode != "system" -> {
                when (languageCode) {
                    "zh-rCN" -> Locale("zh", "CN")
                    "zh-rTW" -> Locale("zh", "TW")
                    else -> Locale(languageCode)
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    LocaleList.getDefault().get(0)
                } else {
                    @Suppress("DEPRECATION")
                    Locale.getDefault()
                }
            }
        }
    }

    /**
     * Wrap the context with the current locale
     */
    fun wrapContext(context: Context): ContextWrapper {
        val locale = getCurrentLocale(context)
        val config = context.resources.configuration
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(config, locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            context.createConfigurationContext(config).let { ctx ->
                ContextWrapper(ctx)
            }
        } else {
            setSystemLocale(config, locale)
            context.createConfigurationContext(config).let { ctx ->
                ContextWrapper(ctx)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setSystemLocale(config: Configuration, locale: Locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocaleApi24(config, locale)
        } else {
            config.locale = locale
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun setSystemLocaleApi24(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }
}
