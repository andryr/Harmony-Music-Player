package org.oucho.musicplayer.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.preferences.ThemePreference;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);

    }

    private void setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int theme = prefs.getInt(PreferencesActivity.KEY_PREF_THEME, ThemePreference.DEFAULT_THEME);

        switch (theme) {
            case ThemePreference.original_green:
                    setTheme(R.style.AppThemeOGreenLight);
                break;
            case ThemePreference.red:
                    setTheme(R.style.AppThemeRedLight);
                break;
            case ThemePreference.orange:
                setTheme(R.style.AppThemeOrangeLight);
                break;
            case ThemePreference.purple:
                setTheme(R.style.AppThemePurpleLight)
                ;break;
            case ThemePreference.navy:
                setTheme(R.style.AppThemeNavyLight);
                break;
            case ThemePreference.blue:
                setTheme(R.style.AppThemeBlueLight);
                break;
            case ThemePreference.sky:
                setTheme(R.style.AppThemeSkyLight);
                break;
            case ThemePreference.seagreen:
                setTheme(R.style.AppThemeSeagreenLight);
                break;
            case ThemePreference.cyan:
                setTheme(R.style.AppThemeCyanLight);
                break;
            case ThemePreference.pink:
                setTheme(R.style.AppThemePinkLight);
                break;
        }
    }
}
