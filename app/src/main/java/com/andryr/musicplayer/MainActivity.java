package com.andryr.musicplayer;


import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.andryr.musicplayer.PlaybackService.PlaybackBinder;
import com.andryr.musicplayer.activities.EqualizerActivity;
import com.andryr.musicplayer.activities.PreferencesActivity;
import com.andryr.musicplayer.activities.SearchActivity;
import com.andryr.musicplayer.fragments.AlbumFragment;
import com.andryr.musicplayer.fragments.ArtistFragment;
import com.andryr.musicplayer.fragments.BaseFragment;
import com.andryr.musicplayer.fragments.MainFragment;
import com.andryr.musicplayer.fragments.dialog.ThemeDialog;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.ArtworkHelper;
import com.andryr.musicplayer.utils.OnItemMovedListener;
import com.andryr.musicplayer.animation.PanelSlideTransition;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.ProgressBar;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        FragmentListener {
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
    public static final String ACTION_REFRESH = "resfresh";
    public static final String ACTION_SHOW_ALBUM = "show_album";
    public static final String ACTION_SHOW_ARTIST = "show_artist";
    public static final String ACTION_PLAY_SONG = "play_song";
    public static final String ACTION_ADD_TO_QUEUE = "add_to_queue";
    public static final String ACTION_SET_AS_NEXT_TRACK = "set_as_next_track";
    private static final int SEARCH_ACTIVITY = 234;
    private Intent mOnActivityResultIntent;
    private PlaybackService mPlaybackService;
    private Intent mServiceIntent;
    private boolean mServiceBound = false;

    private SlidingUpPanelLayout mSlidingLayout;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler();


    private RecyclerView mQueueView;

    private PlaybackRequests mPlaybackRequests;


    private List<Song> mQueue;
    private QueueAdapter mQueueAdapter = new QueueAdapter();


    private boolean mQueueViewAnimating = false;

    private Animator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {

        private int mCount = 0;

        @Override
        public void onAnimationStart(Animator animation) {
            mCount++;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mCount--;
            if (mCount == 0) {
                mQueueViewAnimating = false;
            }
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

    private Runnable mUpdateSeekBarRunnable = new Runnable() {

        @Override
        public void run() {


            updateSeekBar();


            mHandler.postDelayed(mUpdateSeekBarRunnable, 1000);

        }
    };


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;

        }

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
    };

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

                case R.id.shuffle:
                    boolean shuffle = mPlaybackService.isShuffleEnabled();


                    mPlaybackService.setShuffleEnabled(!shuffle);
                    updateShuffleButton();
                    break;
                case R.id.repeat:
                    int mode = mPlaybackService.getNextRepeatMode();//TODO changer ça


                    mPlaybackService.setRepeatMode(mode);
                    updateRepeatButton();
                    break;
                case R.id.action_equalizer:
                    showEqualizer();
                    break;
                case R.id.action_view_queue:

                    if (!mQueueViewAnimating) {
                        mQueueViewAnimating = true;
                        if (mQueueView.getVisibility() != View.VISIBLE) {
                            mQueueView.setVisibility(View.VISIBLE);

                            ViewPropertyAnimator.animate(mQueueView).alpha(1.0F).setListener(mAnimatorListener).start();

                        } else {
                            ViewPropertyAnimator.animate(mQueueView).alpha(0.0F)
                                    .setListener(new AnimatorListenerAdapter() {

                                        @Override
                                        public void onAnimationEnd(
                                                Animator animation) {
                                            mAnimatorListener
                                                    .onAnimationEnd(animation);
                                            mQueueView.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onAnimationStart(
                                                Animator animation) {
                                            mAnimatorListener
                                                    .onAnimationStart(animation);
                                        }

                                    }).start();

                        }
                    }
                    break;
                case R.id.action_close:
                    mSlidingLayout.setPanelState(PanelState.COLLAPSED);
                    break;

            }

        }
    };

    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPlaybackService != null && mPlaybackService.isPlaying()) {
                mHandler.post(mUpdateSeekBarRunnable);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(mUpdateSeekBarRunnable);

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser
                    && mPlaybackService != null
                    && (mPlaybackService.isPlaying() || mPlaybackService
                    .isPaused())) {
                mPlaybackService.seekTo(seekBar.getProgress());
            }

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
                    mHandler.post(mUpdateSeekBarRunnable);
                } else {
                    mHandler.removeCallbacks(mUpdateSeekBarRunnable);
                }


            } else if (action.equals(PlaybackService.META_CHANGED)) {
                updatePanelState();
                updateTrackInfo();
            } else if (action.equals(PlaybackService.QUEUE_CHANGED) || action.equals(PlaybackService.POSITION_CHANGED) || action.equals(PlaybackService.ITEM_ADDED) || action.equals(PlaybackService.ORDER_CHANGED)) {
                Log.d("eee", "position_changed");
                updateQueue(action);
            }

        }
    };

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
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.META_CHANGED);
        filter.addAction(PlaybackService.PLAYSTATE_CHANGED);
        filter.addAction(PlaybackService.POSITION_CHANGED);
        filter.addAction(PlaybackService.ITEM_ADDED);
        filter.addAction(PlaybackService.ORDER_CHANGED);
        registerReceiver(mServiceListener, filter);

        // if (!mServiceBound) {
        // mServiceIntent = new Intent(this, PlaybackService.class);
        // bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        // startService(mServiceIntent);
        // }

    }

    public void setFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f).addToBackStack(null).commit();
    }

    public void refresh() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f != null) {
                Log.d("frag", f.getClass().getCanonicalName());
                ((BaseFragment) f).refresh();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();

        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_main);

        mPlaybackRequests = new PlaybackRequests();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, MainFragment.newInstance()).commit();
        }
        mSlidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingLayout.setPanelSlideListener(new PanelSlideTransition(findViewById(R.id.track_info), findViewById(R.id.top_bar)));


        //updatePanelState();


        mQueueView = (RecyclerView) findViewById(R.id.queue_view);
        mQueueView.setLayoutManager(new LinearLayoutManager(this));

        mQueueView.setAdapter(mQueueAdapter);
        mDragAndDropListener = new OnItemMovedListener(mQueueView,
                (ImageView) findViewById(R.id.drag_overlay)) {

            @Override
            public void onItemMoved(int oldPosition, int newPosition) {
                mQueueAdapter.moveItem(oldPosition, newPosition);

            }
        };
        mQueueView.addOnItemTouchListener(mDragAndDropListener);

        findViewById(R.id.prev).setOnClickListener(mOnClickListener);
        findViewById(R.id.next).setOnClickListener(mOnClickListener);
        findViewById(R.id.play_pause_toggle).setOnClickListener(
                mOnClickListener);
        findViewById(R.id.quick_play_pause_toggle).setOnClickListener(
                mOnClickListener);

        findViewById(R.id.quick_prev).setOnClickListener(mOnClickListener);
        findViewById(R.id.quick_next).setOnClickListener(mOnClickListener);

        findViewById(R.id.shuffle).setOnClickListener(mOnClickListener);
        findViewById(R.id.repeat).setOnClickListener(mOnClickListener);

        findViewById(R.id.action_equalizer)
                .setOnClickListener(mOnClickListener);
        findViewById(R.id.action_view_queue).setOnClickListener(
                mOnClickListener);
        findViewById(R.id.action_close).setOnClickListener(mOnClickListener);


        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    private boolean mLayout = false;

                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {

                        if (mLayout) {
                            return;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mSeekBar.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        }

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSeekBar
                                .getLayoutParams();
                        params.setMargins(0, -mSeekBar.getHeight() / 2, 0,
                                -mSeekBar.getHeight() / 2);
                        mSeekBar.setLayoutParams(params);

                        mLayout = true;

                    }

                });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

    }


    private void setTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean dark = ThemeHelper.isDarkThemeSelected(this);
        int theme = prefs.getInt(PreferencesActivity.KEY_PREF_THEME, ThemeDialog.BLUE_THEME);

        switch (theme) {
            case ThemeDialog.ORANGE_THEME:
                if (dark) {
                    Log.d("theme", "orange dark");
                    setTheme(R.style.MainActivityOrangeDark);
                } else {
                    Log.d("theme", "orange light");
                    setTheme(R.style.MainActivityOrangeLight);
                }
                break;
            case ThemeDialog.BLUE_THEME:
                if (dark) {
                    Log.d("theme", "blue dark");
                    setTheme(R.style.MainActivityBlueDark);
                } else {
                    Log.d("theme", "blue light");
                    setTheme(R.style.MainActivityBlueLight);
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mServiceBound) {
            mServiceIntent = new Intent(this, PlaybackService.class);
            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            startService(mServiceIntent);
        } else {
            updateAll();
        }
    }

    private void updateAll() {
        if (mPlaybackService != null) {
            Log.d("playlist", "hasplaylist " + mPlaybackService.hasPlaylist());
            updateQueue();
            updateTrackInfo();
            setButtonDrawable();
            updatePanelState();
            if (mPlaybackService.isPlaying()) {
                mHandler.post(mUpdateSeekBarRunnable);
            }

            if (mSlidingLayout.getPanelState() == PanelState.EXPANDED) {

                View controlsLayout = findViewById(R.id.quick_controls);
                if (controlsLayout.getVisibility() == View.VISIBLE) {
                    controlsLayout.setVisibility(View.INVISIBLE);
                }
            }

            updateShuffleButton();
            updateRepeatButton();

        }
    }

    private void updateShuffleButton() {
        boolean shuffle = mPlaybackService.isShuffleEnabled();
        Log.d("shuffle", "shuffle " + String.valueOf(shuffle));
        ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffle);
        if (shuffle) {
            shuffleButton.setColorFilter(getStyleColor(R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);

        } else {
            shuffleButton.setColorFilter(getResourcesColor(R.color.playback_controls_tint), PorterDuff.Mode.SRC_ATOP);

        }


    }

    private void updateRepeatButton() {
        ImageButton repeatButton = (ImageButton) findViewById(R.id.repeat);
        int mode = mPlaybackService.getRepeatMode();
        if (mode == PlaybackService.NO_REPEAT) {
            repeatButton.setImageResource(R.drawable.ic_repeat);
            repeatButton.setColorFilter(getResourcesColor(R.color.playback_controls_tint), PorterDuff.Mode.SRC_ATOP);
        } else if (mode == PlaybackService.REPEAT_ALL) {
            repeatButton.setImageResource(R.drawable.ic_repeat);
            repeatButton.setColorFilter(getStyleColor(R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
        } else if (mode == PlaybackService.REPEAT_CURRENT) {
            repeatButton.setImageResource(R.drawable.ic_repeat_one);
            repeatButton.setColorFilter(getStyleColor(R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);

        }
    }

    private int getStyleColor(int attrId) {
        int[] attrs = {attrId};

        TypedArray ta = getTheme().obtainStyledAttributes(attrs);


        return ta.getColor(0, Color.BLACK);
    }

    private int getResourcesColor(int id) {
        return getResources().getColor(id);
    }

    private void updatePanelState() {
        if (mPlaybackService != null && mPlaybackService.hasPlaylist()) {
            Log.d("playlist", "panel " + (mPlaybackService != null && mPlaybackService.hasPlaylist()));

            mSlidingLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.track_info_layout_height));


        } else {
            Log.d("playlist", "panel2 " + (mPlaybackService != null && mPlaybackService.hasPlaylist()));

            mSlidingLayout.setPanelHeight(0);
            mSlidingLayout.setPanelState(PanelState.COLLAPSED);

        }

    }

    @Override
    protected void onStop() {
        unregisterReceiver(mServiceListener);
        mPlaybackService = null;

        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }

        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateSeekBarRunnable);
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
                }
                return true;
            case R.id.action_search:
                showSearchActivity();
                return true;
            case R.id.action_equalizer:
                showEqualizer();
                return true;
            case R.id.action_preferences:
                showPreferencesActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showSearchActivity() {
        Intent i = new Intent(this, SearchActivity.class);
        startActivityForResult(i, SEARCH_ACTIVITY);
    }

    private void showEqualizer() {
        Intent i = new Intent(this, EqualizerActivity.class);
        startActivity(i);
    }

    private void showPreferencesActivity() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }

    @Override
    public void onSongSelected(List<Song> songList, int position) {
        if (mPlaybackService == null) {
            return;
        }
        mPlaybackService.setPlayList(songList, position, true);
        // mPlaybackService.play();

    }

    @Override
    public void onShuffleRequested(List<Song> songList, boolean play) {
        mPlaybackService.setPlayListAndShuffle(songList, play);

        updateShuffleButton();

    }

    private void updateTrackInfo() {
        if (mPlaybackService != null) {

            String title = mPlaybackService.getTrackName();
            String artist = mPlaybackService.getArtistName();
            if (title != null) {
                ((TextView) findViewById(R.id.song_title)).setText(title);
                ((TextView) findViewById(R.id.song_title2)).setText(title);

            }
            if (artist != null) {
                ((TextView) findViewById(R.id.song_artist)).setText(artist);
                ((TextView) findViewById(R.id.song_artist2)).setText(artist);

            }

            long albumId = mPlaybackService.getAlbumId();
            ImageView artworkView = (ImageView) findViewById(R.id.artwork);
            ImageView minArtworkView = (ImageView) findViewById(R.id.artwork_min);
            ArtworkHelper.loadArtwork(albumId, true, artworkView, minArtworkView);

            int duration = mPlaybackService.getTrackDuration();
            if (duration != -1) {
                mSeekBar.setMax(duration);
                mProgressBar.setMax(duration);
                ((TextView) findViewById(R.id.track_duration))
                        .setText(msToText(duration));
                updateSeekBar();
            }

            setQueueSelection(mPlaybackService.getPositionWithinPlayList());

        }
    }

    private void setQueueSelection(int position) {
        mQueueAdapter.setSelection(position);

        if (position >= 0 && position < mQueue.size()) {
            mQueueView.scrollToPosition(position);
        }


    }

    private void updateSeekBar() {
        if (mPlaybackService != null) {
            int position = mPlaybackService.getPlayerPosition();
            mSeekBar.setProgress(position);
            mProgressBar.setProgress(position);

            ((TextView) findViewById(R.id.current_position))
                    .setText(msToText(position));
        }
    }

    private String msToText(int msec) {
        return String.format(Locale.getDefault(), "%d:%02d", msec / 60000,
                (msec % 60000) / 1000);
    }

    private void updateQueue() {
        updateQueue(null);
    }

    private void updateQueue(String action) {
        if (mPlaybackService == null) {
            return;
        }

        List<Song> queue = mPlaybackService.getPlayList();
        if (queue != mQueue) {

            Log.d("eee", "testt");
            mQueue = queue;
            mQueueAdapter.setQueue(mQueue);

        }


//        if (action != null && (action.equals(PlaybackService.ITEM_ADDED) || action.equals(PlaybackService.ORDER_CHANGED))) {
//            mQueueAdapter.notifyDataSetChanged();
//        }


        mQueueAdapter.notifyDataSetChanged();


        setQueueSelection(mPlaybackService.getPositionWithinPlayList());


    }

    private void setButtonDrawable() {
        if (mPlaybackService != null) {
            FloatingActionButton button = (FloatingActionButton) findViewById(R.id.play_pause_toggle);
            ImageButton quickButton = (ImageButton) findViewById(R.id.quick_play_pause_toggle);
            if (mPlaybackService.isPlaying()) {
                button.setImageResource(R.drawable.ic_pause_black);
                quickButton.setImageResource(R.drawable.ic_pause);
            } else {
                button.setImageResource(R.drawable.ic_play_black);
                quickButton.setImageResource(R.drawable.ic_play_small);
            }
        }

    }


    @Override
    public void onBackPressed() {
        if (mSlidingLayout != null
                && (mSlidingLayout.getPanelState() == PanelState.EXPANDED || mSlidingLayout
                .getPanelState() == PanelState.ANCHORED)) {
            mSlidingLayout.setPanelState(PanelState.COLLAPSED);
            return;
        }
        super.onBackPressed();
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

    private Album getAlbumFromBundle(Bundle bundle) {
        long id = bundle.getLong(ALBUM_ID);
        String title = bundle.getString(ALBUM_NAME);
        String artist = bundle.getString(ALBUM_ARTIST);
        int year = bundle.getInt(ALBUM_YEAR);
        int trackCount = bundle.getInt(ALBUM_TRACK_COUNT);

        return new Album(id, title, artist, year, trackCount);
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


        return new Song(id, title, artist, album, albumId, trackNumber);

    }

    class QueueItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        TextView vTitle;
        TextView vArtist;
        ImageButton vReorderButton;
        View itemView;

        public QueueItemViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.title);
            vArtist = (TextView) itemView.findViewById(R.id.artist);
            vReorderButton = (ImageButton) itemView
                    .findViewById(R.id.reorder_button);
            itemView.findViewById(R.id.song_info).setOnClickListener(this);
            itemView.findViewById(R.id.delete_button).setOnClickListener(this);
            this.itemView = itemView;

        }


        @Override
        public void onClick(View v) {
            if (mPlaybackService != null) {

                int position = getAdapterPosition();

                switch (v.getId()) {
                    case R.id.song_info:
                        mPlaybackService.setPosition(position, true);

                        break;
                    case R.id.delete_button:
                        if (mQueueAdapter.getItemCount() > 0) {
                            mQueueAdapter.removeItem(position);
                        }
                        break;

                }

            }
        }
    }

    class QueueAdapter extends RecyclerView.Adapter<QueueItemViewHolder> {

        private List<Song> mQueue;

        private int mSelectedItemPosition = -1;

        public void setQueue(List<Song> queue) {
            mQueue = queue;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (mQueue == null) {
                return 0;
            }
            return mQueue.size();
        }

        @Override
        public void onBindViewHolder(QueueItemViewHolder viewHolder,
                                     int position) {
            Song song = mQueue.get(position);
            if (position == mSelectedItemPosition) {
                viewHolder.itemView.setSelected(true);
            } else {
                viewHolder.itemView.setSelected(false);

            }

            viewHolder.vTitle.setText(song.getTitle());
            viewHolder.vArtist.setText(song.getArtist());

        }

        @Override
        public QueueItemViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.queue_item, parent, false);


            QueueItemViewHolder viewHolder = new QueueItemViewHolder(itemView);
            viewHolder.vReorderButton.setOnTouchListener(mOnItemTouchListener);
            return viewHolder;
        }

        public void moveItem(int oldPosition, int newPosition) {
            if (oldPosition < 0 || oldPosition >= mQueue.size()
                    || newPosition < 0 || newPosition >= mQueue.size()) {
                return;
            }

            Collections.swap(mQueue, oldPosition, newPosition);

            if (mSelectedItemPosition == oldPosition) {
                mSelectedItemPosition = newPosition;
            } else if (mSelectedItemPosition == newPosition) {
                mSelectedItemPosition = oldPosition;
            }
            notifyItemMoved(oldPosition, newPosition);

        }

        public void removeItem(int position) {
            mQueue.remove(position);
            notifyItemRemoved(position);
        }

        public void setSelection(int position) {
            int oldSelection = mSelectedItemPosition;
            mSelectedItemPosition = position;

            if (oldSelection >= 0 && oldSelection < mQueue.size()) {
                notifyItemChanged(oldSelection);
            }

            if (mSelectedItemPosition >= 0
                    && mSelectedItemPosition < mQueue.size()) {
                notifyItemChanged(mSelectedItemPosition);
            }
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
