package org.oucho.musicplayer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.Html;
import android.view.Menu;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.preferences.ThemeDialogFragment;
import org.oucho.musicplayer.preferences.ThemePreference;
import org.oucho.musicplayer.utils.ThemeHelper;

public class PreferencesActivity extends BaseActivity {

    public static final String KEY_PREF_THEME = "pref_theme";

    private PreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        String couleur = BaseActivity.getColor(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#" + couleur + "'>Configuration</font>"));
        actionBar.setElevation(0);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
        upArrow.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.ImageControlColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

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

/*            Preference cachePref = findPreference("pref_cache");

            cachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ArtistImageCache.getInstance().clear();
                    ArtworkCache.getInstance().clear();
                    Toast.makeText(getActivity(), R.string.cache_cleared_message, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });*/
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_PREF_THEME)) {
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
                fragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
            } else super.onDisplayPreferenceDialog(preference);
        }
    }
}
