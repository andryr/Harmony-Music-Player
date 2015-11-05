package com.andryr.musicplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.andryr.musicplayer.activities.EqualizerActivity;
import com.andryr.musicplayer.activities.PreferencesActivity;
import com.andryr.musicplayer.activities.SearchActivity;

/**
 * Created by Andry on 02/11/15.
 */
public class NavigationUtils {
   public static void showSearchActivity(Activity activity, int requestCode) {
        Intent i = new Intent(activity, SearchActivity.class);
        activity.startActivityForResult(i, requestCode);
    }

    public static void showEqualizer(Context context) {
        Intent i = new Intent(context, EqualizerActivity.class);
        context.startActivity(i);
    }

    public static void showPreferencesActivity(Context context) {
        Intent i = new Intent(context, PreferencesActivity.class);
        context.startActivity(i);
    }
}
