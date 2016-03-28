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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.adapters.AlbumListAdapter;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.fragments.dialog.AlbumEditorDialog;
import com.andryr.musicplayer.fragments.dialog.ID3TagEditorDialog;
import com.andryr.musicplayer.fragments.dialog.PlaylistPicker;
import com.andryr.musicplayer.images.ArtistImageCache;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.loaders.AlbumLoader;
import com.andryr.musicplayer.loaders.SongLoader;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.Playlists;
import com.andryr.musicplayer.utils.ThemeHelper;

import java.util.List;

public class ArtistFragment extends BaseFragment {

    private static final String PARAM_ARTIST_ID = "artist_id";
    private static final String PARAM_ARTIST_NAME = "artist_name";
    private static final String PARAM_ALBUM_COUNT = "album_count";
    private static final String PARAM_TRACK_COUNT = "track_count";


    private Artist mArtist;

    private SongListAdapter mSongListAdapter;

    private RecyclerView mSongListView;


    private LoaderManager.LoaderCallbacks<List<Song>> mSongLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(getActivity());

            loader.setSelection(MediaStore.Audio.Media.ARTIST_ID + " = ?", new String[]{String.valueOf(mArtist.getId())});

            loader.setSortOrder(MediaStore.Audio.Media.TRACK);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            mSongListAdapter.setData(songList);
            Log.e("test", "" + mSongListAdapter.getItemCount());
        }


    };

    private AlbumListAdapter mAlbumListAdapter;
    private boolean mAlbumListLoaded = false;
    private LoaderManager.LoaderCallbacks<List<Album>> mAlbumLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Album>>() {

        @Override
        public void onLoaderReset(Loader<List<Album>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
            mAlbumListAdapter.setData(data);
            mAlbumListLoaded = true;
        }

        @Override
        public Loader<List<Album>> onCreateLoader(int id, Bundle args) {


            return new AlbumLoader(getActivity(), mArtist.getName());
        }
    };
    private ID3TagEditorDialog.OnTagsEditionSuccessListener mOnTagsEditionSuccessListener = new ID3TagEditorDialog.OnTagsEditionSuccessListener() {
        @Override
        public void onTagsEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };
    private AlbumEditorDialog.OnEditionSuccessListener mOnEditionSuccessListener = new AlbumEditorDialog.OnEditionSuccessListener() {
        @Override
        public void onEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };
    private BaseAdapter.OnItemClickListener mOnAlbumClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, final View view) {
            Album album = mAlbumListAdapter.getItem(position);

            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.album_name:
                    Fragment fragment = AlbumFragment.newInstance(album);
                    ((MainActivity) getActivity()).setFragment(fragment);
                    break;
                case R.id.menu_button:
                    showAlbumMenu(position, view);
                    break;
                /*case R.id.artist_image:
                    Toast.makeText(getContext(), "et", Toast.LENGTH_LONG).show();

                    break;*/

            }
        }
    };
    private MainActivity mActivity;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shuffle_fab:
                    if (mActivity != null) {
                        mActivity.onShuffleRequested(mSongListAdapter.mSongList, true);
                    }
                    break;
            }
        }
    };
    private int mArtistImageWidth;
    private int mArtistImageHeight;
    private int mThumbWidth;
    private int mThumbHeight;
    private int mArtworkSize;

    public ArtistFragment() {
        // Required empty public constructor
    }

    public static ArtistFragment newInstance(Artist artist) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_ARTIST_ID, artist.getId());
        args.putString(PARAM_ARTIST_NAME, artist.getName());
        args.putInt(PARAM_ALBUM_COUNT, artist.getAlbumCount());
        args.putInt(PARAM_TRACK_COUNT, artist.getTrackCount());
        fragment.setArguments(args);
        return fragment;
    }

    private void selectSong(int position) {

        if (mActivity != null) {
            mActivity.onSongSelected(mSongListAdapter.mSongList, position);
        }
    }

    public void showSongMenu(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        final Song song = mSongListAdapter.getItem(position);
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

    private void showAlbumMenu(final int position, View v) {

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.album_list_item, popup.getMenu());
        final Album album = mAlbumListAdapter.getItem(position);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_edit_tags:
                        AlbumEditorDialog dialog = AlbumEditorDialog.newInstance(album);
                        dialog.setOnEditionSuccessListener(mOnEditionSuccessListener);
                        dialog.show(getChildFragmentManager(), "edit_album_tags");
                        return true;
                    case R.id.action_add_to_playlist:
                        showPlaylistPicker(album);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showPlaylistPicker(final Album album) {
        PlaylistPicker picker = PlaylistPicker.newInstance();
        picker.setListener(new PlaylistPicker.OnPlaylistPickedListener() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                Playlists.addAlbumToPlaylist(getActivity().getContentResolver(), playlist.getId(), album.getId());
            }
        });
        picker.show(getChildFragmentManager(), "pick_playlist");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mThumbWidth = activity.getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);
        mThumbHeight = mThumbWidth;
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
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        if (args != null) {
            long id = args.getLong(PARAM_ARTIST_ID);
            String name = args.getString(PARAM_ARTIST_NAME);
            int albumCount = args.getInt(PARAM_ALBUM_COUNT);
            int trackCount = args.getInt(PARAM_TRACK_COUNT);
            mArtist = new Artist(id, name, albumCount, trackCount);
        }
        mArtistImageWidth = getResources().getDimensionPixelSize(R.dimen.artist_image_req_width);
        mArtistImageHeight = getResources().getDimensionPixelSize(R.dimen.artist_image_req_height);

        mArtworkSize = getResources().getDimensionPixelSize(R.dimen.art_size);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist, container,
                false);


        mAlbumListAdapter = new AlbumListAdapter(getActivity(), mArtworkSize, mArtworkSize);
        mAlbumListAdapter.setLayoutId(R.layout.small_album_grid_item);
        mAlbumListAdapter.setOnItemClickListener(mOnAlbumClickListener);

        mSongListView = (RecyclerView) rootView.findViewById(R.id.song_list);
        mSongListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSongListAdapter = new SongListAdapter();
        mSongListView.setAdapter(mSongListAdapter);


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        fab.setColorFilter(getActivity().getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        fab.setOnClickListener(mOnClickListener);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.artist_image);
        ArtistImageCache.getInstance().loadBitmap(mArtist.getName(), imageView, mArtistImageWidth, mArtistImageHeight);
        //imageView.setOnClickListener(mOnClickListener);

        ((GradientDrawable) rootView.findViewById(R.id.overlay).getBackground()).setGradientRadius(getResources().getDimensionPixelSize(R.dimen.gradient_radius));


        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mArtist.getName());

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mSongLoaderCallbacks);
        getLoaderManager().initLoader(1, null, mAlbumLoaderCallbacks);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mSongLoaderCallbacks);
        getLoaderManager().restartLoader(1, null, mAlbumLoaderCallbacks);

    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView vTitle;
        private final TextView vArtist;
        private final ImageView vArtwork;

        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            vArtwork = (ImageView) itemView.findViewById(R.id.artwork);
            itemView.findViewById(R.id.item_view).setOnClickListener(this);

            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);

            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();

            ThemeHelper.tintDrawable(getActivity(), drawable);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition() - 1;


            switch (v.getId()) {
                case R.id.item_view:


                    selectSong(position);
                    break;
                case R.id.menu_button:
                    showSongMenu(position, v);
                    break;
            }
        }
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {


        public RecyclerView vRecyclerView;


        public RecyclerViewHolder(View itemView) {
            super(itemView);
            vRecyclerView = (RecyclerView) itemView;
            vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            vRecyclerView.setNestedScrollingEnabled(false);

            vRecyclerView.setAdapter(mAlbumListAdapter);


        }


    }

    class SongListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private final int FIRST = 1;
        private final int NORMAL = 2;


        private List<Song> mSongList;


        public void setData(List<Song> data) {
            mSongList = data;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == FIRST) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.horizontal_recycler_view, parent, false);
                return new RecyclerViewHolder(itemView);
            }
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.song_list_item, parent, false);

            return new SongViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            if (viewType == FIRST && mAlbumListAdapter.getItemCount() == 0 && mAlbumListLoaded) {
                View view = ((RecyclerViewHolder) holder).vRecyclerView;
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.height = 0;
                view.setLayoutParams(lp);
            } else if (viewType == NORMAL) {
                Song song = getItem(position - 1);

                ((SongViewHolder) holder).vTitle.setText(song.getTitle());
                ((SongViewHolder) holder).vArtist.setText(song.getArtist());


                ImageView artworkView = ((SongViewHolder) holder).vArtwork;

                //évite de charger des images dans les mauvaises vues si elles sont recyclées
                artworkView.setTag(position);

                ArtworkCache.getInstance().loadBitmap(song.getAlbumId(), artworkView, mThumbWidth, mThumbHeight);
            }
        }

        public Song getItem(int position) {
            return mSongList == null ? null : mSongList.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? FIRST : NORMAL;
        }

        @Override
        public int getItemCount() {
            return mSongList == null ? 1 : mSongList.size() + 1;
        }
    }


}
