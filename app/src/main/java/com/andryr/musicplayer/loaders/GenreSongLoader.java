package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Andry on 01/02/16.
 */
public class GenreSongLoader extends SongLoader {
    private final long mGenreId;

    public GenreSongLoader(Context context, long genreId) {
        super(context);
        mGenreId = genreId;
    }

    @Override
    protected Uri getContentUri() {
        return MediaStore.Audio.Genres.Members.getContentUri(
                "external", mGenreId);
    }
}
