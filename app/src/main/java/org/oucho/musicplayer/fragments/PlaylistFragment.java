package org.oucho.musicplayer.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.activities.MusicPicker;
import org.oucho.musicplayer.utils.FavoritesHelper;
import org.oucho.musicplayer.loaders.FavoritesLoader;
import org.oucho.musicplayer.loaders.PlaylistLoader;
import org.oucho.musicplayer.model.Playlist;
import org.oucho.musicplayer.model.Song;
import org.oucho.musicplayer.utils.Playlists;
import org.oucho.musicplayer.utils.ThemeHelper;
import org.oucho.musicplayer.widgets.DragRecyclerView;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlaylistFragment extends BaseFragment {

    private static final String PARAM_PLAYLIST_FAVORITES = "favorites";

    private static final String PARAM_PLAYLIST_ID = "playlist_id";
    private static final String PARAM_PLAYLIST_NAME = "playlist_name";

    private static final int PICK_MUSIC = 22;


    private MainActivity mActivity;

    private ArrayList<Song> mSongList = new ArrayList<>();
    private DragRecyclerView mRecyclerView;

    private Playlist mPlaylist;

    private SongListAdapter mAdapter;
    private boolean mFavorites = false;
    private final LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderCallbacks<List<Song>>() {


        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            if (mFavorites) {
                return new FavoritesLoader(getActivity());
            }
            return new PlaylistLoader(getActivity(), mPlaylist.getId());
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
            mSongList = new ArrayList<>(data);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {

        }
    };

    public static PlaylistFragment newInstance(Playlist playlist) {
        PlaylistFragment fragment = new PlaylistFragment();

        Bundle args = new Bundle();

        args.putLong(PARAM_PLAYLIST_ID, playlist.getId());
        args.putString(PARAM_PLAYLIST_NAME, playlist.getName());

        fragment.setArguments(args);
        return fragment;
    }

    public static PlaylistFragment newFavoritesFragment() {
        PlaylistFragment fragment = new PlaylistFragment();

        Bundle args = new Bundle();

        args.putBoolean(PARAM_PLAYLIST_FAVORITES, true);

        fragment.setArguments(args);
        return fragment;
    }

    private void selectSong(int position) {

        if (mActivity != null) {
            mActivity.onSongSelected(mSongList, position);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_MUSIC && resultCode == Activity.RESULT_OK) {
            long[] ids = data.getExtras().getLongArray(MusicPicker.EXTRA_IDS);
            addToPlaylist(ids);
        }
    }

    private void addToPlaylist(final long[] ids) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;

            @Override
            protected Void doInBackground(Void... params) {
                if (mFavorites) {
                    for (long id : ids) {
                        FavoritesHelper.addFavorite(getActivity(), id);
                    }
                } else {
                    ContentResolver resolver = getActivity().getContentResolver();
                    for (long id : ids) {
                        Playlists.addSongToPlaylist(resolver, mPlaylist.getId(), id);
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(getActivity(), getString(R.string.loading), getString(R.string.adding_songs_to_playlist), true);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.dismiss();
                getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

            }
        }.execute();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActivity = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        if (args != null) {

            if (args.getBoolean(PARAM_PLAYLIST_FAVORITES)) {
                mFavorites = true;
            } else {
                long id = args.getLong(PARAM_PLAYLIST_ID);
                String name = args.getString(PARAM_PLAYLIST_NAME);
                mPlaylist = new Playlist(id, name);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container,
                false);

        mRecyclerView = (DragRecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new SongListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnItemMovedListener(new DragRecyclerView.OnItemMovedListener() {
            @Override
            public void onItemMoved(int oldPosition, int newPosition) {
                mAdapter.moveItem(oldPosition, newPosition);
            }
        });

        FastScroller scroller = (FastScroller) rootView.findViewById(R.id.fastscroller);
        scroller.setSectionIndexer(mAdapter);
        scroller.setRecyclerView(mRecyclerView);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        load();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                startActivityForResult(
                        new Intent(getActivity(), MusicPicker.class), PICK_MUSIC);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnTouchListener {


        final View itemView;
        final TextView vTitle;
        final TextView vArtist;
        final ImageButton vReorderButton;

        public SongViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            vReorderButton = (ImageButton) itemView
                    .findViewById(R.id.reorder_button);
            itemView.findViewById(R.id.song_info).setOnClickListener(this);
            itemView.findViewById(R.id.delete_button).setOnClickListener(this);
            vReorderButton.setOnTouchListener(this);
            ThemeHelper.tintImageView(getActivity(), vReorderButton);
            ThemeHelper.tintImageView(getActivity(), (ImageView) itemView.findViewById(R.id.delete_button));
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            switch (v.getId()) {
                case R.id.song_info:
                    selectSong(position);
                    break;
                case R.id.delete_button:
                    mAdapter.removeItem(position);
                    break;

            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mRecyclerView.startDrag(itemView);

            return false;
        }
    }

    class SongListAdapter extends RecyclerView.Adapter<SongViewHolder>
            implements FastScroller.SectionIndexer {


        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.playlist_item, parent, false);


            return new SongViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SongViewHolder viewHolder, int position) {
            Song song = mSongList.get(position);
            viewHolder.vTitle.setText(song.getTitle());
            viewHolder.vArtist.setText(song.getArtist());

        }

        @Override
        public int getItemCount() {
            return mSongList.size();
        }

        public void moveItem(int oldPosition, int newPosition) {
            if (oldPosition < 0 || oldPosition >= mSongList.size()
                    || newPosition < 0 || newPosition >= mSongList.size()) {
                return;
            }
            Collections.swap(mSongList, oldPosition, newPosition);
            if (!mFavorites) {
                Playlists.moveItem(getActivity().getContentResolver(), mPlaylist.getId(), oldPosition, newPosition);
            }
            notifyItemMoved(oldPosition, newPosition);

        }

        public void removeItem(int position) {
            Song s = mSongList.remove(position);
            if (mFavorites) {
                FavoritesHelper.removeFromFavorites(getActivity(), s.getId());
            } else {
                Playlists.removeFromPlaylist(getActivity().getContentResolver(),
                        mPlaylist.getId(), s.getId());
            }
            notifyItemRemoved(position);
        }

        @Override
        public String getSectionForPosition(int position) {
            return mSongList.get(position).getTitle().substring(0, 1);
        }
    }

}
