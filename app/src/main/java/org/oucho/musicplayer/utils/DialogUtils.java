package org.oucho.musicplayer.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;

import org.oucho.musicplayer.R;

public class DialogUtils {

    public static void showPermissionDialog(Context context, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.permission)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }

    public static void showSleepTimerDialog(Context context, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.sleep_timer_dialog_title)
                        //.setMessage(R.string.sleep_timer_dialog_message)
                .setPositiveButton(R.string.action_set_timer, listener)
                .setNeutralButton(R.string.back, listener)
                .setNegativeButton(R.string.action_cancel_current, listener)
                .show();
    }


    public static void showSleepHmsPicker(AppCompatActivity activity, HmsPickerDialogFragment.HmsPickerDialogHandler handler) {
        HmsPickerBuilder builder = new HmsPickerBuilder()
                .addHmsPickerDialogHandler(handler)
                .setFragmentManager(activity.getSupportFragmentManager());

            builder.setStyleResId(R.style.BetterPickersDialogFragment_Light);

        builder.show();

    }
}
