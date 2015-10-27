package com.andryr.musicplayer.fragments;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.FragmentListener;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.fragments.dialog.ID3TagEditorDialog;
import com.andryr.musicplayer.utils.ArtworkHelper;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.utils.Playlists;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.loaders.SongLoader;
import com.andryr.musicplayer.utils.ThemeHelper;

import java.util.List;

public class AlbumFragment extends BaseFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_ARTIST = "artist";
    private static final String ARG_YEAR = "year";
    private static final String ARG_TRACK_COUNT = "track_count";


    private Toolbar mToolbar;

    private Album mAlbum;

    private SongListAdapter mAdapter;

    private RecyclerView mRecyclerView;


    private LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            mAdapter.setData(songList);
            Log.e("test", "" + mAdapter.getItemCount());
        }

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(getActivity());
            loader.setSongListType(SongLoader.ALBUM_SONGS);
            loader.setAlbumId(mAlbum.getId());
            loader.setOrder(MediaStore.Audio.Media.TRACK);
            return loader;
        }
    };


    private ID3TagEditorDialog.OnTagsEditionSuccessListener mOnTagsEditionSuccessListener = new ID3TagEditorDialog.OnTagsEditionSuccessListener() {
        @Override
        public void onTagsEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };

    private FragmentListener mListener;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shuffle_fab:
                    if (mListener != null) {
                        mListener.onShuffleRequested(mAdapter.mSongList, true);
                    }
                    break;
            }
        }
    };

    public AlbumFragment() {
        // Required empty public constructor
    }

    public static AlbumFragment newInstance(Album album) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, album.getId());
        args.putString(ARG_NAME, album.getAlbumName());
        args.putString(ARG_ARTIST, album.getArtistName());
        args.putInt(ARG_YEAR, album.getYear());
        args.putInt(ARG_TRACK_COUNT, album.getTrackCount());
        fragment.setArguments(args);
        return fragment;
    }

    private void selectSong(int position) {

        if (mListener != null) {
            mListener.onSongSelected(mAdapter.mSongList, position);
        }
    }

    public void showMenu(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        final Song song = mAdapter.getItem(position);
        inflater.inflate(R.menu.song_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {

            long id = args.getLong(ARG_ID);
            String title = args.getString(ARG_NAME);
            String artist = args.getString(ARG_ARTIST);
            int year = args.getInt(ARG_YEAR);
            int trackCount = args.getInt(ARG_TRACK_COUNT);

            mAlbum = new Album(id, title, artist, year, trackCount);


        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container,
                false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.song_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new SongListAdapter();

        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        fab.setColorFilter(getActivity().getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        fab.setOnClickListener(mOnClickListener);

        ImageView artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);

        ArtworkHelper.loadArtworkAsync(mAlbum.getId(), artworkView);


        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mAlbum.getAlbumName());

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      /*  Drawable background = mToolbar.getBackground();
        background.mutate();
        background.setAlpha(0);

        mAdapter.setParallaxEnabled(true);
        mAdapter.setOnParallaxScrollListener(new RecyclerViewAdapter.OnParallaxScrollListener() {
            @Override
            public void onParallaxScroll(float offset) {
                Drawable background = mToolbar.getBackground();
                background.setAlpha(Math.round(offset * 255));

            }
        });*/
        return rootView;
    }

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView vTitle;
        private final TextView vArtist;


        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            itemView.findViewById(R.id.item_view).setOnClickListener(this);

            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);


            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();
            ThemeHelper.tintDrawable(getActivity(), drawable);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();


            switch (v.getId()) {
                case R.id.item_view:


                    selectSong(position);
                    break;
                case R.id.menu_button:
                    showMenu(position, v);
                    break;
            }
        }
    }

    class SongListAdapter extends RecyclerView.Adapter<SongViewHolder> {


        private List<Song> mSongList;


        public void setData(List<Song> data) {
            mSongList = data;
            notifyDataSetChanged();
        }

        public Song getItem(int position) {
            return mSongList == null ? null : mSongList.get(position);
        }


        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.song_list_item, parent, false);

            return new SongViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SongViewHolder holder, int position) {
            Song song = getItem(position);

            holder.vTitle.setText(song.getTitle());
            holder.vArtist.setText(song.getArtist());
        }

        @Override
        public int getItemCount() {
            return mSongList == null ? 0 : mSongList.size();
        }
    }


}
