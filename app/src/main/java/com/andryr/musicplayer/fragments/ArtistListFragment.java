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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.adapters.ArtistListAdapter;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.loaders.ArtistLoader;
import com.andryr.musicplayer.loaders.SortOrder;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.utils.NavigationUtils;
import com.andryr.musicplayer.utils.PrefUtils;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ArtistListFragment#newInstance} factory method to create an instance
 * of this fragment.
 */
public class ArtistListFragment extends BaseFragment {


    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";

    private RecyclerView mRecyclerView;

    private ArtistListAdapter mAdapter;

    private boolean mShowFastScroller = true;

    private LoaderManager.LoaderCallbacks<List<Artist>> mLoaderCallbacks = new LoaderCallbacks<List<Artist>>() {

        @Override
        public void onLoaderReset(Loader<List<Artist>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
            mAdapter.setData(data);

            PrefUtils prefUtils = PrefUtils.getInstance();
            String sortOrder = prefUtils.getArtistSortOrder();

            mShowScrollerBubble = SortOrder.ArtistSortOrder.ARTIST_A_Z.equals(sortOrder) || SortOrder.ArtistSortOrder.ARTIST_Z_A.equals(sortOrder);

            if (mFastScroller != null) {
                mFastScroller.setShowBubble(mShowScrollerBubble);
            }
        }

        @Override
        public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
            ArtistLoader loader = new ArtistLoader(getActivity());
            loader.setSortOrder(PrefUtils.getInstance().getArtistSortOrder());

            return loader;
        }
    };


    private BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Artist artist = mAdapter.getItem(position);

            Fragment fragment = ArtistFragment.newInstance(artist);
            /*ImageView artistImage = (ImageView) view.findViewById(R.id.artist_image);
            ViewCompat.setTransitionName(artistImage, "artist_image");*/
            NavigationUtils.showFragment(getActivity(), ArtistListFragment.this, fragment/*, new Pair<View, String>(artistImage, "artist_image")*/);
        }
    };
    private boolean mShowScrollerBubble = true;
    private FastScroller mFastScroller;


    public ArtistListFragment() {
        // Required empty public constructor
    }

    public static ArtistListFragment newInstance() {
        ArtistListFragment fragment = new ArtistListFragment();

        return fragment;
    }

    public ArtistListFragment showFastScroller(boolean show) {
        mShowFastScroller = show;
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list,
                container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mAdapter = new ArtistListAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mShowFastScroller = savedInstanceState
                    .getBoolean(STATE_SHOW_FASTSCROLLER) || mShowFastScroller;
        }

        mFastScroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        mFastScroller.setShowBubble(mShowScrollerBubble);
        if (mShowFastScroller) {
            mFastScroller.setRecyclerView(mRecyclerView);
            mFastScroller.setSectionIndexer(mAdapter);
        } else {
            mFastScroller.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artist_sort_by, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PrefUtils prefUtils = PrefUtils.getInstance();
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                prefUtils.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                prefUtils.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_Z_A);
                load();
                break;
            case R.id.menu_sort_by_number_of_songs:
                prefUtils.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS);
                load();
                break;
            case R.id.menu_sort_by_number_of_albums:
                prefUtils.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS);
                load();
                break;


        }
        return super.onOptionsItemSelected(item);
    }
}
