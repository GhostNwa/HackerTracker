package com.shortstack.hackertracker.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.shortstack.hackertracker.App
import com.shortstack.hackertracker.BuildConfig
import com.shortstack.hackertracker.R
import com.shortstack.hackertracker.analytics.AnalyticsController
import com.shortstack.hackertracker.database.DatabaseManager
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private val database: DatabaseManager by inject()
    private val analytics: AnalyticsController by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.DEBUG) {
            findPreference("user_uid").summary = database.user.uid
        }

        findPreference("change_theme").setOnPreferenceClickListener {
            // TODO: Show the change theme dialog.
            return@setOnPreferenceClickListener true
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        if (BuildConfig.DEBUG)
            addPreferencesFromResource(R.xml.dev_settings)

        addPreferencesFromResource(R.xml.settings)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        val event = when (key) {
            "user_analytics" -> AnalyticsController.SETTINGS_ANALYTICS
            "user_allow_push_notifications" -> AnalyticsController.SETTINGS_NOTIFICATIONS
            "user_show_expired_events" -> AnalyticsController.SETTINGS_EXPIRED_EVENTS
        // We're not tracking these events, ignore.
            else -> return
        }

        val value = sharedPreferences.getBoolean(key, false)
        analytics.onSettingsChanged(event, value)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}
