package com.andryr.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.widgets.FastScroller;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.ArtistLoader;

import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link ArtistListFragment#newInstance} factory method to create an instance
 * of this fragment.
 */
public class ArtistListFragment extends BaseFragment {




    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";

    private RecyclerView mRecyclerView;

    private ArtistListAdapter mAdapter;

    private boolean mShowFastScroller = true;

    private LoaderManager.LoaderCallbacks<List<Artist>> mLoaderCallbacks = new LoaderCallbacks<List<Artist>>() {

        @Override
        public void onLoaderReset(Loader<List<Artist>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
            mAdapter.setData(data);

        }

        @Override
        public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {


            return new ArtistLoader(getActivity());
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

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    class ArtistViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView vName;
        TextView vAlbumCount;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.artist_name);
            vAlbumCount = (TextView) itemView.findViewById(R.id.album_count);
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            Artist artist = mAdapter.getItem(position);

            Fragment fragment = ArtistFragment.newInstance(artist);

            ((MainActivity) getActivity()).setFragment(fragment);
        }
    }

    class ArtistListAdapter extends RecyclerView.Adapter<ArtistViewHolder>
            implements FastScroller.SectionIndexer {


        private List<Artist> mArtistList;


        public ArtistListAdapter(Context c) {

        }

        @Override
        public int getItemCount() {
            return mArtistList==null?0:mArtistList.size();
        }

        public Artist getItem(int position)
        {
            return mArtistList==null?null:mArtistList.get(position);
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
            return new ArtistViewHolder(itemView);
        }



        public void setData(List<Artist> data) {
            mArtistList = data;
            notifyDataSetChanged();

        }

        @Override
        public String getSectionForPosition(int position) {
            return getItem(position).getName().substring(0, 1);
        }
    }

}
