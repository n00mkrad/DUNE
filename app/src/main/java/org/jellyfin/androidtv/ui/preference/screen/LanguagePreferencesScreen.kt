package org.jellyfin.androidtv.ui.preference.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.AppLanguage
import org.jellyfin.androidtv.ui.preference.dsl.OptionsFragment
import org.jellyfin.androidtv.ui.preference.dsl.enum
import org.jellyfin.androidtv.ui.preference.dsl.optionsScreen
import timber.log.Timber
import org.koin.android.ext.android.inject

class LanguagePreferencesScreen : OptionsFragment() {
    private val userPreferences: UserPreferences by inject()

    private fun handleLanguageChange(newLanguage: AppLanguage) {
        try {
            // Save to SharedPreferences directly to ensure it's persisted
            val prefs = requireContext().getSharedPreferences("org.jellyfin.androidtv.preferences", Context.MODE_PRIVATE)
            prefs.edit().putString("app_language", newLanguage.code).apply()
            
            // Also save through userPreferences for consistency
            userPreferences[UserPreferences.appLanguage] = newLanguage
            
            // Force commit to ensure the preference is written to disk
            prefs.edit().commit()
            
            // Restart the app to apply the new language
            activity?.let { activity ->
                // Create an intent to restart the app
                val packageManager = activity.packageManager
                val intent = packageManager.getLaunchIntentForPackage(activity.packageName)
                val componentName = intent?.component
                
                // Create a fresh task with the launcher activity
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                
                // Add flags to clear the back stack and create a new task
                mainIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                )
                
                // Add a small delay to ensure the activity is properly finished
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        // Start the new activity
                        activity.startActivity(mainIntent)
                        
                        // Kill the current process to ensure a clean restart
                        android.os.Process.killProcess(android.os.Process.myPid())
                        System.exit(0)
                    } catch (e: Exception) {
                        // Only log errors
                    }
                }, 200)
            }
            
        } catch (e: Exception) {
            // Only log errors
        }
    }

    override val screen by optionsScreen {
        setTitle(R.string.pref_language)

        category {
            enum<AppLanguage> {
                setTitle(R.string.pref_language)
                
                bind {
                    get { userPreferences[UserPreferences.appLanguage] }
                    set { newLanguage ->
                        handleLanguageChange(newLanguage)
                    }
                    default { userPreferences.getDefaultValue(UserPreferences.appLanguage) }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LanguagePreferencesScreen()
    }
}
