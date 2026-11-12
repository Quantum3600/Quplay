package com.music.quplayui.data.preferences

/*
 * The app's look: Immersive theme.
 */

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The looks of the app. */
enum class Look {
    IMMERSIVE,
    @Deprecated("Classic look removed")
    CLASSIC;

    companion object {
        fun of(name: String?): Look = IMMERSIVE
    }
}

object LookPref {
    private const val PREF = "settings_prefs"
    private const val KEY = "look"
    private const val KEY_CHOSEN = "look_chosen"
    private const val KEY_OFFERED = "look_offered"

    /** The chosen look, as Compose state: everything reading it redraws when it changes. */
    var look by mutableStateOf(Look.IMMERSIVE)
        private set

    fun set(c: Context, value: Look) {
        look = Look.IMMERSIVE
        prefs(c).edit().putString(KEY, Look.IMMERSIVE.name).apply()
    }

    /** The look step of the onboarding was seen (new installs and updates alike). */
    fun isChosen(c: Context): Boolean = true
    fun setChosen(c: Context) = prefs(c).edit().putBoolean(KEY_CHOSEN, true).putBoolean(KEY_OFFERED, true).apply()

    /** The new-look popup was shown to an existing user (or the onboarding look step covered it). */
    fun isOffered(c: Context): Boolean = true
    fun setOffered(c: Context) = prefs(c).edit().putBoolean(KEY_OFFERED, true).apply()
    fun clearOffered(c: Context) = prefs(c).edit().remove(KEY_OFFERED).apply()

    private fun prefs(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
}
