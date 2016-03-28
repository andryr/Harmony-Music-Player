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

package com.andryr.musicplayer;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.andryr.musicplayer.PlaybackService.PlaybackBinder;
import com.andryr.musicplayer.activities.PreferencesActivity;
import com.andryr.musicplayer.fragments.AlbumFragment;
import com.andryr.musicplayer.fragments.ArtistFragment;
import com.andryr.musicplayer.fragments.BaseFragment;
import com.andryr.musicplayer.fragments.LibraryFragment;
import com.andryr.musicplayer.fragments.PlaylistFragment;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.preferences.ThemePreference;
import com.andryr.musicplayer.utils.DialogUtils;
import com.andryr.musicplayer.utils.NavigationUtils;
import com.andryr.musicplayer.utils.SleepTimer;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.ProgressBar;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String ALBUM_ID = "id";
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_ARTIST = "artist";
    public static final String ALBUM_YEAR = "year";
    public static final String ALBUM_TRACK_COUNT = "track_count";
    public static final String ARTIST_ARTIST_ID = "artist_id";
    public static final String ARTIST_ARTIST_NAME = "artist_name";
    public static final String ARTIST_ALBUM_COUNT = "album_count";
    public static final String ARTIST_TRACK_COUNT = "track_count";
    public static final String SONG_ID = "song_id";
    public static final String SONG_TITLE = "song_title";
    public static final String SONG_ARTIST = "song_artist";
    public static final String SONG_ALBUM = "song_album";
    public static final String SONG_ALBUM_ID = "song_album_id";
    public static final String SONG_TRACK_NUMBER = "song_track_number";
    private static final String SONG_DURATION = "song_duration";

    public static final String ACTION_REFRESH = "resfresh";
    public static final String ACTION_SHOW_ALBUM = "show_album";
    public static final String ACTION_SHOW_ARTIST = "show_artist";
    public static final String ACTION_PLAY_SONG = "play_song";
    public static final String ACTION_ADD_TO_QUEUE = "add_to_queue";
    public static final String ACTION_SET_AS_NEXT_TRACK = "set_as_next_track";

    private static final int SEARCH_ACTIVITY = 234;

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    private Intent mOnActivityResultIntent;
    private PlaybackService mPlaybackService;
    private Intent mServiceIntent;
    private boolean mServiceBound = false;

    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();

    private int mThumbSize;


    private PlaybackRequests mPlaybackRequests;
    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mPlaybackService == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.play_pause_toggle:
                case R.id.quick_play_pause_toggle:

                    mPlaybackService.toggle();

                    break;
                case R.id.quick_prev:
                case R.id.prev:
                    mPlaybackService.playPrev(true);

                    break;
                case R.id.quick_next:
                case R.id.next:
                    mPlaybackService.playNext(true);
                    break;


                case R.id.action_equalizer:
                    NavigationUtils.showEqualizer(MainActivity.this);
                    break;
                case R.id.track_info:
                    NavigationUtils.showPlaybackActivity(MainActivity.this);
                    break;


            }

        }
    };
    private Runnable mUpdateProgressBar = new Runnable() {

        @Override
        public void run() {


            updateProgressBar();


            mHandler.postDelayed(mUpdateProgressBar, 1000);

        }
    };
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            PlaybackService.PlaybackBinder binder = (PlaybackBinder) service;
            mPlaybackService = binder.getService();
            mServiceBound = true;

            mPlaybackRequests.sendRequests();

            updateAll();
         /*   if (!mPlaybackService.isPlaying()
                    && !mPlaybackService.hasPlaylist()) {

                List<Song> playList = getDefaultPlaylist();
                Log.d("playlist", String.valueOf(playList == null));
                if (playList != null) {
                    Log.d("playlist", String.valueOf(playList.size()));

                    int pos = (int) (Math.random() * (playList.size() - 1));
                    Log.d("playlist", String.valueOf(pos));

                    mPlaybackService.setPlayList(playList, pos, false);
                }

            }*/

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;

        }
    };
    private BroadcastReceiver mServiceListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPlaybackService == null) {
                return;
            }
            String action = intent.getAction();
            Log.d("action", action);
            if (action.equals(PlaybackService.PLAYSTATE_CHANGED)) {
                setButtonDrawable();
                if (mPlaybackService.isPlaying()) {
                    mHandler.post(mUpdateProgressBar);
                } else {
                    mHandler.removeCallbacks(mUpdateProgressBar);
                }


            } else if (action.equals(PlaybackService.META_CHANGED)) {
                updateTrackInfo();
            }

        }
    };


    /**
     * Handler for the sleep timer dialog
     */
    private HmsPickerDialogFragment.HmsPickerDialogHandler mHmsPickerHandler = new HmsPickerDialogFragment.HmsPickerDialogHandler() {
        @Override
        public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SleepTimer.setTimer(MainActivity.this, prefs, hours * 3600 + minutes * 60 + seconds);
        }
    };


    private DialogInterface.OnClickListener mSleepTimerDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which) {
                case DialogInterface.BUTTON_POSITIVE: // set a new timer
                    DialogUtils.showSleepHmsPicker(MainActivity.this, mHmsPickerHandler);
                    break;
                case DialogInterface.BUTTON_NEGATIVE: // cancel the current timer
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SleepTimer.cancelTimer(MainActivity.this, prefs);
                    break;
                case DialogInterface.BUTTON_NEUTRAL: // just go back
                    break;

            }
        }
    };

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

  /*  private List<Song> getDefaultPlaylist() {
        ContentResolver resolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK};
        Cursor cursor = resolver.query(musicUri, projection, null, null,
                android.provider.MediaStore.Audio.Media.TITLE);

        List<Song> songList = new ArrayList<>();
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


                songList.add(new Song(id, title, artist, album, albumId, track));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return songList;

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();

        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_main);


        mThumbSize = getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);


        mPlaybackRequests = new PlaybackRequests();

        if (savedInstanceState == null) {
            showLibrary();
        }


        findViewById(R.id.quick_play_pause_toggle).setOnClickListener(
                mOnClickListener);

        findViewById(R.id.track_info).setOnClickListener(mOnClickListener);

        findViewById(R.id.quick_prev).setOnClickListener(mOnClickListener);
        findViewById(R.id.quick_next).setOnClickListener(mOnClickListener);


        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.inflateHeaderView(R.layout.navigation_header);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                   /* case R.id.action_home:
                        showHome();
                        break;*/
                    case R.id.action_library:
                        showLibrary();
                        break;
                    case R.id.action_favorites:
                        showFavorites();
                        break;
                    case R.id.action_equalizer:
                        NavigationUtils.showEqualizer(MainActivity.this);
                        break;
                    case R.id.action_settings:
                        NavigationUtils.showPreferencesActivity(MainActivity.this);
                        break;
                }
                return true;
            }
        });
        checkPermissions();

    }

    private void setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean dark = ThemeHelper.isDarkThemeSelected(this);
        String prefKey = getString(R.string.pref_theme_key);
        int theme = prefs.getInt(prefKey, ThemePreference.DEFAULT_THEME);

        switch (theme) {
            case ThemePreference.DARK_BLUE_GREY_THEME:
                if (dark) {
                    setTheme(R.style.MainActivityDarkBlueGreyDark);
                } else {
                    setTheme(R.style.MainActivityDarkBlueGreyLight);
                }
                break;
            case ThemePreference.BLUE_GREY_THEME:
                if (dark) {
                    setTheme(R.style.MainActivityBlueGreyDark);
                } else {
                    setTheme(R.style.MainActivityBlueGreyLight);
                }
                break;
            case ThemePreference.BLUE_THEME:
                if (dark) {
                    setTheme(R.style.MainActivityBlueDark);
                } else {
                    setTheme(R.style.MainActivityBlueLight);
                }
                break;
        }
    }

    /**
     * Affiche bibliothèque sans backstack
     */
    public void showLibrary() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.getMenu().findItem(R.id.action_library).setChecked(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, LibraryFragment.newInstance()).commit();
    }

    /**
     * Afficher favoris sans backstack
     */
    public void showFavorites() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.getMenu().findItem(R.id.action_favorites).setChecked(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, PlaylistFragment.newFavoritesFragment()).commit();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                DialogUtils.showPermissionDialog(this, getString(R.string.permission_read_external_storage), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);


            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                DialogUtils.showPermissionDialog(this, getString(R.string.permission_write_external_storage), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                });

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                DialogUtils.showPermissionDialog(this, getString(R.string.permission_read_phone_state), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                });

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSIONS_REQUEST_READ_PHONE_STATE);


            }
        }
    }

    @Override

    protected void onStop() {


        super.onStop();
    }

   /* private void showHome() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.getMenu().findItem(R.id.action_home).setChecked(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance()).commit();
    }*/

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mOnActivityResultIntent != null) {
            Bundle bundle = mOnActivityResultIntent.getExtras();
            if (mOnActivityResultIntent.getAction().equals(ACTION_REFRESH)) {
                refresh();
            } else if (mOnActivityResultIntent.getAction().equals(ACTION_SHOW_ALBUM)) {
                Album album = getAlbumFromBundle(bundle);
                AlbumFragment fragment = AlbumFragment.newInstance(album);
                setFragment(fragment);
            } else if (mOnActivityResultIntent.getAction().equals(ACTION_SHOW_ARTIST)) {
                Artist artist = getArtistFromBundle(bundle);
                ArtistFragment fragment = ArtistFragment.newInstance(artist);
                setFragment(fragment);
            } else {


                Song song = getSongFromBundle(bundle);

                if (mOnActivityResultIntent.getAction().equals(ACTION_PLAY_SONG)) {
                    ArrayList<Song> songList = new ArrayList<>();
                    songList.add(song);
                    mPlaybackRequests.requestPlayList(songList, 0, true);
                } else if (mOnActivityResultIntent.getAction().equals(ACTION_ADD_TO_QUEUE)) {
                    mPlaybackRequests.requestAddToQueue(song);
                } else if (mOnActivityResultIntent.getAction().equals(ACTION_SET_AS_NEXT_TRACK)) {
                    mPlaybackRequests.requestAsNextTrack(song);
                }


            }
            mOnActivityResultIntent = null;
        }
    }

    public void refresh() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f != null) {
                Log.d("frag", f.getClass().getCanonicalName());
                ((BaseFragment) f).load();
            }
        }
    }

    private Album getAlbumFromBundle(Bundle bundle) {
        long id = bundle.getLong(ALBUM_ID);
        String title = bundle.getString(ALBUM_NAME);
        String artist = bundle.getString(ALBUM_ARTIST);
        int year = bundle.getInt(ALBUM_YEAR);
        int trackCount = bundle.getInt(ALBUM_TRACK_COUNT);

        return new Album(id, title, artist, year, trackCount);
    }

    public void setFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f).addToBackStack(null).commit();
    }

    private Artist getArtistFromBundle(Bundle bundle) {
        long id = bundle.getLong(ARTIST_ARTIST_ID);
        String name = bundle.getString(ARTIST_ARTIST_NAME);
        int albumCount = bundle.getInt(ARTIST_ALBUM_COUNT);
        int trackCount = bundle.getInt(ARTIST_TRACK_COUNT);
        return new Artist(id, name, albumCount, trackCount);
    }

    private Song getSongFromBundle(Bundle bundle) {
        long id = bundle.getLong(SONG_ID);
        String title = bundle.getString(SONG_TITLE);
        String artist = bundle.getString(SONG_ARTIST);
        String album = bundle.getString(SONG_ALBUM);
        long albumId = bundle.getLong(SONG_ALBUM_ID);
        int trackNumber = bundle.getInt(SONG_TRACK_NUMBER);
        long duration = bundle.getLong(SONG_DURATION);


        return new Song(id, title, artist, album, albumId, trackNumber, duration);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    showLibrary();
                }
                return true;
            case R.id.action_search:
                NavigationUtils.showSearchActivity(this, SEARCH_ACTIVITY);
                return true;
            case R.id.action_equalizer:
                NavigationUtils.showEqualizer(this);
                return true;
            case R.id.action_preferences:
                NavigationUtils.showPreferencesActivity(this);
                break;
            case R.id.action_sleep_timer:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (SleepTimer.isTimerSet(prefs)) {
                    DialogUtils.showSleepTimerDialog(this, mSleepTimerDialogListener);
                } else {
                    DialogUtils.showSleepHmsPicker(this, mHmsPickerHandler);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSongSelected(List<Song> songList, int position) {
        if (mPlaybackService == null) {
            return;
        }
        mPlaybackService.setPlayList(songList, position, true);
        // mPlaybackService.play();

    }

    public void onShuffleRequested(List<Song> songList, boolean play) {
        if (mPlaybackService == null) {
            return;
        }
        mPlaybackService.setPlayListAndShuffle(songList, play);


    }

    public void addToQueue(Song song) {
        if (mPlaybackService != null) {
            mPlaybackService.addToQueue(song);
        }

    }

    public void setAsNextTrack(Song song) {
        if (mPlaybackService != null) {
            mPlaybackService.setAsNextTrack(song);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_ACTIVITY && resultCode == RESULT_OK) {
            mOnActivityResultIntent = data;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mServiceBound) {
            mPlaybackService = null;

            unregisterReceiver(mServiceListener);

            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        mHandler.removeCallbacks(mUpdateProgressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mServiceBound) {
            mServiceIntent = new Intent(this, PlaybackService.class);
            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            startService(mServiceIntent);
            IntentFilter filter = new IntentFilter();
            filter.addAction(PlaybackService.META_CHANGED);
            filter.addAction(PlaybackService.PLAYSTATE_CHANGED);
            filter.addAction(PlaybackService.POSITION_CHANGED);
            filter.addAction(PlaybackService.ITEM_ADDED);
            filter.addAction(PlaybackService.ORDER_CHANGED);
            registerReceiver(mServiceListener, filter);
        } else {
            updateAll();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        // if (!mServiceBound) {
        // mServiceIntent = new Intent(this, PlaybackService.class);
        // bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        // startService(mServiceIntent);
        // }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PlaybackService.PREF_AUTO_PAUSE, true);
                if (mPlaybackService != null) {
                    mPlaybackService.setAutoPauseEnabled(true);
                }
                editor.commit();
                break;

        }
    }

    private void updateAll() {
        if (mPlaybackService != null) {
            Log.d("playlist", "hasplaylist " + mPlaybackService.hasPlaylist());
            updateTrackInfo();
            setButtonDrawable();
            if (mPlaybackService.isPlaying()) {
                mHandler.post(mUpdateProgressBar);
            }


        }
    }

    private void updateTrackInfo() {
        View trackInfoLayout = findViewById(R.id.track_info);

        if (mPlaybackService != null && mPlaybackService.hasPlaylist()) {

            if (trackInfoLayout.getVisibility() != View.VISIBLE) {
                trackInfoLayout.setVisibility(View.VISIBLE);
                trackInfoLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_grow_fade_in_from_bottom));
            }
            String title = mPlaybackService.getSongTitle();
            String artist = mPlaybackService.getArtistName();
            if (title != null) {
                ((TextView) findViewById(R.id.song_title)).setText(title);

            }
            if (artist != null) {
                ((TextView) findViewById(R.id.song_artist)).setText(artist);
            }

            long albumId = mPlaybackService.getAlbumId();
            final ImageView minArtworkView = (ImageView) findViewById(R.id.artwork_min);
            ArtworkCache.getInstance().loadBitmap(albumId, minArtworkView, mThumbSize, mThumbSize);


            int duration = mPlaybackService.getTrackDuration();
            if (duration != -1) {
                mProgressBar.setMax(duration);

                updateProgressBar();
            }


        } else {
            trackInfoLayout.setVisibility(View.GONE);
        }
    }

    private void setButtonDrawable() {
        if (mPlaybackService != null) {
            ImageButton quickButton = (ImageButton) findViewById(R.id.quick_play_pause_toggle);
            if (mPlaybackService.isPlaying()) {
                quickButton.setImageResource(R.drawable.ic_pause);
            } else {
                quickButton.setImageResource(R.drawable.ic_play_small);
            }
        }

    }


    private void updateProgressBar() {
        if (mPlaybackService != null) {
            int position = mPlaybackService.getPlayerPosition();
            mProgressBar.setProgress(position);


        }
    }

    private class PlaybackRequests {

        private List<Song> mPlayList;
        private int mIndex;
        private boolean mAutoPlay;

        private Song mNextTrack;

        private Song mAddToQueue;

        private void requestPlayList(List<Song> playList, int index, boolean autoPlay) {
            if (mPlaybackService != null) {
                mPlaybackService.setPlayList(playList, 0, true);
            } else {
                mPlayList = playList;
                mIndex = index;
                mAutoPlay = autoPlay;
            }
        }


        public void requestAddToQueue(Song song) {
            if (mPlaybackService != null) {
                mPlaybackService.addToQueue(song);
            } else {
                mAddToQueue = song;
            }
        }

        public void requestAsNextTrack(Song song) {
            if (mPlaybackService != null) {
                mPlaybackService.setAsNextTrack(song);
            } else {
                mNextTrack = song;
            }
        }

        public void sendRequests() {
            if (mPlaybackService == null) {
                return;
            }

            if (mPlayList != null) {
                mPlaybackService.setPlayList(mPlayList, mIndex, mAutoPlay);
                mPlayList = null;
            }

            if (mAddToQueue != null) {
                mPlaybackService.addToQueue(mAddToQueue);
                mAddToQueue = null;
            }

            if (mNextTrack != null) {
                mPlaybackService.setAsNextTrack(mNextTrack);
                mNextTrack = null;
            }
        }
    }

}
