package org.oucho.musicplayer.fragments;


import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.loaders.FavoritesLoader;
import org.oucho.musicplayer.model.Song;

import java.util.List;


public class HomeFragment extends BaseFragment {


    private final LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {


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

    public HomeFragment() {
        // Required empty public constructor
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


            LayoutInflater inflater = LayoutInflater.from(getContext());
            int max = Math.min(favorites.size(),3);
            for(int i = 0; i < max; i++)
            {
                View v = inflater.inflate(R.layout.album_grid_item, favoritesLayout, false);
                favoritesLayout.addView(v);
            }
        }
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }
}
