package com.andryr.musicplayer.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.andryr.musicplayer.R;

/**
 * Created by andry on 13/09/15.
 */
public class ThemeHelper {

    public static boolean isDarkThemeSelected(Context context)
    {

        String lightStr = context.getResources().getString(R.string.light);
        String darkStr = context.getResources().getString(R.string.dark);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return  prefs.getString(PreferencesActivity.KEY_PREF_THEME_BASE, lightStr).equals(darkStr);
    }
}
