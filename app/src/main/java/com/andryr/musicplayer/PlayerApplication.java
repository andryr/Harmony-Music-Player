package com.andryr.musicplayer;

import android.app.Application;

import com.andryr.musicplayer.audiofx.AudioEffects;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by Andry on 11/11/15.
 */
@ReportsCrashes(
        formUri = "https://andryrapps.cloudant.com/acra-musicplayer/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.PUT,
        formUriBasicAuthLogin = "tronstoreddlityptileater",
        formUriBasicAuthPassword = "4d50520783766605ad74b67e2af41550feceb4ff",
        //formKey = "", // This is required for backward compatibility but not used

        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)

public class PlayerApplication extends Application {
    @Override
    public void onCreate() {

        ACRA.init(this);
        com.andryr.musicplayer.acra.HttpSender sender = new com.andryr.musicplayer.acra.HttpSender();
        ACRA.getErrorReporter().setReportSender(sender);
        super.onCreate();

        AudioEffects.init(this);

    }
}
