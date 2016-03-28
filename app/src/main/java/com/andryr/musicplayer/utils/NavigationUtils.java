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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeImageTransform;
import android.transition.TransitionInflater;
import android.view.View;

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

    public static void showEqualizer(Activity activity) {
        Intent i = new Intent(activity, EqualizerActivity.class);
        activity.startActivity(i);
    }

    public static void showPreferencesActivity(Activity activity) {
        Intent i = new Intent(activity, PreferencesActivity.class);
        activity.startActivity(i);
    }

    public static void showMainActivity(Activity activity) {
        Intent i = new Intent(activity, MainActivity.class);
        ActivityCompat.startActivity(activity, i, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, activity.findViewById(R.id.artwork), "artwork").toBundle());
    }

    public static void showPlaybackActivity(Activity activity) {
        Intent i = new Intent(activity, PlaybackActivity.class);
        ActivityCompat.startActivity(activity, i, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, activity.findViewById(R.id.artwork_min), "artwork").toBundle());
    }


    @SuppressLint("NewApi")
    public static void showFragment(FragmentActivity activity, Fragment firstFragment, Fragment secondFragment, @Nullable Pair<View, String>... transitionViews) {

        boolean lollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

        if(lollipop) {
            firstFragment.setSharedElementReturnTransition(TransitionInflater.from(activity).inflateTransition(R.transition.change_image_transform));
            secondFragment.setSharedElementEnterTransition(TransitionInflater.from(activity).inflateTransition(R.transition.change_image_transform));
        }
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, secondFragment)
                .addToBackStack(null);
        if(lollipop && transitionViews != null) {

            for(Pair<View, String> tr : transitionViews) {
                ft.addSharedElement(tr.first, tr.second);
            }
        }
        ft.commit();
    }
}
