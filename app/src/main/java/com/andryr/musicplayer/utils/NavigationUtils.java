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
