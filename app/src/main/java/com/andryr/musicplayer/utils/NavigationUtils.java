package com.andryr.musicplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.activities.EqualizerActivity;
import com.andryr.musicplayer.activities.PlaybackActivity;
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

    public static void showMainActivity(Activity activity, boolean animate) {
        Intent i = new Intent(activity, MainActivity.class);
        if (animate) {
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        activity.startActivity(i);
        if (animate) {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
        }
    }

    public static void showPlaybackActivity(Activity activity, boolean animate) {
        Intent i = new Intent(activity, PlaybackActivity.class);
        if (animate) {
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        activity.startActivity(i);
        if (animate) {
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
        }
    }
}
