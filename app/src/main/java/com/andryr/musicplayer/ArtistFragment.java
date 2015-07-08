package com.andryr.musicplayer;

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

public class ArtistFragment extends Fragment {

    private static final String PARAM_ARTIST_ID = "artist_id";
    private static final String PARAM_ARTIST_NAME = "artist_name";
    private static final String PARAM_ALBUM_COUNT = "track_count";
    private static final String PARAM_TRACK_COUNT = "track_count";

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

    private Artist mArtist;

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

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.artist_image);
        ImageUtils.loadArtistImageAsync(mArtist.getName(), imageView);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
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
                    return AlbumListFragment.newInstance(mArtist);
                case 1:
                    return SongListFragment.newInstance(mArtist, null).showFastScroller(false);

            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.albums).toUpperCase(l);
                case 1:
                    return getString(R.string.titles).toUpperCase(l);

            }
            return null;
        }
    }

}
