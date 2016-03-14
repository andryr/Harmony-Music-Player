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
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.andryr.musicplayer.R;
import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

/**
 * Created by Andry on 07/12/15.
 */
public class DialogUtils {

    public static void showErrorDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }

    public static void showPermissionDialog(Context context, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.permission)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }


    /**
     * Helper method to be called when the user wants to set a timer but one is already set
     *
     * @param context
     * @param listener
     */
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

        if (ThemeHelper.isDarkThemeSelected(activity)) {
            builder.setStyleResId(R.style.BetterPickersDialogFragment);
        } else {
            builder.setStyleResId(R.style.BetterPickersDialogFragment_Light);
        }
        builder.show();

    }
}
