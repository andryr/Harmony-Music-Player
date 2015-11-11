package com.andryr.musicplayer.activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.PlaybackService;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.favorites.FavoritesHelper;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.ArtworkHelper;
import com.andryr.musicplayer.utils.NavigationUtils;
import com.andryr.musicplayer.utils.ThemeHelper;
import com.andryr.musicplayer.widgets.DragRecyclerView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlaybackActivity extends BaseActivity {

    private SeekBar mSeekBar;
    private DragRecyclerView mQueueView;
    private View mQueueLayout;

    private PlaybackRequests mPlaybackRequests = new PlaybackRequests();


    private List<Song> mQueue;
    private QueueAdapter mQueueAdapter = new QueueAdapter();

    private boolean mServiceBound;
    private PlaybackService mPlaybackService;
    private boolean mQueueLayoutAnimating = false;
    private long mAnimDuration;
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
                mQueueLayoutAnimating = false;
            }
        }

    };


    private Handler mHandler = new Handler();
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

            PlaybackService.PlaybackBinder binder = (PlaybackService.PlaybackBinder) service;
            mPlaybackService = binder.getService();
            if (mPlaybackService == null || !mPlaybackService.hasPlaylist()) {
                finish();
            }
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
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

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
                updateTrackInfo();
            } else if (action.equals(PlaybackService.QUEUE_CHANGED) || action.equals(PlaybackService.POSITION_CHANGED) || action.equals(PlaybackService.ITEM_ADDED) || action.equals(PlaybackService.ORDER_CHANGED)) {
                Log.d("eee", "position_changed");
                updateQueue(action);
            }

        }
    };
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mPlaybackService == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.play_pause_toggle:

                    mPlaybackService.toggle();

                    break;
                case R.id.prev:
                    mPlaybackService.playPrev(true);

                    break;
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

                case R.id.action_favorite:
                    ImageButton button = (ImageButton) v;
                    long songId = mPlaybackService.getSongId();
                    if (FavoritesHelper.isFavorite(PlaybackActivity.this, songId)) {
                        FavoritesHelper.removeFromFavorites(PlaybackActivity.this, songId);
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                    } else {
                        FavoritesHelper.addFavorite(PlaybackActivity.this, mPlaybackService.getSongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                    }
                    break;


            }

        }
    };
    private Intent mServiceIntent;

    private void toggleQueue() {
        if (!mQueueLayoutAnimating) {
            mQueueLayoutAnimating = true;
            if (mQueueLayout.getVisibility() != View.VISIBLE) {
                mQueueLayout.setVisibility(View.VISIBLE);

                ViewPropertyAnimator.animate(mQueueLayout).setDuration(mAnimDuration).alpha(1.0F).setListener(mAnimatorListener).start();

            } else {
                ViewPropertyAnimator.animate(mQueueLayout).alpha(0.0F)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(
                                    Animator animation) {
                                mAnimatorListener
                                        .onAnimationEnd(animation);
                                mQueueLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationStart(
                                    Animator animation) {
                                mAnimatorListener
                                        .onAnimationStart(animation);
                            }

                        }).setDuration(mAnimDuration).start();

            }
        }
    }

    private String msToText(int msec) {
        return String.format(Locale.getDefault(), "%d:%02d", msec / 60000,
                (msec % 60000) / 1000);
    }

    private void updateSeekBar() {
        if (mPlaybackService != null) {
            int position = mPlaybackService.getPlayerPosition();
            mSeekBar.setProgress(position);

            ((TextView) findViewById(R.id.current_position))
                    .setText(msToText(position));
        }
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

    private void setQueueSelection(int position) {
        mQueueAdapter.setSelection(position);

        if (position >= 0 && position < mQueue.size()) {
            mQueueView.scrollToPosition(position);
        }


    }

    private void setButtonDrawable() {
        if (mPlaybackService != null) {
            FloatingActionButton button = (FloatingActionButton) findViewById(R.id.play_pause_toggle);
            if (mPlaybackService.isPlaying()) {
                button.setImageResource(R.drawable.ic_pause_black);
            } else {
                button.setImageResource(R.drawable.ic_play_black);
            }
        }

    }

    private void updateTrackInfo() {
        if (mPlaybackService != null) {

            String title = mPlaybackService.getSongTitle();
            String artist = mPlaybackService.getArtistName();
            if (title != null) {
                ((TextView) findViewById(R.id.song_title)).setText(title);

            }
            if (artist != null) {
                ((TextView) findViewById(R.id.song_artist)).setText(artist);

            }

            long albumId = mPlaybackService.getAlbumId();
            ImageView artworkView = (ImageView) findViewById(R.id.artwork);
            ArtworkHelper.loadArtwork(albumId, true, artworkView);

            int duration = mPlaybackService.getTrackDuration();
            if (duration != -1) {
                mSeekBar.setMax(duration);
                ((TextView) findViewById(R.id.track_duration))
                        .setText(msToText(duration));
                updateSeekBar();
            }

            ImageButton favButton = (ImageButton) findViewById(R.id.action_favorite);
            if (FavoritesHelper.isFavorite(this, mPlaybackService.getSongId())) {
                favButton.setImageResource(R.drawable.ic_action_favorite);
            } else {
                favButton.setImageResource(R.drawable.ic_action_favorite_outline);
            }

            setQueueSelection(mPlaybackService.getPositionWithinPlayList());

        }
    }

    private void updateAll() {
        if (mPlaybackService != null) {
            Log.d("playlist", "hasplaylist " + mPlaybackService.hasPlaylist());
            updateQueue();
            updateTrackInfo();
            setButtonDrawable();
            if (mPlaybackService.isPlaying()) {
                mHandler.post(mUpdateSeekBarRunnable);
            }


            updateShuffleButton();
            updateRepeatButton();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        mAnimDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        mQueueLayout = findViewById(R.id.queue_layout);
        ViewHelper.setAlpha(mQueueLayout, 0.0F);
        mQueueView = (DragRecyclerView) findViewById(R.id.queue_view);

        mQueueView.setLayoutManager(new LinearLayoutManager(this));
        mQueueAdapter = new QueueAdapter();
        mQueueView.setOnItemMovedListener(new DragRecyclerView.OnItemMovedListener() {
            @Override
            public void onItemMoved(int oldPosition, int newPosition) {
                mQueueAdapter.moveItem(oldPosition, newPosition);
            }
        });
        mQueueView.setAdapter(mQueueAdapter);

        findViewById(R.id.prev).setOnClickListener(mOnClickListener);
        findViewById(R.id.next).setOnClickListener(mOnClickListener);
        findViewById(R.id.play_pause_toggle).setOnClickListener(
                mOnClickListener);
        findViewById(R.id.shuffle).setOnClickListener(mOnClickListener);
        findViewById(R.id.repeat).setOnClickListener(mOnClickListener);
        findViewById(R.id.action_favorite).setOnClickListener(mOnClickListener);


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
    protected void onStart() {
        super.onStart();


        // if (!mServiceBound) {
        // mServiceIntent = new Intent(this, PlaybackService.class);
        // bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        // startService(mServiceIntent);
        // }

    }

    @Override
    protected void onStop() {


        super.onStop();
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mServiceListener);
        mPlaybackService = null;

        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        mHandler.removeCallbacks(mUpdateSeekBarRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
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
                NavigationUtils.showMainActivity(this, true);
                return true;
            case R.id.action_equalizer:
                NavigationUtils.showEqualizer(this);
                return true;
            case R.id.action_view_queue:
                toggleQueue();
                return true;
            case R.id.action_settings:
                NavigationUtils.showPreferencesActivity(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateShuffleButton() {
        boolean shuffle = mPlaybackService.isShuffleEnabled();
        Log.d("shuffle", "shuffle " + String.valueOf(shuffle));
        ImageButton shuffleButton = (ImageButton) findViewById(R.id.shuffle);
        if (shuffle) {
            shuffleButton.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);

        } else {
            shuffleButton.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.controlsTint), PorterDuff.Mode.SRC_ATOP);

        }


    }

    private void updateRepeatButton() {
        ImageButton repeatButton = (ImageButton) findViewById(R.id.repeat);
        int mode = mPlaybackService.getRepeatMode();
        if (mode == PlaybackService.NO_REPEAT) {
            repeatButton.setImageResource(R.drawable.ic_repeat);
            repeatButton.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.controlsTint), PorterDuff.Mode.SRC_ATOP);
        } else if (mode == PlaybackService.REPEAT_ALL) {
            repeatButton.setImageResource(R.drawable.ic_repeat);
            repeatButton.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
        } else if (mode == PlaybackService.REPEAT_CURRENT) {
            repeatButton.setImageResource(R.drawable.ic_repeat_one);
            repeatButton.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);

        }
    }


    class QueueItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

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
            vReorderButton.setOnTouchListener(this);
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

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mQueueView.startDrag(itemView);
            return false;
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

    @Override
    public void onBackPressed() {
        NavigationUtils.showMainActivity(this, true);
    }
}
