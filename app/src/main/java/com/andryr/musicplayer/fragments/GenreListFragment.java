package com.andryr.musicplayer.fragments;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.TextView;

import com.andryr.musicplayer.FastScroller;
import com.andryr.musicplayer.Genre;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.R;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link SongListFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link GenreListFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class GenreListFragment extends BaseFragment {

    private static final String[] sProjection = {MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME};

    private RecyclerView mRecyclerView;

    private List<Genre> mGenreList = new ArrayList<>();
    private GenreListAdapter mAdapter;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

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

            mAdapter.notifyDataSetChanged();

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            Uri genreUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

            CursorLoader loader = new CursorLoader(getActivity(), genreUri,
                    sProjection, null, null, null);

            return loader;
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);

            Genre genre = mGenreList.get(position);

            SongListFragment fragment = SongListFragment.newInstance(genre);
            fragment.showToolbar(true);
            ((MainActivity) getActivity()).setFragment(fragment);

        }
    };

    public static GenreListFragment newInstance() {
        GenreListFragment fragment = new GenreListFragment();

        return fragment;
    }

    public GenreListFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_genre_list,
                container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new GenreListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        FastScroller scroller = (FastScroller) rootView
                .findViewById(R.id.fastscroller);
        scroller.setRecyclerView(mRecyclerView);
        scroller.setSectionIndexer(mAdapter);
        return rootView;
    }

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }


    class GenreViewHolder extends RecyclerView.ViewHolder {

        TextView vName;
        TextView vArtist;

        public GenreViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.name);
        }

    }

    class GenreListAdapter extends RecyclerView.Adapter<GenreViewHolder>
            implements FastScroller.SectionIndexer {

        public GenreListAdapter() {

            List<String> sectionList = new ArrayList<>();


        }

        @Override
        public int getItemCount() {
            return mGenreList.size();
        }

        @Override
        public void onBindViewHolder(GenreViewHolder viewHolder, int position) {
            Genre genre = mGenreList.get(position);
            viewHolder.vName.setText(genre.getName());

        }

        @Override
        public GenreViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.genre_list_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);
            return new GenreViewHolder(itemView);
        }


        @Override
        public String getSectionForPosition(int position) {
            return mGenreList.get(position).getName().substring(0,1);

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
