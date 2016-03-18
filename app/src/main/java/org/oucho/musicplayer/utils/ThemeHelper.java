package org.oucho.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.musicplayer.activities.PreferencesActivity;
import org.oucho.musicplayer.preferences.ThemePreference;


public class ThemeHelper {


    public static int getStyleColor(Context context, int attr) {
        int[] attrs = {attr};

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);


        return ta.getColor(0, Color.BLACK);
    }

    public static void tintDrawable(Context context, Drawable drawable) {

        if (drawable != null) {
            drawable.mutate().setColorFilter(getStyleColor(context, android.R.attr.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void tintImageView(Context context, ImageView imageView) {
        if (imageView != null) {
            imageView.setColorFilter(getStyleColor(context, android.R.attr.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void tintCompoundDrawables(Context context, TextView textView) {
        for (Drawable d : textView.getCompoundDrawables()) {
            tintDrawable(context, d);
        }
    }

    public static String getColor(Context context) {

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
