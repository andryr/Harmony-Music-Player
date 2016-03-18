package org.oucho.musicplayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.adapters.ArtistListAdapter;
import org.oucho.musicplayer.adapters.BaseAdapter;
import org.oucho.musicplayer.loaders.ArtistLoader;
import org.oucho.musicplayer.model.Artist;
import org.oucho.musicplayer.widgets.FastScroller;

import java.util.List;


public class ArtistListFragment extends BaseFragment {


    private static final String STATE_SHOW_FASTSCROLLER = "fastscroller";

    private ArtistListAdapter mAdapter;

    private boolean mShowFastScroller = true;

    private final LoaderManager.LoaderCallbacks<List<Artist>> mLoaderCallbacks = new LoaderCallbacks<List<Artist>>() {

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


    private final BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Artist artist = mAdapter.getItem(position);

            Fragment fragment = ArtistFragment.newInstance(artist);

            ((MainActivity) getActivity()).setFragment(fragment);
        }
    };


    public ArtistListFragment() {
        // Required empty public constructor
    }

    public static ArtistListFragment newInstance() {

        return new ArtistListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);


        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mAdapter = new ArtistListAdapter(getActivity());
        mAdapter.setOnItemClickListener(mOnItemClickListener);
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
    public void load() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    @Override
    public void setUserVisibleHint(boolean visible){
        super.setUserVisibleHint(visible);
        if (visible && isResumed()){
            getActivity().setTitle("Artistes");
        }else  if (visible){
            getActivity().setTitle("Artistes");
        }
    }
}
