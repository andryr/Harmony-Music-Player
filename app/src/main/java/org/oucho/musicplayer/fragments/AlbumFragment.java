package org.oucho.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.TextView;

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.adapters.BaseAdapter;
import org.oucho.musicplayer.adapters.SongAlbumListAdapter;
import org.oucho.musicplayer.fragments.dialog.ID3TagEditorDialog;
import org.oucho.musicplayer.fragments.dialog.PlaylistPicker;
import org.oucho.musicplayer.images.ArtworkCache;
import org.oucho.musicplayer.loaders.SongLoader;
import org.oucho.musicplayer.model.Album;
import org.oucho.musicplayer.model.Playlist;
import org.oucho.musicplayer.model.Song;
import org.oucho.musicplayer.utils.Playlists;

import java.util.List;

public class AlbumFragment extends BaseFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_ARTIST = "artist";
    private static final String ARG_YEAR = "year";
    private static final String ARG_TRACK_COUNT = "track_count";


    private Album mAlbum;

    private SongAlbumListAdapter mAdapter;


    private final LoaderManager.LoaderCallbacks<List<Song>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(getActivity());

            loader.setSelection(MediaStore.Audio.Media.ALBUM_ID + " = ?", new String[]{String.valueOf(mAlbum.getId())});
            loader.setSortOrder(MediaStore.Audio.Media.TRACK);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            mAdapter.setData(songList);
            Log.e("test", "" + mAdapter.getItemCount());
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub
        }
    };


    private final ID3TagEditorDialog.OnTagsEditionSuccessListener mOnTagsEditionSuccessListener = new ID3TagEditorDialog.OnTagsEditionSuccessListener() {
        @Override
        public void onTagsEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };

    private MainActivity mActivity;
/*    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
*//*                case R.id.shuffle_fab:
                    if (mActivity != null) {
                        mActivity.onShuffleRequested(mAdapter.getSongList(), true);
                    }
                    break;*//*
            }
        }
    };*/

    private final BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
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
    private int mArtworkWidth;
    private int mArtworkHeight;

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

        if (mActivity != null) {
            mActivity.onSongSelected(mAdapter.getSongList(), position);
        }
    }

    private void showMenu(final int position, View v) {
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
/*                    case R.id.action_set_as_next_track:
                        ((MainActivity) getActivity()).setAsNextTrack(song);
                        return true;*/
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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActivity = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private String Titre = "";
    private String Artiste = "";
    private String Année = "";
    private String nb_Morceaux = "";

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

            Titre = title;
            Artiste = artist;
            Année = String.valueOf(year);

            if (trackCount < 2) {
                nb_Morceaux = String.valueOf(trackCount) + " morceau";
            } else {
                nb_Morceaux = String.valueOf(trackCount) + " morceaux";
            }
        }

        mArtworkWidth = getResources().getDimensionPixelSize(R.dimen.artist_image_req_width);
        mArtworkHeight = getResources().getDimensionPixelSize(R.dimen.artist_image_req_height);

        getActivity().setTitle(Titre);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        TextView titreAlbum = (TextView) rootView.findViewById(R.id.line1);
        titreAlbum.setText(Titre);

        TextView artiste = (TextView) rootView.findViewById(R.id.line2);
        artiste.setText(Artiste);

        TextView an = (TextView) rootView.findViewById(R.id.line3);
        an.setText(Année);

        TextView morceaux = (TextView) rootView.findViewById(R.id.line4);
        morceaux.setText(nb_Morceaux);


        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.song_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new SongAlbumListAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        mRecyclerView.setAdapter(mAdapter);

        ImageView artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);

        ArtworkCache.getInstance().loadBitmap(mAlbum.getId(), artworkView, mArtworkWidth, mArtworkHeight);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);

    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

}
