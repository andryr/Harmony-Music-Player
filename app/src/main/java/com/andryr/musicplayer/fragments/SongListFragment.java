package com.andryr.musicplayer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.adapters.AdapterWithHeader;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.adapters.SongListAdapter;
import com.andryr.musicplayer.fragments.dialog.ID3TagEditorDialog;
import com.andryr.musicplayer.fragments.dialog.PlaylistPicker;
import com.andryr.musicplayer.loaders.SongLoader;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.Playlists;
import com.andryr.musicplayer.utils.RecyclerViewUtils;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.FastScroller;

import java.util.List;


public class SongListFragment extends BaseFragment {


    private static final String STATE_SHOW_TOOLBAR = "toolbar";
    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";


    private MainActivity mActivity;

    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;

    private boolean mShowToolbar = false;
    private boolean mShowFastScroller = true;


    private LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(getActivity());

            loader.setOrder(MediaStore.Audio.Media.TITLE);
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

    protected void populateAdapter(List<Song> songList) {
        mAdapter.setData(songList);
    }

    private ID3TagEditorDialog.OnTagsEditionSuccessListener mOnTagsEditionSuccessListener = new ID3TagEditorDialog.OnTagsEditionSuccessListener() {
        @Override
        public void onTagsEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };
    private BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:


                    selectSong(position);
                    break;
                case R.id.menu_button:
                    showMenu(position, view);
                    break;
            }
        }
    };
    private AdapterWithHeader.OnHeaderClickListener mOnHeaderClickListener = new AdapterWithHeader.OnHeaderClickListener() {
        @Override
        public void onHeaderClick() {
            if (mActivity != null) {
                mActivity.onShuffleRequested(mAdapter.getSongList(), true);
            }
        }
    };

    public SongListFragment() {
        // Required empty public constructor
    }

    public static SongListFragment newInstance() {
        SongListFragment fragment = new SongListFragment();

        return fragment;
    }


    public void showMenu(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        final Song song = mAdapter.getItem(position);
        inflater.inflate(R.menu.song_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_to_queue:
                        ((MainActivity) getActivity()).addToQueue(song);
                        return true;
                    case R.id.action_set_as_next_track:
                        ((MainActivity) getActivity()).setAsNextTrack(song);
                        return true;
                    case R.id.action_edit_tags:
                        showID3TagEditor(song);
                        return true;
                    case R.id.action_add_to_playlist:
                        showPlaylistPicker(song);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showID3TagEditor(Song song) {
        ID3TagEditorDialog dialog = ID3TagEditorDialog.newInstance(song);
        dialog.setOnTagsEditionSuccessListener(mOnTagsEditionSuccessListener);
        dialog.show(getChildFragmentManager(), "edit_tags");
    }

    private void showPlaylistPicker(final Song song) {
        PlaylistPicker picker = PlaylistPicker.newInstance();
        picker.setListener(new PlaylistPicker.OnPlaylistPickedListener() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                Playlists.addSongToPlaylist(getActivity().getContentResolver(), playlist.getId(), song.getId());
            }
        });
        picker.show(getChildFragmentManager(), "pick_playlist");

    }

    public SongListFragment showToolbar(boolean show) {
        mShowToolbar = show;
        return this;
    }

    public SongListFragment showFastScroller(boolean show) {
        mShowFastScroller = show;
        return this;
    }

    private void selectSong(int position) {

        if (mActivity != null) {
            mActivity.onSongSelected(mAdapter.getSongList(), position);
        }
    }

    @Override
    public void load() {
        Log.d("frag", "ertr");

        getLoaderManager().restartLoader(0, null, getLoaderCallbacks());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (MainActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_song_list,
                container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new SongListAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        View headerView = RecyclerViewUtils.inflateChild(inflater, R.layout.shuffle_list_item, mRecyclerView);
        ThemeHelper.tintCompoundDrawables(getContext(), (TextView) headerView.findViewById(R.id.text_view));

        mAdapter.setHeaderView(headerView);
        mAdapter.setOnHeaderClickListener(mOnHeaderClickListener);
        mRecyclerView.setAdapter(mAdapter);


        if (savedInstanceState != null) {
            mShowToolbar = savedInstanceState.getBoolean(STATE_SHOW_TOOLBAR)
                    || mShowToolbar;
            mShowFastScroller = savedInstanceState
                    .getBoolean(STATE_SHOW_FASTSCROLLER) || mShowFastScroller;
        }

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        Log.e("fastsc", String.valueOf(mShowFastScroller));
        if (mShowFastScroller) {
            scroller.setRecyclerView(mRecyclerView);
            scroller.setSectionIndexer(mAdapter);
        } else {
            scroller.setVisibility(View.GONE);
        }

        if (mShowToolbar) {
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, getLoaderCallbacks());

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOW_TOOLBAR, mShowToolbar);
        outState.putBoolean(STATE_SHOW_FASTSCROLLER, mShowFastScroller);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }


    protected LoaderCallbacks<List<Song>> getLoaderCallbacks() {
        return mLoaderCallbacks;
    }
}
