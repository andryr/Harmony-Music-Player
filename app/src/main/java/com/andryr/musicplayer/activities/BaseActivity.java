package com.andryr.musicplayer.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.preferences.ThemePreference;
import com.andryr.musicplayer.utils.ThemeHelper;

/**
 * Created by Andry on 25/09/15.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    protected void setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean dark = ThemeHelper.isDarkThemeSelected(this);
        int theme = prefs.getInt(PreferencesActivity.KEY_PREF_THEME, ThemePreference.DEFAULT_THEME);

        switch (theme) {
            case ThemePreference.DARK_BLUE_GREY_THEME:
                if (dark) {
                    setTheme(R.style.AppThemeDarkBlueGreyDark);
                } else {
                    setTheme(R.style.AppThemeDarkBlueGreyLight);
                }
                break;
            case ThemePreference.BLUE_GREY_THEME:
                if (dark) {
                    setTheme(R.style.AppThemeBlueGreyDark);
                } else {
                    setTheme(R.style.AppThemeBlueGreyLight);
                }
                break;
            case ThemePreference.BLUE_THEME:
                if (dark) {
                    setTheme(R.style.AppThemeBlueDark);
                } else {
                    setTheme(R.style.AppThemeBlueLight);
                }
                break;
        }
    }
}
