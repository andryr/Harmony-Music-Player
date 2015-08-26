package com.andryr.musicplayer.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andryr.musicplayer.DividerItemDecoration;
import com.andryr.musicplayer.FastScroller;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.Playlist;
import com.andryr.musicplayer.Playlists;
import com.andryr.musicplayer.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link SongListFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link PlaylistBrowserFragment#newInstance} factory method to create an
 * instance of this fragment.
 */
public class PlaylistBrowserFragment extends BaseFragment {

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};

    private RecyclerView mRecyclerView;

    private List<Playlist> mPlaylists = new ArrayList<>();
    private PlaylistsAdapter mAdapter;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

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
            int position = mRecyclerView.getChildPosition(v);

            Playlist playlist = mPlaylists.get(position);

            PlaylistFragment fragment = PlaylistFragment.newInstance(playlist);

            ((MainActivity) getActivity()).setFragment(fragment);

        }
    };

    public static PlaylistBrowserFragment newInstance() {
        PlaylistBrowserFragment fragment = new PlaylistBrowserFragment();

        return fragment;
    }

    public PlaylistBrowserFragment() {
        // Required empty public constructor
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
        View rootView = inflater.inflate(R.layout.fragment_genre_list,
                container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mAdapter = new PlaylistsAdapter();
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
        inflater.inflate(R.menu.playlist_browser, menu);

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
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.create_playlist_dialog,
                new LinearLayout(getActivity()), false);
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_playlist)
                .setView(layout)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                EditText editText = (EditText) layout
                                        .findViewById(R.id.playlist_name);
                                Playlists.createPlaylist(getActivity()
                                        .getContentResolver(), editText
                                        .getText().toString());
                                getLoaderManager().restartLoader(0, null,
                                        mLoaderCallbacks);
                                mRecyclerView.getAdapter()
                                        .notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {

        TextView vName;
        TextView vArtist;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.name);
        }

    }

    class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistViewHolder>
            implements FastScroller.SectionIndexer {

        public PlaylistsAdapter() {

            List<String> sectionList = new ArrayList<>();


        }

        @Override
        public int getItemCount() {
            return mPlaylists.size();
        }

        @Override
        public void onBindViewHolder(PlaylistViewHolder viewHolder, int position) {
            Playlist playlist = mPlaylists.get(position);
            viewHolder.vName.setText(playlist.getName());

        }

        @Override
        public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.playlist_browser_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);
            return new PlaylistViewHolder(itemView);
        }


        @Override
        public String getSectionForPosition(int position) {
            return mPlaylists.get(position).getName().substring(0, 1);
        }
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
