package com.andryr.musicplayer.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
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
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.Artist;
import com.andryr.musicplayer.FastScroller;
import com.andryr.musicplayer.ImageUtils;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.AlbumLoader;

import org.w3c.dom.Text;

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



            return new AlbumLoader(getActivity(),mArtist);
        }
    };



    private AlbumEditorDialog.OnEditionSuccessListener mOnEditionSuccessListener = new AlbumEditorDialog.OnEditionSuccessListener() {
        @Override
        public void onEditionSuccess() {
            ((MainActivity)getActivity()).refresh();
        }
    };


    private void showMenu(final int position, View v) {

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.album_list_item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_edit_tags:
                        AlbumEditorDialog dialog = AlbumEditorDialog.newInstance(mAdapter.getItem(position));
                        dialog.setOnEditionSuccessListener(mOnEditionSuccessListener);
                        dialog.show(getChildFragmentManager(), "edit_album_tags");
                        return true;
                }
                return false;
            }
        });
        popup.show();
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

    public AlbumListFragment() {
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
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
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
            drawable.setColorFilter(getActivity().getResources().getColor(R.color.primary_text), PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Album album = mAdapter.getItem(position);

            switch(v.getId())
            {
                case R.id.album_artwork:
                case R.id.album_info:
                    Log.d("album", "album id " + album.getId() + " " + album.getAlbumName());
                    Fragment fragment = AlbumFragment.newInstance(album);
                    ((MainActivity) getActivity()).setFragment(fragment);
                    break;
                case R.id.menu_button:
                    showMenu(position,v);
                    break;

            }
        }
    }

    class AlbumListAdapter extends RecyclerView.Adapter<AlbumViewHolder>
            implements FastScroller.SectionIndexer {

        private Drawable mDefaultArtwork = null;


        private List<Album> mAlbumList;

        public AlbumListAdapter(Context c) {
            super();
            mDefaultArtwork = ImageUtils.getDefaultArtwork(c);
        }

        public void setData(List<Album> data) {
            mAlbumList = data;
            notifyDataSetChanged();
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
            viewHolder.vArtist.setText(album.getArtistName());
            if (mDefaultArtwork != null) {
                viewHolder.vArtwork.setImageDrawable(mDefaultArtwork);
            }

            ImageUtils.loadArtworkAsync(album.getId(), viewHolder.vArtwork);

        }

        @Override
        public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.album_grid_item, parent, false);




            return new AlbumViewHolder(itemView);
        }


        @Override
        public String getSectionForPosition(int position) {
            return getItem(position).getAlbumName().substring(0,1);
        }
    }

}
