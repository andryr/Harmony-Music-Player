package org.oucho.musicplayer.fragments.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.adapters.BaseAdapter;
import org.oucho.musicplayer.adapters.PlaylistListAdapter;
import org.oucho.musicplayer.model.Playlist;
import org.oucho.musicplayer.utils.ThemeHelper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class PlaylistPicker extends DialogFragment {

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};

    private PlaylistListAdapter mAdapter;

    private OnPlaylistPickedListener mListener;

    private final LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

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

            return new CursorLoader(getActivity(), playlistsUri,
                    sProjection, null, null, null);
        }
    };
    private final OnClickListener mOnClickListener = new OnClickListener() {

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
    private final BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
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

        return new PlaylistPicker();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity(), getTheme());
        mAdapter = new PlaylistListAdapter();
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        builder.setTitle(R.string.choose_playlist);

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_playlist_picker, null);
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setAdapter(mAdapter);


        Button newPlaylistButton = (Button) rootView.findViewById(R.id.new_playlist);
        newPlaylistButton.setOnClickListener(mOnClickListener);
        ThemeHelper.tintCompoundDrawables(getActivity(), newPlaylistButton);

        builder.setView(rootView);
        return builder.create();
    }


    private void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    public void setListener(OnPlaylistPickedListener listener) {
        mListener = listener;
    }

    public interface OnPlaylistPickedListener {
        void onPlaylistPicked(Playlist playlist);
    }


}
