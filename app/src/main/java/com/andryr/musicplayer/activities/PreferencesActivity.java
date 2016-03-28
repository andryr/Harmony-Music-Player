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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.widget.Toast;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.preferences.ThemeDialogFragment;
import com.andryr.musicplayer.preferences.ThemePreference;
import com.andryr.musicplayer.images.ArtistImageCache;

public class PreferencesActivity extends BaseActivity {



    private PreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        if(savedInstanceState == null) {
            mFragment = new PreferenceFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (mFragment == null || !mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private boolean mShouldRestart = false;


        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences);

            String prefKey = getString(R.string.pref_cache_key);
            Preference cachePref = findPreference(prefKey);

            cachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ArtistImageCache.getInstance().clear();
                    ArtworkCache.getInstance().clear();
                    Toast.makeText(getActivity(), R.string.cache_cleared_message, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref_theme_key)) || key.equals(getString(R.string.pref_theme_base_key))) {
                mShouldRestart = true;
            }
        }

        public boolean onBackPressed() {
            Context c = getActivity().getBaseContext();
            if (mShouldRestart) {
                Intent i = c.getPackageManager()
                        .getLaunchIntentForPackage(c.getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
            return false;
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment fragment;
            if (preference instanceof ThemePreference) {
                fragment = new ThemeDialogFragment();
                fragment.setTargetFragment(this, 0);
                Bundle args = new Bundle(1);
                args.putString("key", preference.getKey());
                fragment.setArguments(args);
                fragment.show(getFragmentManager(),
                        "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else super.onDisplayPreferenceDialog(preference);
        }
    }


}
