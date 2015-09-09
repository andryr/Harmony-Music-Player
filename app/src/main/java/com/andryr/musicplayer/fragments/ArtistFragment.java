package com.andryr.musicplayer.fragments;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.Artist;
import com.andryr.musicplayer.FastScroller;
import com.andryr.musicplayer.FragmentListener;
import com.andryr.musicplayer.ImageUtils;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.Song;
import com.andryr.musicplayer.loaders.AlbumLoader;
import com.andryr.musicplayer.loaders.SongLoader;

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
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            mSongListAdapter.setData(songList);
            Log.e("test", "" + mSongListAdapter.getItemCount());
        }

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(getActivity());
            loader.setSongListType(SongLoader.ARTIST_SONGS);
            loader.setArtistId(mArtist.getId());
            loader.setOrder(MediaStore.Audio.Media.TRACK);
            return loader;
        }
    };

    private AlbumListAdapter mAlbumListAdapter;


    private LoaderManager.LoaderCallbacks<List<Album>> mAlbumLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Album>>() {

        @Override
        public void onLoaderReset(Loader<List<Album>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
            mAlbumListAdapter.setData(data);

        }

        @Override
        public Loader<List<Album>> onCreateLoader(int id, Bundle args) {



            return new AlbumLoader(getActivity(),mArtist.getName());
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
            ((MainActivity)getActivity()).refresh();
        }
    };

    private FragmentListener mListener;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId())
            {
                case R.id.shuffle_fab:
                    if (mListener != null) {
                        mListener.onShuffleRequested(mSongListAdapter.mSongList,true);
                    }
                    break;
            }
        }
    };

    private void selectSong(int position) {

        if (mListener != null) {
            mListener.onSongSelected(mSongListAdapter.mSongList, position);
        }
    }


    public void showSongMenu(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.song_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_to_queue:
                        ((MainActivity) getActivity()).addToQueue(mSongListAdapter.getItem(position));
                        return true;
                    case R.id.action_set_as_next_track:
                        ((MainActivity) getActivity()).setAsNextTrack(mSongListAdapter.getItem(position));
                        return true;
                    case R.id.action_edit_tags:
                        ID3TagEditorDialog dialog = ID3TagEditorDialog.newInstance(mSongListAdapter.getItem(position));
                        dialog.setOnTagsEditionSuccessListener(mOnTagsEditionSuccessListener);
                        dialog.show(getChildFragmentManager(), "edit_tags");
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showAlbumMenu(final int position, View v) {

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.album_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_edit_tags:
                        AlbumEditorDialog dialog = AlbumEditorDialog.newInstance(mAlbumListAdapter.getItem(position));
                        dialog.setOnEditionSuccessListener(mOnEditionSuccessListener);
                        dialog.show(getChildFragmentManager(), "edit_album_tags");
                        return true;
                }
                return false;
            }
        });
        popup.show();
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

    public ArtistFragment() {
        // Required empty public constructor
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
        getLoaderManager().initLoader(0, null, mSongLoaderCallbacks);
        getLoaderManager().initLoader(1, null, mAlbumLoaderCallbacks);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            long id = args.getLong(PARAM_ARTIST_ID);
            String name = args.getString(PARAM_ARTIST_NAME);
            int albumCount = args.getInt(PARAM_ALBUM_COUNT);
            int trackCount = args.getInt(PARAM_TRACK_COUNT);
            mArtist = new Artist(id, name, albumCount, trackCount);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist, container,
                false);


        mAlbumListAdapter = new AlbumListAdapter();

        mSongListView = (RecyclerView) rootView.findViewById(R.id.song_list);
        mSongListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSongListAdapter = new SongListAdapter();
        mSongListView.setAdapter(mSongListAdapter);



        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        fab.setColorFilter(getActivity().getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        fab.setOnClickListener(mOnClickListener);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.artist_image);
        ImageUtils.loadArtistImageAsync(mArtist.getName(), imageView);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mArtist.getName());

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    @Override
    public void refresh() {
       getLoaderManager().restartLoader(0, null, mSongLoaderCallbacks);
       getLoaderManager().restartLoader(1, null, mAlbumLoaderCallbacks);

    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
            drawable.setColorFilter(getActivity().getResources().getColor(R.color.primary_text), PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition()-1;


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
            vRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
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

        public Song getItem(int position) {
            return mSongList == null ? null : mSongList.get(position-1);
        }


        @Override
        public int getItemViewType(int position) {
            return position==0?FIRST:NORMAL;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == FIRST)
            {
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
            if(getItemViewType(position)==FIRST)
            {
                return;
            }
            Song song = getItem(position);

            ((SongViewHolder)holder).vTitle.setText(song.getTitle());
            ((SongViewHolder)holder).vArtist.setText(song.getArtist());
        }

        @Override
        public int getItemCount() {
            return mSongList == null?1:mSongList.size()+1;
        }
    }
    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView vArtwork;
        TextView vName;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            vArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
            vName = (TextView) itemView.findViewById(R.id.album_name);
            vArtwork.setOnClickListener(this);
            vName.setOnClickListener(this);
            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            menuButton.setOnClickListener(this);

            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();
            drawable.setColorFilter(getActivity().getResources().getColor(R.color.primary_text), PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Album album = mAlbumListAdapter.getItem(position);

            switch(v.getId())
            {
                case R.id.album_artwork:
                case R.id.album_name:
                    Log.d("album", "album id " + album.getId() + " " + album.getAlbumName());
                    Fragment fragment = AlbumFragment.newInstance(album);
                    ((MainActivity) getActivity()).setFragment(fragment);
                    break;
                case R.id.menu_button:
                    showAlbumMenu(position, v);
                    break;

            }
        }
    }

    class AlbumListAdapter extends RecyclerView.Adapter<AlbumViewHolder>
            implements FastScroller.SectionIndexer {

        private Drawable mDefaultArtwork = null;


        private List<Album> mAlbumList;

        public AlbumListAdapter() {
            super();
            mDefaultArtwork = ImageUtils.getDefaultArtwork(getActivity());
        }

        public void setData(List<Album> data) {
            mAlbumList = data;
            notifyDataSetChanged();
            if(mSongListAdapter != null)
            {
                mSongListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return mAlbumList==null?0:mAlbumList.size();
        }

        public Album getItem(int position)
        {
            return mAlbumList==null?null:mAlbumList.get(position);
        }

        @Override
        public void onBindViewHolder(AlbumViewHolder viewHolder, int position) {
            Album album = mAlbumList.get(position);
            viewHolder.vName.setText(album.getAlbumName());
            if (mDefaultArtwork != null) {
                viewHolder.vArtwork.setImageDrawable(mDefaultArtwork);
            }

            ImageUtils.loadArtworkAsync(album.getId(), viewHolder.vArtwork);

        }

        @Override
        public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.small_album_grid_item, parent, false);




            return new AlbumViewHolder(itemView);
        }


        @Override
        public String getSectionForPosition(int position) {
            return getItem(position).getAlbumName().substring(0,1);
        }
    }


}
