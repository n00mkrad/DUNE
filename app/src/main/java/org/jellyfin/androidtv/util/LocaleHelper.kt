package org.jellyfin.androidtv.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.AppLanguage
import java.util.Locale

/**
 * Helper class to manage app locale changes
 */
object LocaleHelper {
    /**
     * Apply the selected language to the app context
     * @param context The context to update
     * @param userPreferences The user preferences to get the language from
     * @return The context with the updated configuration
     */
    fun onAttach(context: Context, userPreferences: UserPreferences): Context {
        val language = userPreferences[UserPreferences.appLanguage]
        return setAppLocale(context, language)
    }

    /**
     * Set the app locale to the specified language
     * @param context The context to update
     * @param language The language to set
     * @return The context with the updated configuration
     */
    fun setAppLocale(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.SYSTEM_DEFAULT -> getSystemLocale()
            else -> language.code.let { code ->
                // Handle special cases for Chinese
                when (code) {
                    "zh-rCN" -> Locale("zh", "CN")
                    "zh-rTW" -> Locale("zh", "TW")
                    else -> Locale(code)
                }
            }
        }

        return updateResources(context, locale)
    }

    /**
     * Get the system's default locale
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault().get(0)
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault()
        }
    }

    /**
     * Update the context's configuration with the new locale
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setLocale(locale)
                setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                setLocale(locale)
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
