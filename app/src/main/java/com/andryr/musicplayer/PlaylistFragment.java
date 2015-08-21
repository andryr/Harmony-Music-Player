package com.andryr.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link SongListFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link PlaylistFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class PlaylistFragment extends Fragment {

    private static final String PARAM_PLAYLIST_ID = "playlist_id";
    private static final String PARAM_PLAYLIST_NAME = "playlist_name";

    private static final int PICK_MUSIC = 22;

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};

    private OnSongSelectedListener mListener;

    private ArrayList<Song> mSongList = new ArrayList<>();
    private RecyclerView mRecyclerView;

    private Playlist mPlaylist;

    private SongListAdapter mAdapter;

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mSongList.clear();
            mAdapter.notifyDataSetChanged();
            int pos = 0;
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                if (idCol == -1) {
                    idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                }
                int titleCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int trackCol  = cursor
                        .getColumnIndex(MediaStore.Audio.Media.TRACK);

                do {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);

                    String artist = cursor.getString(artistCol);

                    String album = cursor.getString(albumCol);

                    long albumId = cursor.getLong(albumIdCol);

                    int track = cursor.getInt(trackCol);


                    mSongList.add(new Song(id, title, artist, album, albumId, track));
                    mAdapter.notifyItemInserted(pos);
                    pos++;
                } while (cursor.moveToNext());

            }
            mAdapter.updateSections();

        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            Uri musicUri = MediaStore.Audio.Playlists.Members.getContentUri(
                    "external", mPlaylist.getId());

            CursorLoader loader = new CursorLoader(getActivity(), musicUri,
                    sProjection, null, null,
                    MediaStore.Audio.Playlists.Members.PLAY_ORDER);

            return loader;
        }
    };

    private OnItemMovedListener mDragAndDropListener;

    private OnTouchListener mOnItemTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDragAndDropListener.startDrag((View) v.getParent());
            return false;
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);

            selectSong(position);

        }
    };

    public static PlaylistFragment newInstance(Playlist playlist) {
        PlaylistFragment fragment = new PlaylistFragment();

        Bundle args = new Bundle();

        args.putLong(PARAM_PLAYLIST_ID, playlist.getId());
        args.putString(PARAM_PLAYLIST_NAME, playlist.getName());

        fragment.setArguments(args);
        return fragment;
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
        setHasOptionsMenu(true);
        if (args != null) {

            long id = args.getLong(PARAM_PLAYLIST_ID);
            String name = args.getString(PARAM_PLAYLIST_NAME);
            mPlaylist = new Playlist(id, name);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container,
                false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mAdapter = new SongListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        ImageView dragOverlay = (ImageView) rootView
                .findViewById(R.id.drag_overlay);
        mDragAndDropListener = new OnItemMovedListener(mRecyclerView,
                dragOverlay) {

            @Override
            public void onItemMoved(int oldPosition, int newPosition) {
                mAdapter.moveItem(oldPosition, newPosition);
            }
        };
        mRecyclerView.addOnItemTouchListener(mDragAndDropListener);

        mRecyclerView.addOnItemTouchListener(new SwipeToDismissListener(
                getActivity()) {

            @Override
            public void onDismiss(int position) {
                mAdapter.removeItem(position);

            }

            @Override
            protected boolean canBeDismissed(int position) {
                return true;
            }
        });

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                startActivityForResult(
                        new Intent(getActivity(), MusicPicker.class), PICK_MUSIC);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectSong(int position) {

        if (mListener != null) {
            mListener.onSongSelected(mSongList, position);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_MUSIC && resultCode == Activity.RESULT_OK) {
            long[] ids = data.getExtras().getLongArray(MusicPicker.EXTRA_IDS);
            ContentResolver resolver = getActivity().getContentResolver();
            for (long id : ids) {
                Playlists.addToPlaylist(resolver, mPlaylist.getId(), id);
            }
            getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
        }
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        TextView vTitle;
        TextView vArtist;
        ImageButton vReorderButton;

        public SongViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            vReorderButton = (ImageButton) itemView
                    .findViewById(R.id.reorder_button);
        }

    }

    class SongListAdapter extends RecyclerView.Adapter<SongViewHolder>
            implements SectionIndexer {
        private String[] mSections = new String[10];

        public SongListAdapter() {

            updateSections();
        }

        private void updateSections() {
            List<String> sectionList = new ArrayList<>();

            String str = " ";
            for (Song s : mSongList) {
                String title = s.getTitle().trim();
                if (!title.startsWith(str)) {
                    str = title.substring(0, 1);
                    sectionList.add(str);
                }
            }
            mSections = sectionList.toArray(mSections);
        }

        @Override
        public int getItemCount() {
            return mSongList.size();
        }

        @Override
        public void onBindViewHolder(SongViewHolder viewHolder, int position) {
            Song song = mSongList.get(position);
            viewHolder.vTitle.setText(song.getTitle());
            viewHolder.vArtist.setText(song.getArtist());

        }

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.playlist_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);
            SongViewHolder viewHolder = new SongViewHolder(itemView);
            viewHolder.vReorderButton.setOnTouchListener(mOnItemTouchListener);

            return viewHolder;
        }

        @Override
        public Object[] getSections() {
            return mSections;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position < 0 || position >= mSongList.size()) {
                return 0;
            }
            String str = mSongList.get(position).getTitle().trim()
                    .substring(0, 1);
            for (int i = 0; i < mSections.length; i++) {
                String s = mSections[i];
                if (str.equals(s)) {
                    return i;
                }
            }
            return mSections.length - 1;
        }

        public void moveItem(int oldPosition, int newPosition) {
            if (oldPosition < 0 || oldPosition >= mSongList.size()
                    || newPosition < 0 || newPosition >= mSongList.size()) {
                return;
            }
            Collections.swap(mSongList, oldPosition, newPosition);
            notifyItemMoved(oldPosition, newPosition);

        }

        public void removeItem(int position) {
            Song s = mSongList.remove(position);
            Playlists.removeFromPlaylist(getActivity().getContentResolver(),
                    mPlaylist.getId(), s.getId());
            notifyItemRemoved(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSongSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
