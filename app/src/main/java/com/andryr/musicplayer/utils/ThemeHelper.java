package com.andryr.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.activities.PreferencesActivity;

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

    public static int getStyleColor(Context context, int attr)
    {
        int[] attrs = {attr};

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);


        return ta.getColor(0, Color.BLACK);
    }

    public static void tintDrawable(Context context, Drawable drawable)
    {


        drawable.setColorFilter(getStyleColor(context, android.R.attr.textColorPrimary), PorterDuff.Mode.SRC_ATOP);

    }

    public static void tintImageView(Context context, ImageView imageView)
    {
        imageView.setColorFilter(getStyleColor(context, android.R.attr.textColorPrimary), PorterDuff.Mode.SRC_ATOP);
    }
}
