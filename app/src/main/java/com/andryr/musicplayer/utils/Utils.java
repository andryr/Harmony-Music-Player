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
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.Locale;

/**
 * Created by andry on 20/03/16.
 */
public class Utils {

    public static String msToText(long msec) {
        return String.format(Locale.getDefault(), "%d:%02d", msec / 60000,
                (msec % 60000) / 1000);
    }

    public static float getValueInRange(float val, float min, float max) {
        return Math.min(max, Math.max(min, val));
    }

    public static int getValueInRange(int val, int min, int max) {
        return Math.min(max, Math.max(min, val));
    }

    public static boolean checkPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
