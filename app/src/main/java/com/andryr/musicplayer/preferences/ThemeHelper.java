package com.andryr.musicplayer.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by andry on 13/09/15.
 */
public class ThemeHelper {

    public static boolean isDarkThemeSelected(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return  prefs.getString(PreferencesActivity.KEY_PREF_THEME_BASE, "Light").equals("Dark");
    }
}
