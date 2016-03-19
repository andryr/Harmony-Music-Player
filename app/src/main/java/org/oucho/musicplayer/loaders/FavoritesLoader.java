package org.oucho.musicplayer.loaders;

import android.content.Context;

import org.oucho.musicplayer.DataBase.FavoritesDatabase;
import org.oucho.musicplayer.model.Song;

import java.util.List;


public class FavoritesLoader extends BaseLoader<List<Song>> {

    private int mLimit = -1;
    public FavoritesLoader(Context context) {
        super(context);
    }

    public FavoritesLoader(Context context, int limit) {
        super(context);
        mLimit = 3;
    }

    @Override
    public List<Song> loadInBackground() {
        FavoritesDatabase dbHelper = new FavoritesDatabase(getContext());
        List<Song> favorites = dbHelper.read(mLimit);
        dbHelper.close();
        return favorites;
    }
}
