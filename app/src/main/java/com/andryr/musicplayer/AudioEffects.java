package com.andryr.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;

/**
 * Created by Andry on 25/10/15.
 */
public class AudioEffects {

    public static final short BASSBOOST_MAX_STRENGTH = 1000;
    public static final String PREF_EQ_ENABLED = "enabled";
    public static final String PREF_BAND_LEVEL = "level";
    public static final String PREF_PRESET = "preset";
    public static final String PREF_BASSBOOST = "bassboost";
    public static final String AUDIO_EFFECTS_PREFS = "audioeffects";

    private static BassBoost sBassBoost;
    private static Equalizer sEqualizer;
    private static boolean sCustomPreset;


    public static void openAudioEffectSession(Context context, int audioSessionId)
    {
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_EFFECTS_PREFS, Context.MODE_PRIVATE);
        initBassBoost(prefs, audioSessionId);
        initEqualizer(prefs, audioSessionId);
    }

    public static void closeAudioEffectSession()
    {
        if(sBassBoost != null)
        {
            sBassBoost.release();
            sBassBoost = null;
        }

        if(sEqualizer != null)
        {
            sEqualizer.release();
            sEqualizer = null;
        }
    }



    private static void initBassBoost(SharedPreferences prefs, int audioSessionId) {
        if(sBassBoost != null)
        {
            sBassBoost.release();
            sBassBoost = null;
        }
        sBassBoost = new BassBoost(0, audioSessionId);
        sBassBoost.setEnabled(prefs.getBoolean(PREF_EQ_ENABLED, false));

        short strength = (short) prefs.getInt(PREF_BASSBOOST, 0);

        if (strength >= 0 && strength <= BASSBOOST_MAX_STRENGTH) {
            sBassBoost.setStrength(strength);
        }

    }

    private static void initEqualizer(SharedPreferences prefs, int audioSessionId) {

        if (sEqualizer != null) {
            sEqualizer.release();
            sEqualizer = null;
        }
        sEqualizer = new Equalizer(0, audioSessionId);
        sEqualizer.setEnabled(prefs.getBoolean(PREF_EQ_ENABLED, false));

        short preset = (short) prefs.getInt(PREF_PRESET, -1);

        if (preset == -1) {
            sCustomPreset = true;
        } else {
            usePreset(preset);

        }

        if (sCustomPreset) {
            short bands = sEqualizer.getNumberOfBands();

            for (short b = 0; b < bands; b++) {
                short level = sEqualizer.getBandLevel(b);

                sEqualizer.setBandLevel(b,
                        (short) prefs.getInt(PREF_BAND_LEVEL + b, level));
            }
        }

    }

    public static short getBassBoostStrength() {
        if(sBassBoost == null)
        {
            return 0;
        }
        return sBassBoost.getRoundedStrength();
    }

    public static void setBassBoostStrength(short strength) {
        if(sBassBoost == null)
        {
            return;
        }
        sBassBoost.setStrength(strength);
    }

    public static short[] getBandLevelRange() {
        if(sEqualizer == null)
        {
            return null;
        }
        return sEqualizer.getBandLevelRange();
    }

    public static short getBandLevel(short band) {
        if (sEqualizer == null) {
            return 0;
        }
        return sEqualizer.getBandLevel(band);
    }

    public static boolean areAudioEffectsEnabled() {
        if (sEqualizer == null) {
            return false;
        }
        return sEqualizer.getEnabled();
    }

    public static void setAudioEffectsEnabled(boolean enabled) {
        if (sEqualizer == null || sBassBoost == null) {
            return;
        }
        sBassBoost.setEnabled(true);
        sEqualizer.setEnabled(enabled);

    }

    public static void setBandLevel(short band, short level) {
        if (sEqualizer == null) {
            return;
        }
        sCustomPreset = true;
        sEqualizer.setBandLevel(band, level);

    }

    public static String[] getEqualizerPresets(Context context) {
        if (sEqualizer == null) {
            return null;
        }
        short numberOfPresets = sEqualizer.getNumberOfPresets();

        String[] presets = new String[numberOfPresets + 1];

        presets[0] = context.getResources().getString(R.string.custom);

        for (short n = 0; n < numberOfPresets; n++) {
            presets[n + 1] = sEqualizer.getPresetName(n);
        }

        return presets;
    }

    public static int getCurrentPreset() {
        if (sEqualizer == null || sCustomPreset) {
            return 0;
        }

        return sEqualizer.getCurrentPreset() + 1;
    }

    public static void usePreset(short preset) {
        if (sEqualizer == null) {
            return;
        }
        sCustomPreset = false;
        sEqualizer.usePreset(preset);

    }



    public static short getNumberOfBands() {
        if (sEqualizer == null) {
            return 0;
        }
        return sEqualizer.getNumberOfBands();
    }

    public static int getCenterFreq(short band) {
        if (sEqualizer == null) {
            return 0;
        }
        return sEqualizer.getCenterFreq(band);
    }

    public static void savePrefs(Context context) {
        if (sEqualizer == null || sBassBoost == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_EFFECTS_PREFS,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(PREF_BASSBOOST, sBassBoost.getRoundedStrength());

        short preset = sCustomPreset ? -1 : sEqualizer.getCurrentPreset();
        editor.putInt(PREF_PRESET, preset);


        short bands = sEqualizer.getNumberOfBands();

        for (short b = 0; b < bands; b++) {
            short level = sEqualizer.getBandLevel(b);

            editor.putInt(PREF_BAND_LEVEL + b, level);
        }
        editor.putBoolean(PREF_EQ_ENABLED,
                sEqualizer.getEnabled());

        editor.apply();
    }
}
