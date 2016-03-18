package org.oucho.musicplayer.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.oucho.musicplayer.DataBase.FavoritesHelper;
import org.oucho.musicplayer.PlaybackService;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.images.ArtworkCache;
import org.oucho.musicplayer.model.Song;
import org.oucho.musicplayer.utils.NavigationUtils;
import org.oucho.musicplayer.utils.ThemeHelper;
import org.oucho.musicplayer.widgets.DragRecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlaybackActivity extends BaseActivity {

    private SeekBar mSeekBar;
    private DragRecyclerView mQueueView;
    private View mQueueLayout;

    private final PlaybackRequests mPlaybackRequests = new PlaybackRequests();


    private List<Song> mQueue;
    private QueueAdapter mQueueAdapter = new QueueAdapter();

    private boolean mServiceBound;
    private PlaybackService mPlaybackService;

    private final Handler mHandler = new Handler();

Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String couleur = ThemeHelper.getColor(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#" + couleur + "'>Lecture en cours</font>"));
        actionBar.setElevation(0);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_playback);

        mArtworkSize = getResources().getDimensionPixelSize(R.dimen.playback_activity_art_size);

        ImageView button_next = (ImageView) findViewById(R.id.next);
        ImageView button_prev = (ImageView) findViewById(R.id.prev);

        button_next.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
        button_prev.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);


        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_black_24dp);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);


        mQueueLayout = findViewById(R.id.queue_layout);

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
        findViewById(R.id.play_pause_toggle).setOnClickListener(mOnClickListener);
        findViewById(R.id.shuffle).setOnClickListener(mOnClickListener);
        findViewById(R.id.repeat).setOnClickListener(mOnClickListener);
        findViewById(R.id.action_favorite).setOnClickListener(mOnClickListener);


        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

    }



    private final Runnable mUpdateSeekBarRunnable = new Runnable() {

        @Override
        public void run() {

            updateSeekBar();

            mHandler.postDelayed(mUpdateSeekBarRunnable, 1000);
        }
    };

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser
                    && mPlaybackService != null
                    && (mPlaybackService.isPlaying() || mPlaybackService
                    .isPaused())) {
                mPlaybackService.seekTo(seekBar.getProgress());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(mUpdateSeekBarRunnable);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mPlaybackService != null && mPlaybackService.isPlaying()) {
                mHandler.post(mUpdateSeekBarRunnable);
            }

        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

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
                    ImageView button = (ImageView) v;
                    long songId = mPlaybackService.getSongId();
                    if (FavoritesHelper.isFavorite(PlaybackActivity.this, songId)) {
                        FavoritesHelper.removeFromFavorites(PlaybackActivity.this, songId);
                        button.setImageResource(R.drawable.musicplayer_like_disable);
                    } else {
                        FavoritesHelper.addFavorite(PlaybackActivity.this, mPlaybackService.getSongId());
                        button.setImageResource(R.drawable.musicplayer_like);
                    }
                    break;
            }
        }
    };

    private int mArtworkSize;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;

        }
    };

    private final BroadcastReceiver mServiceListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPlaybackService == null) {
                return;
            }
            String action = intent.getAction();
            Log.d("action", action);
            switch (action) {
                case PlaybackService.PLAYSTATE_CHANGED:
                    setButtonDrawable();
                    if (mPlaybackService.isPlaying()) {
                        mHandler.post(mUpdateSeekBarRunnable);
                    } else {
                        mHandler.removeCallbacks(mUpdateSeekBarRunnable);
                    }


                    break;
                case PlaybackService.META_CHANGED:
                    updateTrackInfo();
                    break;
                case PlaybackService.QUEUE_CHANGED:
                case PlaybackService.POSITION_CHANGED:
                case PlaybackService.ITEM_ADDED:
                case PlaybackService.ORDER_CHANGED:
                    Log.d("eee", "position_changed");
                    updateQueue(action);
                    break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    private void toggleQueue() {
        if (mQueueLayout.getVisibility() != View.VISIBLE) {
            mQueueLayout.setVisibility(View.VISIBLE);
        } else {
            mQueueLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mQueueLayout.getVisibility() == View.VISIBLE) {
            mQueueLayout.setVisibility(View.GONE);
        } else {
            NavigationUtils.showMainActivity(this, true);
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
    protected void onResume() {
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
        super.onResume();
        if (!mServiceBound) {
            Intent mServiceIntent = new Intent(this, PlaybackService.class);
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

    private void updateQueue() {
        updateQueue(null);
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

            //long albumId = mPlaybackService.getAlbumId();
            ImageView artworkView = (ImageView) findViewById(R.id.artwork);
            ArtworkCache.getInstance().loadBitmap(mPlaybackService.getAlbumId(), artworkView, mArtworkSize, mArtworkSize);

            int duration = mPlaybackService.getTrackDuration();
            if (duration != -1) {
                mSeekBar.setMax(duration);
                ((TextView) findViewById(R.id.track_duration))
                        .setText(msToText(duration));
                updateSeekBar();
            }

            ImageView favButton = (ImageView) findViewById(R.id.action_favorite);

            if (FavoritesHelper.isFavorite(this, mPlaybackService.getSongId())) {
                favButton.setImageResource(R.drawable.musicplayer_like);
            } else {
                favButton.setImageResource(R.drawable.musicplayer_like_disable);
            }

            setQueueSelection(mPlaybackService.getPositionWithinPlayList());

        }
    }

    private void setButtonDrawable() {
        if (mPlaybackService != null) {
            ImageView button = (ImageView) findViewById(R.id.play_pause_toggle);
            if (mPlaybackService.isPlaying()) {
                button.setImageResource(R.drawable.musique_pause);
                button.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
            } else {
                button.setImageResource(R.drawable.musique_play);
                button.setColorFilter(ThemeHelper.getStyleColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP);
            }
        }

    }

    private void updateShuffleButton() {
        boolean shuffle = mPlaybackService.isShuffleEnabled();
        Log.d("shuffle", "shuffle " + String.valueOf(shuffle));
        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffle);
        if (shuffle) {
            shuffleButton.setImageResource(R.drawable.musique_shuffle_on);
            shuffleButton.setColorFilter(ThemeHelper.getStyleColor(this, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);

        } else {
            shuffleButton.setImageResource(R.drawable.musique_shuffle);
            shuffleButton.setColorFilter(ThemeHelper.getStyleColor(this, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);

        }
    }

    private void updateRepeatButton() {
        ImageView repeatButton = (ImageView) findViewById(R.id.repeat);
        int mode = mPlaybackService.getRepeatMode();
        if (mode == PlaybackService.NO_REPEAT) {
            repeatButton.setImageResource(R.drawable.musique_repeat_no);
            repeatButton.setColorFilter(ThemeHelper.getStyleColor(this, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);
        } else if (mode == PlaybackService.REPEAT_ALL) {
            repeatButton.setImageResource(R.drawable.musique_repeat);
            repeatButton.setColorFilter(ThemeHelper.getStyleColor(this, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);
        } else if (mode == PlaybackService.REPEAT_CURRENT) {
            repeatButton.setImageResource(R.drawable.musique_repeat_once);
            repeatButton.setColorFilter(ThemeHelper.getStyleColor(this, R.color.controls_tint_light), PorterDuff.Mode.SRC_ATOP);

        }
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

        mQueueAdapter.notifyDataSetChanged();


        setQueueSelection(mPlaybackService.getPositionWithinPlayList());
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

    private void setQueueSelection(int position) {
        mQueueAdapter.setSelection(position);

        if (position >= 0 && position < mQueue.size()) {
            mQueueView.scrollToPosition(position);
        }
    }

    class QueueItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

        final TextView vTitle;
        final TextView vArtist;
        final ImageButton vReorderButton;
        final View itemView;

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
        public boolean onTouch(View v, MotionEvent event) {
            mQueueView.startDrag(itemView);
            return false;
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
        public QueueItemViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.queue_item, parent, false);


            return new QueueItemViewHolder(itemView);
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
        public int getItemCount() {
            if (mQueue == null) {
                return 0;
            }
            return mQueue.size();
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
