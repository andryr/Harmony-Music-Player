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
import com.andryr.musicplayer.preferences.ThemeDialogFragment;
import com.andryr.musicplayer.preferences.ThemePreference;
import com.andryr.musicplayer.musicbrainz.ArtistImageUtils;

public class PreferencesActivity extends BaseActivity {

    public static final String KEY_PREF_THEME_BASE = "pref_theme_base";
    public static final String KEY_PREF_THEME = "pref_theme";

    private PreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        mFragment = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (!mFragment.onBackPressed()) {
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

            Preference cachePref = findPreference("pref_cache");

            cachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ArtistImageUtils.clearMemoryCache();
                    ArtistImageUtils.clearDbCache(getActivity());
                    Toast.makeText(getActivity(), R.string.cache_cleared_message, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_PREF_THEME) || key.equals(KEY_PREF_THEME_BASE)) {
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
                fragment.show(getChildFragmentManager(),
                        "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else super.onDisplayPreferenceDialog(preference);
        }
    }


}
