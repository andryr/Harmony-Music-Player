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


import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.FavoritesLoader;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.images.ArtworkHelper;

import java.util.List;


public class HomeFragment extends BaseFragment {


    private LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {


        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
                return new FavoritesLoader(getActivity(), 3);

        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
            populateFavorites(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {

        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId())
            {
                case R.id.action_favorites:
                    ((MainActivity)getActivity()).showFavorites();
                    break;
            }
        }
    };

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();

        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);




        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        load();
    }

    private void populateFavorites(List<Song> favorites)
    {
        if(favorites != null)
        {
            GridLayout favoritesLayout = (GridLayout)getView().findViewById(R.id.favorites_layout);

            float favWidth = getContext().getResources().getDimension(R.dimen.album_grid_item_width);
            float favHeight = getContext().getResources().getDimension(R.dimen.album_grid_item_height);

            LayoutInflater inflater = LayoutInflater.from(getContext());
            int max = Math.min(favorites.size(),3);
            for(int i = 0; i < max; i++)
            {
                Song song = favorites.get(i);
                View v = inflater.inflate(R.layout.album_grid_item, favoritesLayout, false);
                favoritesLayout.addView(v);

                ImageView artworkView = (ImageView) v.findViewById(R.id.album_artwork);
            }
        }
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }



}
