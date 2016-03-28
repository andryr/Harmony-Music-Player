/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        String prefKey = getString(R.string.pref_theme_key);
        int theme = prefs.getInt(prefKey, ThemePreference.DEFAULT_THEME);

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
