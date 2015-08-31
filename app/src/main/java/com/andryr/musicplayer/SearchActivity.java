package com.andryr.musicplayer;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.loaders.AlbumLoader;
import com.andryr.musicplayer.loaders.ArtistLoader;
import com.andryr.musicplayer.loaders.SongLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends ActionBarActivity {

    public static final String FILTER = "filter";


    private LoaderManager.LoaderCallbacks<List<Album>> mAlbumLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Album>>() {


        @Override
        public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
            mAdapter.setAlbumList(data);

        }

        @Override
        public void onLoaderReset(Loader<List<Album>> loader) {

        }

        @Override
        public Loader<List<Album>> onCreateLoader(int id, Bundle args) {



            return new AlbumLoader(SearchActivity.this,null);
        }
    };
    private LoaderManager.LoaderCallbacks<List<Artist>> mArtistLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Artist>>() {

        @Override
        public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
            mAdapter.setArtistList(data);

        }

        @Override
        public void onLoaderReset(Loader<List<Artist>> loader) {

        }

        @Override
        public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {


            return new ArtistLoader(SearchActivity.this);
        }
    };

    private LoaderManager.LoaderCallbacks<List<Song>> mSongLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<Song>>() {
        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> songList) {
            mAdapter.setSongList(songList);
            Log.e("test", "" + mAdapter.getItemCount());
        }

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            SongLoader loader = new SongLoader(SearchActivity.this, SongLoader.ALL_SONGS, 0, 0, 0);
            if(args != null)
            {
                String filter = args.getString(FILTER);
                loader.setFilter(filter);
            }
            return loader;
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);

        }
    };


    private SearchAdapter mAdapter;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mRecyclerView = (RecyclerView) findViewById(R.id.list_view);
        mAdapter = new SearchAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mAdapter);


        getSupportLoaderManager().initLoader(0, null, mAlbumLoaderCallbacks);
        getSupportLoaderManager().initLoader(1, null, mArtistLoaderCallbacks);
        getSupportLoaderManager().initLoader(2, null, mSongLoaderCallbacks);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView search = (SearchView) MenuItemCompat.getActionView(menu
                .findItem(R.id.action_search));

        search.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                Bundle args = new Bundle();
                args.putString(FILTER,newText);
                getSupportLoaderManager().restartLoader(0, args, mAlbumLoaderCallbacks);
                getSupportLoaderManager().restartLoader(1, args, mArtistLoaderCallbacks);
                getSupportLoaderManager().restartLoader(2, args, mSongLoaderCallbacks);

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
                getSupportLoaderManager().restartLoader(0, null, mAlbumLoaderCallbacks);
                getSupportLoaderManager().restartLoader(1, null, mArtistLoaderCallbacks);
                getSupportLoaderManager().restartLoader(2, null, mSongLoaderCallbacks);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        ImageView vArtwork;
        TextView vName;
        TextView vArtist;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            vArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
            vName = (TextView) itemView.findViewById(R.id.album_name);
            vArtist = (TextView) itemView.findViewById(R.id.artist_name);
            itemView.findViewById(R.id.album_artwork).setOnClickListener(this);
            itemView.findViewById(R.id.album_name).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder {

        TextView vName;
        TextView vAlbumCount;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.artist_name);
            vAlbumCount = (TextView) itemView.findViewById(R.id.album_count);

        }

    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        TextView vTitle;
        TextView vArtist;


        public SongViewHolder(View itemView) {
            super(itemView);

            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);


        }


    }

    class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView vSection;

        public SectionViewHolder(View itemView) {
            super(itemView);
            vSection = (TextView) itemView;
        }

    }

    class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ALBUM = 1;
        private static final int ARTIST = 2;
        private static final int SONG = 3;
        private static final int SECTION_ALBUMS = 4;
        private static final int SECTION_ARTISTS = 5;
        private static final int SECTION_SONGS = 6;


        private List<Album> mAlbumList = Collections.synchronizedList(new ArrayList<Album>());
        private List<Artist> mArtistList = Collections.synchronizedList(new ArrayList<Artist>());
        private List<Song> mSongList = Collections.synchronizedList(new ArrayList<Song>());
        private Drawable mDefaultArtwork;


        public SearchAdapter(Context c) {
            mDefaultArtwork = ImageUtils.getDefaultArtwork(c);

        }

        public void setAlbumList(List<Album> albumList) {
            mAlbumList.clear();
            mAlbumList.addAll(albumList);
            notifyDataSetChanged();
        }

        public void setArtistList(List<Artist> artistList) {
            mArtistList.clear();
            mArtistList.addAll(artistList);
            notifyDataSetChanged();
        }

        public void setSongList(List<Song> songList) {
            mSongList.clear();
            mSongList.addAll(songList);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {

            int count = 0;
            if (mAlbumList.size() > 0) {
                count += mAlbumList.size() + 1;
            }
            if (mArtistList.size() > 0) {
                count += mArtistList.size() + 1;
            }
            if (mSongList.size() > 0) {
                count += mSongList.size() + 1;
            }
            return count;

        }

        @Override
        public int getItemViewType(int position) {
            int albumRows = mAlbumList.size() > 0 ? mAlbumList.size() + 1 : 0;

            if (albumRows > position) {
                if (position == 0) {
                    return SECTION_ALBUMS;
                }
                return ALBUM;
            }
            int artistRows = mArtistList.size() > 0 ? mArtistList.size() + 1 : 0;
            if (albumRows + artistRows > position) {
                if (position - albumRows == 0) {
                    return SECTION_ARTISTS;
                }
                return ARTIST;
            }
            int songRows = mSongList.size() > 0 ? mSongList.size() + 1 : 0;
            if (albumRows + artistRows + songRows > position) {
                if (position - albumRows - artistRows == 0) {
                    return SECTION_SONGS;
                }
                return SONG;
            }
            return 0;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            int type = getItemViewType(position);
            int albumRows = mAlbumList.size() > 0 ? mAlbumList.size() + 1 : 0;


            int artistRows = mArtistList.size() > 0 ? mArtistList.size() + 1 : 0;

            switch (type) {
                case ALBUM:
                    Album album = mAlbumList.get(position - 1);
                    ((AlbumViewHolder) viewHolder).vName.setText(album.getAlbumName());
                    ((AlbumViewHolder) viewHolder).vArtist.setText(album.getArtistName());
                    if (mDefaultArtwork != null) {
                        ((AlbumViewHolder) viewHolder).vArtwork.setImageDrawable(mDefaultArtwork);
                    }
                    ImageUtils.loadArtworkAsync(album.getId(), ((AlbumViewHolder) viewHolder).vArtwork);

                    break;
                case ARTIST:
                    Artist artist = mArtistList.get(position - albumRows - 1);
                    ((ArtistViewHolder) viewHolder).vName.setText(artist.getName());
                    ((ArtistViewHolder) viewHolder).vAlbumCount.setText(getResources()
                            .getQuantityString(R.plurals.albums_count,
                                    artist.getAlbumCount(), artist.getAlbumCount()));
                    break;
                case SONG:

                    Song song = mSongList.get(position - albumRows - artistRows - 1);

                    ((SongViewHolder) viewHolder).vTitle.setText(song.getTitle());
                    ((SongViewHolder) viewHolder).vArtist.setText(song.getArtist());
                    break;
                case SECTION_ALBUMS:
                    ((SectionViewHolder) viewHolder).vSection.setText(R.string.albums);

                    break;
                case SECTION_ARTISTS:
                    ((SectionViewHolder) viewHolder).vSection.setText(R.string.artists);

                    break;
                case SECTION_SONGS:
                    ((SectionViewHolder) viewHolder).vSection.setText(R.string.titles);

                    break;
            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {


            View itemView;
            RecyclerView.ViewHolder viewHolder;

            switch (type) {
                case ALBUM:
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_item, parent, false);

                    viewHolder = new AlbumViewHolder(itemView);
                    tintMenuButton(itemView);
                    return viewHolder;
                case ARTIST:
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_list_item, parent, false);
                    itemView.setOnClickListener(mOnClickListener);

                    viewHolder = new ArtistViewHolder(itemView);
                    return viewHolder;
                case SONG:
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list_item, parent, false);
                    itemView.findViewById(R.id.item_view).setOnClickListener(mOnClickListener);

                    viewHolder = new SongViewHolder(itemView);
                    tintMenuButton(itemView);
                    return viewHolder;

                case SECTION_ALBUMS:
                case SECTION_ARTISTS:
                case SECTION_SONGS:
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.section, parent, false);
                    viewHolder = new SectionViewHolder(itemView);
                    return viewHolder;
            }
            return null;
        }

        private void tintMenuButton(View itemView) {
            ImageButton menuButton = (ImageButton) itemView.findViewById(R.id.menu_button);
            if(menuButton == null)
            {
                return;
            }
            menuButton.setOnClickListener(mOnClickListener);

            Drawable drawable = menuButton.getDrawable();

            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.primary_text), PorterDuff.Mode.SRC_ATOP);
        }


    }
}
