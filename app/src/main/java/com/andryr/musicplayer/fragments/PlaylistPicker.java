package com.andryr.musicplayer.fragments;

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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.widgets.FastScroller;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.fragments.dialog.CreatePlaylistDialog;
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

    private List<Playlist> mPlaylists = new ArrayList<>();
    private PlaylistsAdapter mAdapter;

    private OnPlaylistPickedListener mListener;

    private LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mPlaylists.clear();
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
                int nameCol = cursor
                        .getColumnIndex(MediaStore.Audio.Genres.NAME);

                do {
                    long id = cursor.getLong(idCol);
                    String name = cursor.getString(nameCol);
                    mPlaylists.add(new Playlist(id, name));
                } while (cursor.moveToNext());

                Collections.sort(mPlaylists, new Comparator<Playlist>() {

                    @Override
                    public int compare(Playlist lhs, Playlist rhs) {
                        Collator c = Collator.getInstance(Locale.getDefault());
                        c.setStrength(Collator.PRIMARY);
                        return c.compare(lhs.getName(), rhs.getName());
                    }
                });

            }

            mAdapter.notifyDataSetChanged();

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
            int position = mRecyclerView.getChildAdapterPosition(v);

            if (position == mPlaylists.size()) {
                CreatePlaylistDialog dialog = CreatePlaylistDialog.newInstance();
                dialog.setOnPlaylistCreatedListener(new CreatePlaylistDialog.OnPlaylistCreatedListener() {
                    @Override
                    public void onPlaylistCreated() {
                        refresh();
                    }
                });
                dialog.show(getChildFragmentManager(), "create_playlist");
                return;
            }
            Playlist playlist = mPlaylists.get(position);

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
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        mAdapter = new PlaylistsAdapter();

        builder.setTitle(R.string.choose_playlist);

        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list_dialog, null);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRecyclerView.setAdapter(mAdapter);


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

    class PlaylistViewHolder extends RecyclerView.ViewHolder {

        ImageView vIcon;
        TextView vName;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            vIcon = (ImageView) itemView.findViewById(R.id.icon);
            vName = (TextView) itemView.findViewById(R.id.name);

            ThemeHelper.tintImageView(getActivity(), (ImageView) itemView.findViewById(R.id.icon));
        }

    }

    class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistViewHolder>
            implements FastScroller.SectionIndexer {

        public PlaylistsAdapter() {


        }

        @Override
        public int getItemCount() {
            return mPlaylists.size() + 1;
        }

        @Override
        public void onBindViewHolder(PlaylistViewHolder viewHolder, int position) {
            if (position < mPlaylists.size()) {
                Playlist playlist = mPlaylists.get(position);
                viewHolder.vIcon.setImageResource(R.drawable.ic_playlist);
                viewHolder.vName.setText(playlist.getName());
            } else {
                viewHolder.vIcon.setImageResource(R.drawable.ic_new_playlist);
                viewHolder.vName.setText(R.string.new_playlist);
            }

        }

        @Override
        public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.playlist_picker_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);
            return new PlaylistViewHolder(itemView);
        }


        @Override
        public String getSectionForPosition(int position) {
            return mPlaylists.get(position).getName().substring(0, 1);
        }
    }

}
