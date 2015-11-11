package com.andryr.musicplayer;

import android.app.Application;

import com.andryr.musicplayer.audiofx.AudioEffects;

/**
 * Created by Andry on 11/11/15.
 */
public class PlayerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AudioEffects.init(this);
    }
}
