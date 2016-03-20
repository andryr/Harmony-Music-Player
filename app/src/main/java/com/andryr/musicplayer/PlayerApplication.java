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

package com.andryr.musicplayer;

import android.app.Application;

import com.andryr.musicplayer.audiofx.AudioEffects;
import com.andryr.musicplayer.images.ArtistImageCache;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.utils.PrefUtils;

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


        PrefUtils.init(this);
        ArtworkCache.init(this);
        ArtistImageCache.init(this);
        AudioEffects.init(this);

    }
}
