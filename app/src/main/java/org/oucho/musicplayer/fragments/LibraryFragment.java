package org.oucho.musicplayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.utils.ToolbarDrawerToggle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class LibraryFragment extends BaseFragment {


    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static LibraryFragment newInstance() {

        return new LibraryFragment();
    }

    public LibraryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter( getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

/*        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);*/

        MainActivity activity = (MainActivity) getActivity();

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        DrawerLayout drawerLayout = activity.getDrawerLayout();


        activity.setSupportActionBar(toolbar);

        ToolbarDrawerToggle drawerToggle = new ToolbarDrawerToggle(activity,drawerLayout,toolbar, new int[]{Gravity.START});
        drawerLayout.addDrawerListener(drawerToggle);
        return rootView;
    }

    @Override
    public void load() {
        int fragmentCount = mSectionsPagerAdapter.getCount();
        for(int pos = 0; pos < fragmentCount; pos++)
        {
            BaseFragment fragment = (BaseFragment) mSectionsPagerAdapter.getFragment(pos);
            if(fragment != null)
            {
                Log.d("frag1", fragment.getClass().getCanonicalName());

                fragment.load();
            }
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final Map<Integer, String> mFragmentTags;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentTags = new HashMap<>();

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return AlbumListFragment.newInstance();
                case 1:
                    return SongListFragment.newInstance();
                case 2:
                    return ArtistListFragment.newInstance();
                case 3:
                    return GenreListFragment.newInstance();
                case 4:
                    return PlaylistListFragment.newInstance();
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
                Log.d("fragtag", tag);

            }
            return obj;
        }

        public Fragment getFragment(int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null)
                return null;
            return getChildFragmentManager().findFragmentByTag(tag);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.albums).toUpperCase(l);
                case 1:
                    return getString(R.string.titles).toUpperCase(l);
                case 2:
                    return getString(R.string.artists).toUpperCase(l);
                case 3:
                    return getString(R.string.genres).toUpperCase(l);
                case 4:
                    return getString(R.string.playlists).toUpperCase(l);

            }
            return null;
        }
    }

}
