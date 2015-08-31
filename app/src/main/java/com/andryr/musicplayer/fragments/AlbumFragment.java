package com.andryr.musicplayer.fragments;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
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

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.ImageUtils;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.OnSongSelectedListener;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.RecyclerViewAdapter;
import com.andryr.musicplayer.Song;
import com.andryr.musicplayer.loaders.SongLoader;

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
            SongLoader loader = new SongLoader(getActivity(), SongLoader.ALBUM_SONGS, 0, mAlbum.getId(), 0);
            return loader;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            View itemView = (View) v.getParent();

            if (itemView == null) {
                return;
            }
            int position = mAdapter.getViewPosition(itemView);

            Song song = mAdapter.getItem(position);
            Log.d("album", "album id " + song.getAlbumId() + " " + song.getAlbum());
            switch (v.getId()) {
                case R.id.item_view:


                    selectSong(position);
                    break;
                case R.id.menu_button:
                    showMenu(position, v);
                    break;
            }


        }
    };
    private ID3TagEditorDialog.OnTagsEditionSuccessListener mOnTagsEditionSuccessListener = new ID3TagEditorDialog.OnTagsEditionSuccessListener() {
        @Override
        public void onTagsEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };

    private OnSongSelectedListener mListener;

    private void selectSong(int position) {

        if (mListener != null) {
            mListener.onSongSelected(mAdapter.mSongList, position);
        }
    }


    public void showMenu(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.song_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_to_queue:
                        ((MainActivity) getActivity()).addToQueue(mAdapter.getItem(position));
                        return true;
                    case R.id.action_set_as_next_track:
                        ((MainActivity) getActivity()).setAsNextTrack(mAdapter.getItem(position));
                        return true;
                    case R.id.action_edit_tags:
                        ID3TagEditorDialog dialog = ID3TagEditorDialog.newInstance(mAdapter.getItem(position));
                        dialog.setOnTagsEditionSuccessListener(mOnTagsEditionSuccessListener);
                        dialog.show(getChildFragmentManager(), "edit_tags");
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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

    public AlbumFragment() {
        // Required empty public constructor
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

        mAdapter = new SongListAdapter(mRecyclerView);


        ImageView artworkView = (ImageView) mAdapter.setHeader(R.layout.artwork_view);

        ImageUtils.loadArtworkAsync(mAlbum.getId(), artworkView);


        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable background = mToolbar.getBackground();
        background.setAlpha(0);

        mAdapter.setParallaxEnabled(true);
        mAdapter.setOnParallaxScrollListener(new RecyclerViewAdapter.OnParallaxScrollListener() {
            @Override
            public void onParallaxScroll(float offset) {
                Drawable background = mToolbar.getBackground();
                background.setAlpha(Math.round(offset * 255));

            }
        });
        return rootView;
    }

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    class SongListAdapter extends RecyclerViewAdapter {


        private static final int FIRST_VIEW = 1;
        private static final int NORMAL_VIEW = 2;


        private List<Song> mSongList;


        public SongListAdapter(RecyclerView recyclerView) {
            super(recyclerView);
        }

        class SongViewHolder extends RecyclerView.ViewHolder {

            private final TextView vTitle;
            private final TextView vArtist;

            public SongViewHolder(View itemView) {
                super(itemView);
                vTitle = (TextView) itemView.findViewById(R.id.title);
                vArtist = (TextView) itemView.findViewById(R.id.artist);
            }
        }

        class AlbumInfoViewHolder extends RecyclerView.ViewHolder {

            private final TextView vAlbumName;
            private final TextView vTrackCount;

            public AlbumInfoViewHolder(View itemView) {
                super(itemView);
                vAlbumName = (TextView) itemView.findViewById(R.id.album_name);
                vTrackCount = (TextView) itemView.findViewById(R.id.track_count);
            }
        }

        public void setData(List<Song> data) {
            mSongList = data;
            notifyDataSetChanged();
        }

        public Song getItem(int position) {
            return mSongList == null ? null : mSongList.get(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, int viewType) {
            View itemView = null;

            if (viewType == NORMAL_VIEW) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.song_list_item, parent, false);
                itemView.findViewById(R.id.item_view).setOnClickListener(mOnClickListener);

                ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
                menuButton.setOnClickListener(mOnClickListener);

                Drawable drawable = menuButton.getDrawable();

                drawable.mutate();
                drawable.setColorFilter(getActivity().getResources().getColor(R.color.primary_text), PorterDuff.Mode.SRC_ATOP);
                SongViewHolder songViewHolder = new SongViewHolder(itemView);

                return songViewHolder;
            } else if (viewType == FIRST_VIEW) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.album_info, parent, false);
                AlbumInfoViewHolder albumInfoViewHolder = new AlbumInfoViewHolder(itemView);
                return albumInfoViewHolder;
            }


            return null;
        }

        @Override
        public void onBindViewHolderImpl(RecyclerView.ViewHolder holder, int position) {

            if (getItemViewTypeImpl(position) == FIRST_VIEW) {
                ((AlbumInfoViewHolder) holder).vAlbumName.setText(mAlbum.getAlbumName());
                ((AlbumInfoViewHolder) holder).vTrackCount.setText(getActivity().getResources().getQuantityString(R.plurals.track_count, mAlbum.getTrackCount(), mAlbum.getTrackCount()));
                return;
            }
            Song song = mAdapter.getItem(position);

            ((SongViewHolder) holder).vTitle.setText(song.getTitle());
            ((SongViewHolder) holder).vArtist.setText(song.getArtist());
        }

        @Override
        public int getItemCountImpl() {
            return mSongList == null ? 0 : mSongList.size()+1;
        }

        @Override
        public int getItemViewTypeImpl(int position) {
            return position == 0 ? FIRST_VIEW : NORMAL_VIEW;
        }
    }


}
