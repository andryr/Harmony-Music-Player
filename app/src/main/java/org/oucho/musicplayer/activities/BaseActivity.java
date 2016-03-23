package org.oucho.musicplayer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.preferences.ThemePreference;

@SuppressLint("Registered")
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

    static String getColor(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int theme = prefs.getInt(PreferencesActivity.KEY_PREF_THEME, ThemePreference.DEFAULT_THEME);

        String Couleur = "";
        switch (theme) {
            case ThemePreference.original_green:
                Couleur = "14b68e";
                break;
            case ThemePreference.red:
                Couleur = "a50916";
                break;
            case ThemePreference.orange:
                Couleur = "fd7c08";
                break;
            case ThemePreference.purple:
                Couleur = "5b1588";
                break;
            case ThemePreference.navy:
                Couleur = "303aa6";
                break;
            case ThemePreference.blue:
                Couleur = "175fc9";
                break;
            case ThemePreference.sky:
                Couleur = "19729a";
                break;
            case ThemePreference.seagreen:
                Couleur = "239388";
                break;
            case ThemePreference.cyan:
                Couleur = "138d3a";
                break;
            case ThemePreference.pink:
                Couleur = "ff4381";
                break;
        }

        return Couleur;
    }

}
