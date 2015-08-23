package com.andryr.musicplayer.fragments;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.Artist;
import com.andryr.musicplayer.FastScroller;
import com.andryr.musicplayer.ImageUtils;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.AlbumLoader;

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

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);

            Album album = mAdapter.getItem(position);
            Log.d("album","album id "+album.getId()+" "+album.getName());
            Fragment fragment = AlbumFragment.newInstance(album);
            ((MainActivity) getActivity()).setFragment(fragment);

        }
    };

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

    class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView vArtwork;
        TextView vName;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            vArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
            vName = (TextView) itemView.findViewById(R.id.album_name);
        }

    }

    class AlbumListAdapter extends RecyclerView.Adapter<AlbumViewHolder>
            implements SectionIndexer {

        private Drawable mDefaultArtwork = null;

        private String[] mSections = new String[10];

        private List<Album> mAlbumList;

        public AlbumListAdapter(Context c) {
            super();
            mDefaultArtwork = ImageUtils.getDefaultArtwork(c);
        }

        public void setData(List<Album> data) {
            mAlbumList = data;
            updateSections();
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
            viewHolder.vName.setText(album.getName());
            if (mDefaultArtwork != null) {
                viewHolder.vArtwork.setImageDrawable(mDefaultArtwork);
            }

            ImageUtils.loadArtworkAsync(album.getId(), viewHolder.vArtwork);

        }

        @Override
        public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.album_list_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);
            return new AlbumViewHolder(itemView);
        }

        @Override
        public Object[] getSections() {
            return mSections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            if(mAlbumList==null)
            {
                return -1;
            }
            if (position < 0 || position >= mAlbumList.size()) {
                return 0;
            }
            String str = mAlbumList.get(position).getName().trim()
                    .substring(0, 1);
            for (int i = 0; i < mSections.length; i++) {
                String s = mSections[i];
                if (str.equals(s)) {
                    return i;
                }
            }
            return mSections.length - 1;
        }

        private void updateSections() {
            ArrayList<String> sectionList = new ArrayList<>();
            String str = " ";
            for (Album a : mAlbumList) {
                String title = a.getName().trim();
                if (!title.startsWith(str) && title.length() >= 1) {
                    str = title.substring(0, 1);
                    sectionList.add(str);
                }
            }
            mSections = sectionList.toArray(mSections);
        }
    }

}
