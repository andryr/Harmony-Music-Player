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

package com.andryr.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.andryr.musicplayer.loaders.GenreSongLoader;
import com.andryr.musicplayer.loaders.SongLoader;
import com.andryr.musicplayer.model.Genre;
import com.andryr.musicplayer.model.Song;

import java.util.List;

/**
 * Created by Andry on 01/02/16.
 */
public class GenreFragment extends SongListFragment {

    public static final String PARAM_GENRE_ID = "genre_id";

    private long mGenreId;
    private LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {

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
