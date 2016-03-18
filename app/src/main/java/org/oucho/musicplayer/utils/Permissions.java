package org.oucho.musicplayer.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;


public class Permissions {
    public static boolean checkPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }
}
