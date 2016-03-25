package org.oucho.musicplayer.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.oucho.musicplayer.R;
import org.oucho.musicplayer.model.Song;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

//TODO utiliser des Loaders, selon le modèle de SearchActivity
public class MusicPicker extends BaseActivity {

    public static final String EXTRA_IDS = "ids";
    private static final String[] sProjection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.TRACK};

    private final OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildLayoutPosition(v);
            mAdapter.toggleSelection(position);

        }
    };

    private ActionMode mActionMode;

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.music_picker_context, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_submit:
                    ArrayList<Song> songList = new ArrayList<>();
                    SparseBooleanArray positions = mAdapter.getSelectedPositions();
                    int count = positions.size();
                    for (int i = 0; i < count; i++) {
                        int key = positions.keyAt(i);
                        if (positions.get(key)) {
                            songList.add(mSongList.get(key));
                        }
                    }

                    Intent data = new Intent();
                    long[] ids = new long[songList.size()];
                    for (int i = 0; i < songList.size(); i++) {
                        ids[i] = songList.get(i).getId();
                    }

                    data.putExtra(EXTRA_IDS, ids);

                    setResult(RESULT_OK, data);
                    finish();
                    return true;
                case R.id.action_cancel:
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    private final List<Song> mSongList = new ArrayList<>();
    private MusicPickerAdapter mAdapter;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_picker);
        getSongList();

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
        mAdapter = new MusicPickerAdapter(mSongList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setAdapter(mAdapter);
    }

    private void getSongList() {
        ContentResolver resolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = resolver.query(musicUri, sProjection, null, null, null);

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
            int trackCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TRACK);

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);

                String artist = cursor.getString(artistCol);

                String album = cursor.getString(albumCol);

                long albumId = cursor.getLong(albumIdCol);

                int track = cursor.getInt(trackCol);


                mSongList.add(new Song(id, title, artist, album, albumId, track));
            } while (cursor.moveToNext());

            Collections.sort(mSongList, new Comparator<Song>() {

                @Override
                public int compare(Song lhs, Song rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getTitle(), rhs.getTitle());
                }
            });
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.music_picker, menu);
        SearchView search = (SearchView) MenuItemCompat.getActionView(menu
                .findItem(R.id.action_search));

        search.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return true;
            }

        });
        search.setOnCloseListener(new OnCloseListener() {

            @Override
            public boolean onClose() {
                mAdapter.getFilter().filter("");
                return false;
            }
        });
        return true;
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        final TextView vTitle;
        final TextView vArtist;

        private final View mItemView;

        public SongViewHolder(View itemView) {
            super(itemView);

            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);

            mItemView = itemView;
        }

        public void setSelected(boolean selected) {
            mItemView.setSelected(selected);
        }
    }

    class MusicPickerAdapter extends RecyclerView.Adapter<SongViewHolder>
            implements Filterable {
        private final List<Song> mSongList;
        private final List<Song> mVisibleSongs = new ArrayList<>();
        private final SparseBooleanArray mSelectedPositions = new SparseBooleanArray();

        private final Filter mFilter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                mVisibleSongs.clear();
                mVisibleSongs.addAll((List<Song>) results.values);
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Song> filteredList = new ArrayList<>();

                if (constraint == null || constraint == "") {
                    filteredList.addAll(mSongList);
                } else {

                    Locale l = Locale.getDefault();
                    constraint = constraint.toString().trim().toLowerCase(l);
                    for (Song s : mSongList) {
                        if (s.getTitle().toLowerCase(l).contains(constraint)
                                || s.getArtist().toLowerCase(l)
                                .contains(constraint)
                                || s.getAlbum().toLowerCase(l)
                                .contains(constraint)) {
                            filteredList.add(s);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }
        };

        public MusicPickerAdapter(List<Song> songList) {

            mSongList = songList;
            mVisibleSongs.addAll(mSongList);
        }

        @Override
        public int getItemCount() {
            return mVisibleSongs.size();
        }

        @Override
        public void onBindViewHolder(SongViewHolder viewHolder, int position) {
            Song song = mVisibleSongs.get(position);

            viewHolder.vTitle.setText(song.getTitle());
            viewHolder.vArtist.setText(song.getArtist());

            position = mSongList.indexOf(song);

            viewHolder.setSelected(mSelectedPositions.get(position, false));
        }

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.music_picker_list_item, parent, false);
            itemView.setOnClickListener(mOnClickListener);

            return new SongViewHolder(itemView);
        }

        public void setSelected(int position, boolean selected) {
            Song s = mVisibleSongs.get(position);
            int index = mSongList.indexOf(s);
            if (index != -1) {
                if (selected) {
                    mSelectedPositions.append(index, true);

                } else {
                    mSelectedPositions.delete(index);

                }

                if (mActionMode == null && mSelectedPositions.size() > 0) {
                    mActionMode = startSupportActionMode(mActionModeCallback);
                } else if (mActionMode != null && mSelectedPositions.size() == 0) {
                    mActionMode.finish();
                }

                notifyItemChanged(position);
            }
        }

        public void toggleSelection(int position) {
            Song s = mVisibleSongs.get(position);
            int index = mSongList.indexOf(s);
            boolean selected = mSelectedPositions.get(index, false);
            setSelected(position, !selected);
        }

        public SparseBooleanArray getSelectedPositions() {
            return mSelectedPositions;
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }
    }
}
