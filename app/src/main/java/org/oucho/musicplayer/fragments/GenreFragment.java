package org.oucho.musicplayer.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import org.oucho.musicplayer.loaders.GenreSongLoader;
import org.oucho.musicplayer.model.Genre;
import org.oucho.musicplayer.model.Song;

import java.util.List;


public class GenreFragment extends SongListFragment {

    private static final String PARAM_GENRE_ID = "genre_id";

    private long mGenreId;
    private final LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            GenreSongLoader loader = new GenreSongLoader(getActivity(), mGenreId);

            loader.setSortOrder(MediaStore.Audio.Media.TITLE);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            populateAdapter(songList);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub
        }
    };

    public GenreFragment() {
        super();
    }

    public static GenreFragment newInstance(Genre genre) {

        Bundle args = new Bundle();
        args.putLong(PARAM_GENRE_ID, genre.getId());

        GenreFragment fragment = new GenreFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if(args != null) {
            mGenreId = args.getLong(PARAM_GENRE_ID);


        }
    }

    @Override
    protected LoaderManager.LoaderCallbacks<List<Song>> getLoaderCallbacks() {
        return mLoaderCallbacks;
    }
}
