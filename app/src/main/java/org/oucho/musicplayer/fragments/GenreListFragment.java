package org.oucho.musicplayer.fragments;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.adapters.BaseAdapter;
import org.oucho.musicplayer.adapters.GenreListAdapter;
import org.oucho.musicplayer.model.Genre;
import org.oucho.musicplayer.utils.Permissions;
import org.oucho.musicplayer.widgets.FastScroller;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class GenreListFragment extends BaseFragment {

    private static final String[] sProjection = {MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME};

    private final List<Genre> mGenreList = new ArrayList<>();
    private GenreListAdapter mAdapter;

    private final LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mGenreList.clear();
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME);

                do {
                    long id = cursor.getLong(idCol);
                    String name = cursor.getString(nameCol);
                    mGenreList.add(new Genre(id, name));
                } while (cursor.moveToNext());

                Collections.sort(mGenreList, new Comparator<Genre>() {

                    @Override
                    public int compare(Genre lhs, Genre rhs) {
                        Collator c = Collator.getInstance(Locale.getDefault());
                        c.setStrength(Collator.PRIMARY);
                        return c.compare(lhs.getName(), rhs.getName());
                    }
                });

            }

            mAdapter.setData(mGenreList);

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (!Permissions.checkPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                return null;
            }
            Uri genreUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

            return new CursorLoader(getActivity(), genreUri,
                    sProjection, null, null, null);
        }
    };

    private final BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Genre genre = mGenreList.get(position);

            GenreFragment fragment = GenreFragment.newInstance(genre);
            fragment.showToolbar(true);
            ((MainActivity) getActivity()).setFragment(fragment);
        }
    };

    public GenreListFragment() {
        // Required empty public constructor
    }

    public static GenreListFragment newInstance() {

        return new GenreListFragment();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_genre_list, container, false);


        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new GenreListAdapter();
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        scroller.setRecyclerView(mRecyclerView);
        scroller.setSectionIndexer(mAdapter);
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
            getActivity().setTitle("Genre");
        }else  if (visible){
            getActivity().setTitle("Genre");
        }
    }

}
