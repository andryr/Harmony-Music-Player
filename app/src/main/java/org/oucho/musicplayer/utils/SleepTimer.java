package org.oucho.musicplayer.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;

import org.oucho.musicplayer.PlaybackService;

public class SleepTimer {
    private static final String KEY_TIMER_SET = "org.oucho.musicplayer.KEY_TIMER_SET";
    private static final String KEY_TIMER_EXPIRATION = "org.oucho.musicplayer.KEY_TIMER_EXPIRATION";

    public static boolean isTimerSet(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_TIMER_SET, false) && SystemClock.elapsedRealtime() - prefs.getLong(KEY_TIMER_EXPIRATION, 0) < 0;
    }


    public static void setTimer(Context context, SharedPreferences prefs, int seconds) {

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(PlaybackService.ACTION_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long timerMillis = SystemClock.elapsedRealtime() + seconds * 1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, timerMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, timerMillis, pendingIntent);
        }

        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(KEY_TIMER_SET, true);
        prefsEditor.putLong(KEY_TIMER_EXPIRATION, timerMillis);
        prefsEditor.apply();

    }


    public static void cancelTimer(Context context, SharedPreferences prefs) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(PlaybackService.ACTION_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(KEY_TIMER_SET, false);
        prefsEditor.apply();
    }
}