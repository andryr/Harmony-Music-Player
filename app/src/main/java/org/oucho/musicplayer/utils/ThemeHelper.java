package org.oucho.musicplayer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;


public class ThemeHelper {

/*    public static boolean isDarkThemeSelected(Context context) {

        String lightStr = context.getResources().getString(R.string.light);
        String darkStr = context.getResources().getString(R.string.dark);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(PreferencesActivity.KEY_PREF_THEME_BASE, lightStr).equals(darkStr);
    }*/

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


    public static int getResourcesColor(Context context, int id) {
        //return context.getResources().getColor(id);

        return ContextCompat.getColor(context, id);

    }
}
