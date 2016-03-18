package org.oucho.musicplayer.loaders;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

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
