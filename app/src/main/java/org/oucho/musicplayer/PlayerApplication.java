package org.oucho.musicplayer;

import android.app.Application;

import org.oucho.musicplayer.audiofx.AudioEffects;
import org.oucho.musicplayer.images.ArtistImageCache;
import org.oucho.musicplayer.images.ArtworkCache;

import org.oucho.musicplayer.utils.PrefUtils;

public class PlayerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PrefUtils.init(this);
        ArtworkCache.init(this);
        ArtistImageCache.init(this);
        AudioEffects.init(this);
    }
}
