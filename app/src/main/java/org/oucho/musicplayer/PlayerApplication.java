package org.oucho.musicplayer;

import android.app.Application;

import org.oucho.musicplayer.audiofx.AudioEffects;
import org.oucho.musicplayer.images.ArtistImageCache;
import org.oucho.musicplayer.images.ArtworkCache;


public class PlayerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ArtworkCache.init(this);
        ArtistImageCache.init(this);
        AudioEffects.init(this);
    }
}
