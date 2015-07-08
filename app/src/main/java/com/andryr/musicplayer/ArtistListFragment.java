package com.andryr.musicplayer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ArtistListFragment#newInstance} factory method to create an instance
 * of this fragment.
 */
public class ArtistListFragment extends Fragment {

    private List<Artist> mArtistList = new ArrayList<>();

    private static final String[] sProjection = {BaseColumns._ID,
            ArtistColumns.ARTIST, ArtistColumns.NUMBER_OF_ALBUMS,
            ArtistColumns.NUMBER_OF_TRACKS};

    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";

    private RecyclerView mRecyclerView;

    private ArtistListAdapter mAdapter;

    private boolean mShowFastScroller = true;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mArtistList.clear();
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(BaseColumns._ID);

                int nameCol = cursor.getColumnIndex(ArtistColumns.ARTIST);

                int albumsNbCol = cursor
                        .getColumnIndex(ArtistColumns.NUMBER_OF_ALBUMS);

                int tracksNbCol = cursor
                        .getColumnIndex(ArtistColumns.NUMBER_OF_TRACKS);

                do {

                    long id = cursor.getLong(idCol);

                    String artistName = cursor.getString(nameCol);
                    if (artistName == null || artistName.equals(MediaStore.UNKNOWN_STRING)) {
                        artistName = getString(R.string.unknown_artist);
                        id = -1;
                    }


                    int albumCount = cursor.getInt(albumsNbCol);

                    int trackCount = cursor.getInt(tracksNbCol);

                    mArtistList.add(new Artist(id, artistName, albumCount,
                            trackCount));

                } while (cursor.moveToNext());

                Collections.sort(mArtistList, new Comparator<Artist>() {

                    @Override
                    public int compare(Artist lhs, Artist rhs) {
                        Collator c = Collator.getInstance(Locale.getDefault());
                        c.setStrength(Collator.PRIMARY);
                        return c.compare(lhs.getName(), rhs.getName());
                    }
                });
            }
            mAdapter.updateSections();
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            Uri musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

            CursorLoader loader = new CursorLoader(getActivity(), musicUri,
                    sProjection, null, null, null);

            return loader;
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);

            Artist artist = mArtistList.get(position);

            Fragment fragment = ArtistFragment.newInstance(artist);

            ((MainActivity) getActivity()).setFragment(fragment);

        }
    };

    public static ArtistListFragment newInstance() {
        ArtistListFragment fragment = new ArtistListFragment();

        return fragment;
    }

    public ArtistListFragment showFastScroller(boolean show) {
        mShowFastScroller = show;
        return this;
    }

    public ArtistListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list,
                container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new ArtistListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mShowFastScroller = savedInstanceState
                    .getBoolean(STATE_SHOW_FASTSCROLLER) || mShowFastScroller;
        }

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        if (mShowFastScroller) {
            scroller.setRecyclerView(mRecyclerView);
            scroller.setSectionIndexer(mAdapter);
        } else {
            scroller.setVisibility(View.GONE);
        }

        return rootView;
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder {

        TextView vName;
        TextView vAlbumCount;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.artist_name);
            vAlbumCount = (TextView) itemView.findViewById(R.id.album_count);
        }

    }

    class ArtistListAdapter extends RecyclerView.Adapter<ArtistViewHolder>
            implements SectionIndexer {

        private String[] mSections = new String[10];

        public ArtistListAdapter(Context c) {

            updateSections();
        }

        @Override
        public int getItemCount() {
            return mArtistList.size();
        }

        @Override
        public void onBindViewHolder(ArtistViewHolder viewHolder, int position) {
            Artist artist = mArtistList.get(position);
            viewHolder.vName.setText(artist.getName());
            viewHolder.vAlbumCount.setText(getActivity().getResources()
                    .getQuantityString(R.plurals.albums_count,
                            artist.getAlbumCount(), artist.getAlbumCount()));

        }

        @Override
        public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.artist_list_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);
            return new ArtistViewHolder(itemView);
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
            if (position < 0 || position >= mArtistList.size()) {
                return 0;
            }
            String str = mArtistList.get(position).getName().trim()
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
            for (Artist a : mArtistList) {
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
