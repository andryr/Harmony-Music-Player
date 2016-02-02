/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.fragments;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.utils.ToolbarDrawerToggle;

/**
 * A simple {@link Fragment} subclass. Use the {@link LibraryFragment#newInstance}
 * factory method to create an instance of this fragment.
 */
public class LibraryFragment extends BaseFragment {

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

    public static LibraryFragment newInstance() {
        LibraryFragment fragment = new LibraryFragment();

        return fragment;
    }

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container,
                false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);


        MainActivity activity = (MainActivity) getActivity();

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        DrawerLayout drawerLayout = activity.getDrawerLayout();


        activity.setSupportActionBar(toolbar);

        ToolbarDrawerToggle drawerToggle = new ToolbarDrawerToggle(activity,drawerLayout,toolbar, new int[]{Gravity.START});
        drawerLayout.setDrawerListener(drawerToggle);
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Map<Integer, String> mFragmentTags;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentTags = new HashMap<Integer, String>();

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SongListFragment.newInstance();
                case 1:
                    return AlbumListFragment.newInstance(null);
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
                    return getString(R.string.titles).toUpperCase(l);
                case 1:
                    return getString(R.string.albums).toUpperCase(l);
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
