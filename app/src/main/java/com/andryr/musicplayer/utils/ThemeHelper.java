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

package com.andryr.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.activities.PreferencesActivity;

/**
 * Created by andry on 13/09/15.
 */
public class ThemeHelper {

    public static boolean isDarkThemeSelected(Context context) {

        String lightStr = context.getResources().getString(R.string.light);
        String darkStr = context.getResources().getString(R.string.dark);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(R.string.pref_theme_base_key);
        return prefs.getString(prefKey, lightStr).equals(darkStr);
    }

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
        return context.getResources().getColor(id);
    }
}
