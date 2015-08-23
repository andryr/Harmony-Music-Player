package com.andryr.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
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
import com.andryr.musicplayer.fragments.BaseFragment;
import com.andryr.musicplayer.fragments.MainFragment;
import com.nineoldandroids.view.ViewHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends ActionBarActivity implements
        OnSongSelectedListener {

    private PlaybackService mPlaybackService;
    private Intent mServiceIntent;
    private boolean mServiceBound = false;

    private SlidingUpPanelLayout mSlidingLayout;
    private SeekBar mSeekBar;
    private Handler mHandler = new Handler();

    private View mQuickControls;
    private View mMenu;

    private RecyclerView mQueueView;

    private List<Song> mQueue;
    private QueueAdapter mQueueAdapter = new QueueAdapter();

    private boolean mQueueViewAnimating = false;

    private AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {

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

    private OnClickListener mOnItemClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mPlaybackService != null) {
                int position = mQueueView.getChildPosition(v);

                mPlaybackService.setPosition(position, true);
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

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    updateSeekBar();

                }
            });
            mHandler.postDelayed(mUpdateSeekBarRunnable, 1000);

        }
    };

    private PanelSlideListener mSlideListener = new PanelSlideListener() {

        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            if (mQuickControls.getVisibility() != View.VISIBLE) {
                mQuickControls.setVisibility(View.VISIBLE);
            }

            if (mMenu.getVisibility() == View.VISIBLE) {
                mMenu.setVisibility(View.GONE);
            }
            ViewHelper.setAlpha(mQuickControls, 1 - slideOffset);

        }

        @Override
        public void onPanelHidden(View panel) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPanelExpanded(View panel) {

            mQuickControls.setVisibility(View.GONE);
            mMenu.setVisibility(View.VISIBLE);

        }

        @Override
        public void onPanelCollapsed(View panel) {
            ViewHelper.setAlpha(mQuickControls, 1);
            mQuickControls.setVisibility(View.VISIBLE);
            mMenu.setVisibility(View.GONE);

        }

        @Override
        public void onPanelAnchored(View panel) {
            // TODO Auto-generated method stub

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

            updateAll();
            if (!mPlaybackService.isPlaying()
                    && !mPlaybackService.hasPlaylist()) {

                List<Song> playList = getDefaultPlaylist();
                Log.d("playlist", String.valueOf(playList == null));
                if (playList != null) {
                    Log.d("playlist", String.valueOf(playList.size()));

                    int pos = (int) (Math.random() * (playList.size() - 1));
                    Log.d("playlist", String.valueOf(pos));

                    mPlaybackService.setPlayList(playList, pos, false);
                }

            }

        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
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
                    ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffle);
                    if (shuffle) {
                        shuffleButton.setImageResource(R.drawable.ic_shuffle);
                    } else {
                        shuffleButton
                                .setImageResource(R.drawable.ic_shuffle_checked);

                    }

                    mPlaybackService.setShuffleEnabled(!shuffle);
                    break;
                case R.id.repeat:
                    ImageButton repeatButton = (ImageButton) findViewById(R.id.repeat);
                    int mode = mPlaybackService.getNextRepeatMode();
                    if (mode == PlaybackService.NO_REPEAT) {
                        repeatButton.setImageResource(R.drawable.ic_repeat);
                    } else if (mode == PlaybackService.REPEAT_ALL) {
                        repeatButton.setImageResource(R.drawable.ic_repeat_checked);
                    } else if (mode == PlaybackService.REPEAT_CURRENT) {
                        repeatButton.setImageResource(R.drawable.ic_repeat_one);
                    }

                    mPlaybackService.setRepeatMode(mode);
                    break;
                case R.id.action_equalizer:
                    showEqualizer();
                    break;
                case R.id.action_view_queue:

                    if (!mQueueViewAnimating) {
                        mQueueViewAnimating = true;
                        if (mQueueView.getVisibility() != View.VISIBLE) {
                            mQueueView.setVisibility(View.VISIBLE);
                            mQueueView.animate().scaleX(1.0F)
                                    .setListener(mAnimatorListener);
                        } else {
                            mQueueView.animate().scaleX(0)
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

                                    });

                        }
                    }
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
            String action = intent.getAction();
            Log.d("action", action);
            if (action.equals(PlaybackService.PLAYSTATE_CHANGED)) {
                setButtonDrawable();
                if (mPlaybackService != null) {
                    if (mPlaybackService.isPlaying()) {
                        mHandler.post(mUpdateSeekBarRunnable);
                    } else {
                        mHandler.removeCallbacks(mUpdateSeekBarRunnable);
                    }
                }

            } else if (action.equals(PlaybackService.META_CHANGED)) {

                updateTrackInfo();
            } else if (action.equals(PlaybackService.POSITION_CHANGED) || action.equals(PlaybackService.ITEM_ADDED) || action.equals(PlaybackService.ORDER_CHANGED)) {
                Log.d("eee", "position_changed");
                updateQueue(action);
            }

        }
    };

    private List<Song> getDefaultPlaylist() {
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

    }

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

        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, MainFragment.newInstance()).commit();
        }
        mSlidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingLayout.setPanelSlideListener(mSlideListener);

        mQuickControls = findViewById(R.id.quick_controls);
        mMenu = findViewById(R.id.menu);

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
        mQueueView.addOnItemTouchListener(new SwipeToDismissListener(this) {

            @Override
            public void onDismiss(int position) {
                mQueueAdapter.removeItem(position);

            }

            @Override
            protected boolean canBeDismissed(int position) {
                return true;
            }
        });

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mServiceBound) {
            mServiceIntent = new Intent(this, PlaybackService.class);
            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            startService(mServiceIntent);
        }

        updateAll();
    }

    private void updateAll() {
        if (mPlaybackService != null) {
            updateQueue();
            updateTrackInfo();
            setButtonDrawable();
            if (mPlaybackService.isPlaying()) {
                mHandler.post(mUpdateSeekBarRunnable);
            }

            if (mSlidingLayout.getPanelState() == PanelState.EXPANDED) {

                View controlsLayout = findViewById(R.id.quick_controls);
                if (controlsLayout.getVisibility() == View.VISIBLE) {
                    controlsLayout.setVisibility(View.INVISIBLE);
                }
            }

            boolean shuffle = mPlaybackService.isShuffleEnabled();
            Log.d("shuffle", "shuffle " + String.valueOf(shuffle));
            ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffle);
            if (shuffle) {
                shuffleButton.setImageResource(R.drawable.ic_shuffle_checked);
            } else {
                shuffleButton.setImageResource(R.drawable.ic_shuffle);

            }

            ImageButton repeatButton = (ImageButton) findViewById(R.id.repeat);
            int mode = mPlaybackService.getRepeatMode();
            if (mode == PlaybackService.NO_REPEAT) {
                repeatButton.setImageResource(R.drawable.ic_repeat);
            } else if (mode == PlaybackService.REPEAT_ALL) {
                repeatButton.setImageResource(R.drawable.ic_repeat_checked);
            } else if (mode == PlaybackService.REPEAT_CURRENT) {
                repeatButton.setImageResource(R.drawable.ic_repeat_one);
            }

            mPlaybackService.setRepeatMode(mode);
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
            case R.id.action_equalizer:
                showEqualizer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEqualizer() {
        Intent i = new Intent(this, EqualizerActivity.class);
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

    private void updateTrackInfo() {
        if (mPlaybackService != null) {

            String title = mPlaybackService.getTrackName();
            String artist = mPlaybackService.getArtistName();
            if (title != null) {
                ((TextView) findViewById(R.id.song_title)).setText(title);
            }
            if (artist != null) {
                ((TextView) findViewById(R.id.song_artist)).setText(artist);
            }

            long albumId = mPlaybackService.getAlbumId();
            ImageView artworkView = (ImageView) findViewById(R.id.artwork);
            ImageUtils.loadArtworkAsync(albumId, artworkView);

            int duration = mPlaybackService.getTrackDuration();
            if (duration != -1) {
                mSeekBar.setMax(duration);
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

        if (action != null && (action.equals(PlaybackService.ITEM_ADDED) || action.equals(PlaybackService.ORDER_CHANGED))) {
            mQueueAdapter.notifyDataSetChanged();
        }


        setQueueSelection(mPlaybackService.getPositionWithinPlayList());


    }

    private void setButtonDrawable() {
        if (mPlaybackService != null) {
            ImageButton button = (ImageButton) findViewById(R.id.play_pause_toggle);
            ImageButton quickButton = (ImageButton) findViewById(R.id.quick_play_pause_toggle);
            if (mPlaybackService.isPlaying()) {
                button.setImageResource(R.drawable.ic_pause_black);
                quickButton.setImageResource(R.drawable.ic_pause_small);
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

    class QueueItemViewHolder extends RecyclerView.ViewHolder {

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
            this.itemView = itemView;

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
            itemView.setOnClickListener(mOnItemClickListener);

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

    public void addToQueue(Song song) {
        if (mPlaybackService != null) {
            mPlaybackService.appendToPlayList(song);
        }

    }

    public void setAsNextTrack(Song song) {
        if (mPlaybackService != null) {
            mPlaybackService.setAsNextTrack(song);
        }

    }

}
