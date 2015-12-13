package com.andryr.musicplayer.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.andryr.musicplayer.R;

/**
 * Created by Andry on 07/12/15.
 */
public class DialogUtils {

    public static void showErrorDialog(Context context,String message) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }
}
