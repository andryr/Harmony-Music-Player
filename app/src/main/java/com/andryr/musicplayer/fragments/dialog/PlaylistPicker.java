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

package com.andryr.musicplayer.fragments.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.adapters.PlaylistListAdapter;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.utils.ThemeHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class PlaylistPicker extends DialogFragment {

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};

    private RecyclerView mRecyclerView;

    private PlaylistListAdapter mAdapter;

    private OnPlaylistPickedListener mListener;

    private LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

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
    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.new_playlist:
                    CreatePlaylistDialog dialog = CreatePlaylistDialog.newInstance();
                    dialog.setOnPlaylistCreatedListener(new CreatePlaylistDialog.OnPlaylistCreatedListener() {
                        @Override
                        public void onPlaylistCreated() {
                            refresh();
                        }
                    });
                    dialog.show(getChildFragmentManager(), "create_playlist");
                    break;
            }


        }
    };
    private BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Playlist playlist = mAdapter.getItem(position);

            if (mListener != null) {
                mListener.onPlaylistPicked(playlist);
            }

            dismiss();
        }
    };

    public PlaylistPicker() {
        // Required empty public constructor
    }

    public static PlaylistPicker newInstance() {
        PlaylistPicker fragment = new PlaylistPicker();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity(), getTheme());
        mAdapter = new PlaylistListAdapter();
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        builder.setTitle(R.string.choose_playlist);

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_playlist_picker, null);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setAdapter(mAdapter);


        Button newPlaylistButton = (Button) rootView.findViewById(R.id.new_playlist);
        newPlaylistButton.setOnClickListener(mOnClickListener);
        ThemeHelper.tintCompoundDrawables(getActivity(), newPlaylistButton);

        builder.setView(rootView);
        return builder.create();
    }


    public void refresh() {
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

    public void setListener(OnPlaylistPickedListener listener) {
        mListener = listener;
    }

    public interface OnPlaylistPickedListener {
        void onPlaylistPicked(Playlist playlist);
    }


}
