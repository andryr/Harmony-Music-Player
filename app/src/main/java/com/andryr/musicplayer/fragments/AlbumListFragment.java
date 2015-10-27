package com.andryr.musicplayer.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.fragments.dialog.AlbumEditorDialog;
import com.andryr.musicplayer.utils.ArtworkHelper;
import com.andryr.musicplayer.widgets.FastScroller;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.utils.Playlists;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.AlbumLoader;
import com.andryr.musicplayer.utils.ThemeHelper;

import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link AlbumListFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class AlbumListFragment extends BaseFragment {

    private static final String PARAM_ARTIST = "artist";
    private static final String PARAM_ARTIST_ALBUM = "artist_album";


    private AlbumListAdapter mAdapter;

    private boolean mArtistAlbum = false;
    private String mArtist;

    private RecyclerView mRecyclerView;

    private LoaderManager.LoaderCallbacks<List<Album>> mLoaderCallbacks = new LoaderCallbacks<List<Album>>() {

        @Override
        public void onLoaderReset(Loader<List<Album>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
            mAdapter.setData(data);

        }

        @Override
        public Loader<List<Album>> onCreateLoader(int id, Bundle args) {


            return new AlbumLoader(getActivity(), mArtist);
        }
    };


    private AlbumEditorDialog.OnEditionSuccessListener mOnEditionSuccessListener = new AlbumEditorDialog.OnEditionSuccessListener() {
        @Override
        public void onEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };


    public AlbumListFragment() {
        // Required empty public constructor
    }

    public static AlbumListFragment newInstance(Artist artist) {
        AlbumListFragment fragment = new AlbumListFragment();
        if (artist != null) {
            Bundle args = new Bundle();
            args.putString(PARAM_ARTIST, artist.getName());
            args.putBoolean(PARAM_ARTIST_ALBUM, true);
            fragment.setArguments(args);
        }

        return fragment;
    }

    private void showMenu(final int position, View v) {

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.album_list_item, popup.getMenu());
        final Album album = mAdapter.getItem(position);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_edit_tags:
                        showEditorDialog(album);
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

    private void showEditorDialog(Album album) {
        AlbumEditorDialog dialog = AlbumEditorDialog.newInstance(album);
        dialog.setOnEditionSuccessListener(mOnEditionSuccessListener);
        dialog.show(getChildFragmentManager(), "edit_album_tags");
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mArtistAlbum = args.getBoolean(PARAM_ARTIST_ALBUM);
            if (mArtistAlbum) {
                mArtist = args.getString(PARAM_ARTIST);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_list,
                container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Resources res = getActivity().getResources();
        float screenWidth = display.getWidth();
        float itemWidth = res.getDimension(R.dimen.album_grid_item_width);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Math.round(screenWidth / itemWidth)));
        mAdapter = new AlbumListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        FastScroller scroller = (FastScroller) rootView.findViewById(R.id.fastscroller);
        scroller.setSectionIndexer(mAdapter);
        scroller.setRecyclerView(mRecyclerView);

        return rootView;
    }

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        ImageView vArtwork;
        TextView vName;
        TextView vArtist;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            vArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
            vName = (TextView) itemView.findViewById(R.id.album_name);
            vArtist = (TextView) itemView.findViewById(R.id.artist_name);
            vArtwork.setOnClickListener(this);
            itemView.findViewById(R.id.album_info).setOnClickListener(this);
            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);

            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();
            ThemeHelper.tintDrawable(getActivity(), drawable);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Album album = mAdapter.getItem(position);

            switch (v.getId()) {
                case R.id.album_artwork:
                case R.id.album_info:
                    Log.d("album", "album id " + album.getId() + " " + album.getAlbumName());
                    Fragment fragment = AlbumFragment.newInstance(album);
                    ((MainActivity) getActivity()).setFragment(fragment);
                    break;
                case R.id.menu_button:
                    showMenu(position, v);
                    break;

            }
        }
    }

    class AlbumListAdapter extends RecyclerView.Adapter<AlbumViewHolder>
            implements FastScroller.SectionIndexer {


        private List<Album> mAlbumList;

        public AlbumListAdapter(Context c) {
            super();
        }

        public void setData(List<Album> data) {
            mAlbumList = data;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mAlbumList == null ? 0 : mAlbumList.size();
        }

        public Album getItem(int position) {
            return mAlbumList == null ? null : mAlbumList.get(position);
        }

        @Override
        public void onBindViewHolder(AlbumViewHolder viewHolder, int position) {
            Album album = mAlbumList.get(position);
            viewHolder.vName.setText(album.getAlbumName());
            viewHolder.vArtist.setText(album.getArtistName());


            ArtworkHelper.loadArtworkAsync(album.getId(), viewHolder.vArtwork);


        }

        @Override
        public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.album_grid_item, parent, false);


            return new AlbumViewHolder(itemView);
        }


        @Override
        public String getSectionForPosition(int position) {
            return getItem(position).getAlbumName().substring(0, 1);
        }
    }

}
