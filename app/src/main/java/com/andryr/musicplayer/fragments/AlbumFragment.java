package com.andryr.musicplayer.fragments;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andryr.musicplayer.Album;
import com.andryr.musicplayer.ImageUtils;
import com.andryr.musicplayer.R;

public class AlbumFragment extends BaseFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_ARTIST = "artist";
    private static final String ARG_YEAR = "year";
    private static final String ARG_TRACK_COUNT = "track_count";

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this
     * becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;


    private Album mAlbum;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {

            long id = args.getLong(ARG_ID);
            String title = args.getString(ARG_NAME);
            String artist = args.getString(ARG_ARTIST);
            int year = args.getInt(ARG_YEAR);
            int trackCount = args.getInt(ARG_TRACK_COUNT);

            mAlbum = new Album(id,title,artist,year,trackCount);


        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container,
                false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        ImageView artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);
        ImageUtils.loadArtworkAsync(mAlbum.getId(), artworkView);


        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    @Override
    public void refresh() {
       // getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SongListFragment.newInstance(null, mAlbum).showFastScroller(false);

            }
            return null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.titles).toUpperCase(l);


            }
            return null;
        }
    }


}
