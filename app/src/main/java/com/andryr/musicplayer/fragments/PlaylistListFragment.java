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

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.adapters.AdapterWithHeader;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.adapters.PlaylistListAdapter;
import com.andryr.musicplayer.fragments.dialog.CreatePlaylistDialog;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.utils.RecyclerViewUtils;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.FastScroller;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PlaylistListFragment extends BaseFragment {

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};

    private RecyclerView mRecyclerView;

    private PlaylistListAdapter mAdapter;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            List<Playlist> list = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
                int nameCol = cursor
                        .getColumnIndex(MediaStore.Audio.Genres.NAME);

                do {
                    long id = cursor.getLong(idCol);
                    String name = cursor.getString(nameCol);
                    list.add(new Playlist(id, name));
                } while (cursor.moveToNext());

                Collections.sort(list, new Comparator<Playlist>() {

                    @Override
                    public int compare(Playlist lhs, Playlist rhs) {
                        Collator c = Collator.getInstance(Locale.getDefault());
                        c.setStrength(Collator.PRIMARY);
                        return c.compare(lhs.getName(), rhs.getName());
                    }
                });

            }

            mAdapter.setData(list);

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            Uri playlistsUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

            CursorLoader loader = new CursorLoader(getActivity(), playlistsUri,
                    sProjection, null, null, null);

            return loader;
        }
    };

    private BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Playlist playlist = mAdapter.getItem(position);

            PlaylistFragment fragment = PlaylistFragment.newInstance(playlist);

            ((MainActivity) getActivity()).setFragment(fragment);
        }
    };

    public PlaylistListFragment() {
        // Required empty public constructor
    }

    public static PlaylistListFragment newInstance() {
        PlaylistListFragment fragment = new PlaylistListFragment();

        return fragment;
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
        View rootView = inflater.inflate(R.layout.fragment_playlist_list,
                container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mAdapter = new PlaylistListAdapter();

        /*View headerView = RecyclerViewUtils.inflateChild(inflater, R.layout.favorites_card, mRecyclerView);
        ThemeHelper.tintCompoundDrawables(getContext(), (TextView) headerView.findViewById(R.id.text_view));

        mAdapter.setHeaderView(headerView);*/
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        /*mAdapter.setOnHeaderClickListener(new AdapterWithHeader.OnHeaderClickListener() {
            @Override
            public void onHeaderClick() {

                PlaylistFragment fragment = PlaylistFragment.newFavoritesFragment();

                ((MainActivity) getActivity()).setFragment(fragment);
            }
        });*/
        mRecyclerView.setAdapter(mAdapter);

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        scroller.setRecyclerView(mRecyclerView);
        scroller.setSectionIndexer(mAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_list, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_playlist:
                showCreatePlaylistDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreatePlaylistDialog() {
        CreatePlaylistDialog dialog = CreatePlaylistDialog.newInstance();
        dialog.setOnPlaylistCreatedListener(new CreatePlaylistDialog.OnPlaylistCreatedListener() {
            @Override
            public void onPlaylistCreated() {
                load();
            }
        });
        dialog.show(getChildFragmentManager(), "create_playlist");

    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
