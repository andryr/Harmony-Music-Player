package com.andryr.musicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.andryr.musicplayer.audiofx.AudioEffectsReceiver;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.utils.ArtworkHelper;

import org.acra.ACRA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO déplacer certaines méthodes dans d'autres classes (égaliseur, mediaplayer, etc.)

public class PlaybackService extends Service implements OnPreparedListener,
        OnErrorListener, OnCompletionListener {


    public static final String PREF_AUTO_PAUSE = "com.andryr.musicplayer.AUTO_PAUSE";//pause automatique quand on détecte un appel entrant


    public static final String ACTION_PLAY = "com.andryr.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.andryr.musicplayer.ACTION_PAUSE";
    public static final String ACTION_RESUME = "com.andryr.musicplayer.ACTION_RESUME";
    public static final String ACTION_TOGGLE = "com.andryr.musicplayer.ACTION_TOGGLE";
    public static final String ACTION_NEXT = "com.andryr.musicplayer.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "com.andryr.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "com.andryr.musicplayer.ACTION_STOP";
    public static final String ACTION_CHOOSE_SONG = "com.andryr.musicplayer.ACTION_CHOOSE_SONG";
    public static final String META_CHANGED = "com.andryr.musicplayer.META_CHANGED";
    public static final String PLAYSTATE_CHANGED = "com.andryr.musicplayer.PLAYSTATE_CHANGED";
    public static final String QUEUE_CHANGED = "com.andryr.musicplayer.QUEUE_CHANGED";
    public static final String POSITION_CHANGED = "com.andryr.musicplayer.POSITION_CHANGED";
    public static final String ITEM_ADDED = "com.andryr.musicplayer.ITEM_ADDED";
    public static final String ORDER_CHANGED = "com.andryr.musicplayer.ORDER_CHANGED";
    public static final String EXTRA_POSITION = "com.andryr.musicplayer.POSITION";
    public static final int NO_REPEAT = 20;
    public static final int REPEAT_ALL = 21;
    public static final int REPEAT_CURRENT = 22;
    private static final String TAG = "PlaybackService";
    private static final int IDLE_DELAY = 60000;
    private static int NOTIFY_ID = 32;

    private PlaybackBinder mBinder = new PlaybackBinder();
    private MediaPlayer mMediaPlayer;

    private List<Song> mOriginalSongList;
    private List<Song> mPlayList = new ArrayList<>();
    private Song mCurrentSong;


    private boolean mIsPlaying = false;

    private boolean mIsPaused = false;

    private boolean mHasPlaylist = false;

    private boolean mShuffle = false;

    private int mStartId;

    private int mRepeatMode = NO_REPEAT;

    private int mCurrentPosition;

    private boolean mBound = false;

    private boolean mAutoPause = false;

    //
    private boolean mPlayImmediately = false;

    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isPlaying() || mBound) {
                return;
            }

            stopSelf(mStartId);
        }
    };

    private BroadcastReceiver mHeadsetStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                boolean plugged = intent.getIntExtra("state", 0) == 1;
                if (!plugged) {
                    pause();
                }
            }

        }
    };

    private TelephonyManager mTelephonyManager;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    pause();
                    break;
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        Intent i = new Intent(this, AudioEffectsReceiver.class);
        i.setAction(AudioEffectsReceiver.ACTION_OPEN_AUDIO_EFFECT_SESSION);
        i.putExtra(AudioEffectsReceiver.EXTRA_AUDIO_SESSION_ID, mMediaPlayer.getAudioSessionId());
        sendBroadcast(i);

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetStateReceiver, receiverFilter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAutoPause = prefs.getBoolean(PREF_AUTO_PAUSE, false);

        initTelephony();

    }

    private void initTelephony() {
        if(mAutoPause) {
            mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    public void setAutoPauseEnabled(boolean enable) {
        if(enable == !mAutoPause) {
            mAutoPause = enable;

            if(enable) {
                initTelephony();
            }
            //si !enable on a rien à faire à priori

        }
    }

    public Song getCurrentSong()
    {
        return mCurrentSong;
    }

    public long getSongId()
    {
        if(mCurrentSong != null)
        {
            return mCurrentSong.getId();
        }
        return -1;
    }

    public String getSongTitle() {
        if (mCurrentSong != null) {
            return mCurrentSong.getTitle();
        }
        return null;
    }

    public String getArtistName() {
        if (mCurrentSong != null) {
            return mCurrentSong.getArtist();
        }
        return null;
    }

    public String getAlbumName() {
        if (mCurrentSong != null) {
            return mCurrentSong.getAlbum();
        }
        return null;
    }

    public long getAlbumId() {
        if (mCurrentSong != null) {
            return mCurrentSong.getAlbumId();
        }
        return -1;
    }

    public List<Song> getPlayList() {
        return mPlayList;
    }

    public int getPositionWithinPlayList() {
        if (mPlayList != null) {
            return mPlayList.indexOf(mCurrentSong);
        }
        return -1;
    }

    public void setPlayList(List<Song> songList, int position, boolean play) {

        setPlayListInternal(songList);

        setPosition(position, play);
        if (mShuffle) {
            shuffle();
        }

    }

    private void setPlayListInternal(List<Song> songList) {
        if (songList == null || songList.size() <= 0) {
            return;
        }
        mOriginalSongList = songList;
        mPlayList.clear();
        mPlayList.addAll(mOriginalSongList);
        mHasPlaylist = true;
    }

    public void setPlayListAndShuffle(List<Song> songList, boolean play) {
        setPlayListInternal(songList);
        mCurrentSong = null;
        mShuffle = true;
        shuffle();
        setPosition(0, play);
    }

    public void addToQueue(Song song) {
        if (mPlayList != null) {
            mOriginalSongList.add(song);
            mPlayList.add(song);
            sendBroadcast(ITEM_ADDED);

        }
    }

    public void setAsNextTrack(Song song) {
        if (mPlayList != null) {
            mOriginalSongList.add(song);
            int currentPos = mCurrentPosition;
            mPlayList.add(currentPos + 1, song);
            sendBroadcast(ITEM_ADDED);

        }
    }

    public void setPosition(int position, boolean play) {
        mCurrentPosition = position;
        Song song = mPlayList.get(position);
        if (song != mCurrentSong) {
            mCurrentSong = song;
            if (play) {
                openAndPlay();
            } else {
                open();
            }
        }
    }

    public boolean hasPlaylist() {
        return mHasPlaylist;
    }

    public int getTrackDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getPlayerPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void seekTo(int msec) {
        mMediaPlayer.seekTo(msec);
    }

    private void open() {

        // Intent i = new Intent(META_CHANGED);
        // sendStickyBroadcast(i);

        Bundle extras = new Bundle();
        extras.putInt(EXTRA_POSITION, getPositionWithinPlayList());
        sendBroadcast(POSITION_CHANGED, extras);

        mMediaPlayer.reset();

        Uri songUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mCurrentSong.getId());

        try {
            mMediaPlayer.setDataSource(getApplicationContext(), songUri);
            mMediaPlayer.prepareAsync();

        } catch (IllegalArgumentException | SecurityException
                | IllegalStateException | IOException e) {
            ACRA.getErrorReporter().handleException(e);
            Log.e("ee", "ee", e);
        }

    }

    private void openAndPlay() {

        mPlayImmediately = true;

        open();
    }

    private void updateCurrentPosition() {
        int pos = mPlayList.indexOf(mCurrentSong);
        if (pos != -1) {
            mCurrentPosition = pos;
        }
    }

    private int getNextPosition(boolean force) {

        updateCurrentPosition();
        int position = mCurrentPosition;
        if (mRepeatMode == REPEAT_CURRENT && !force) {
            return position;
        }


        if (position + 1 >= mPlayList.size()) {
            if (mRepeatMode == REPEAT_ALL) {
                return 0;
            }
            return -1;// NO_REPEAT;

        }
        return position + 1;


    }

    private int getPreviousPosition(boolean force) {

        updateCurrentPosition();
        int position = mCurrentPosition;


        if ((mRepeatMode == REPEAT_CURRENT && !force) || (isPlaying() && getPlayerPosition() >= 1500)) {
            return position;
        }


        if (position - 1 < 0) {
            if (mRepeatMode == REPEAT_ALL) {
                return mPlayList.size() - 1;
            }
            return -1;// NO_REPEAT;

        }
        return position - 1;

    }

    public int getNextRepeatMode() {
        return 20 + (mRepeatMode + 1) % 20 % 3;
    }

    public void play() {
        mMediaPlayer.start();
        mIsPlaying = true;
        mIsPaused = false;
        sendBroadcast(PLAYSTATE_CHANGED);
        updateNotification();

    }

    public void pause() {
        mMediaPlayer.pause();
        mIsPlaying = false;
        mIsPaused = true;
        sendBroadcast(PLAYSTATE_CHANGED);
        updateNotification();
    }

    public void resume() {
        play();

    }

    public void toggle() {
        if (mMediaPlayer.isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public void stop() {
        mMediaPlayer.stop();

        mIsPlaying = false;
        sendBroadcast(PLAYSTATE_CHANGED);
    }

    public void playNext(boolean force) {
        int position = getNextPosition(force);
        Log.e("pos", String.valueOf(position));
        if (position >= 0 && position < mPlayList.size()) {
            mCurrentPosition = position;
            mCurrentSong = mPlayList.get(position);
            openAndPlay();
        }

    }

    public void playPrev(boolean force) {
        int position = getPreviousPosition(force);
        Log.e("pos", String.valueOf(position));

        if (position >= 0 && position < mPlayList.size()) {
            mCurrentPosition = position;
            mCurrentSong = mPlayList.get(position);
            openAndPlay();
        }
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(int mode) {
        mRepeatMode = mode;
    }

    public boolean isShuffleEnabled() {
        return mShuffle;
    }

    public void setShuffleEnabled(boolean enable) {

        if (mShuffle != enable) {

            mShuffle = enable;
            if (enable) {
                shuffle();
            } else {
                mPlayList.clear();
                mPlayList.addAll(mOriginalSongList);
            }

            //on met à jour la position
            updateCurrentPosition();


            sendBroadcast(ORDER_CHANGED);

        }
    }

    public void shuffle() {
        boolean b = mPlayList.remove(mCurrentSong);
        Collections.shuffle(mPlayList);
        if (b) {
            mPlayList.add(0, mCurrentSong);
        }
    }

    private void sendBroadcast(String action, Bundle data) {
        Log.d("action", action + "2");
        Intent i = new Intent(action);
        if (data != null) {
            i.putExtras(data);
        }
        sendStickyBroadcast(i);
        refreshAppWidgets();

    }

    private void refreshAppWidgets() {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(this, PlaybackWidget.class));
        PlaybackWidget.updateAppWidget(this, appWidgetIds);
    }

    private void sendBroadcast(String action) {
        sendBroadcast(action, null);
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBound = false;
        if (mMediaPlayer.isPlaying()) {
            return true;
        }

        if (mPlayList.size() > 0) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }

        stopSelf(mStartId);
        return true;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mHeadsetStateReceiver);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        mMediaPlayer.stop();
        Intent i = new Intent(this, AudioEffectsReceiver.class);
        i.setAction(AudioEffectsReceiver.ACTION_CLOSE_AUDIO_EFFECT_SESSION);
        sendBroadcast(i);
        mMediaPlayer.release();

        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // mp.stop();

        Log.d(TAG, "onCompletion");
        playNext(false);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG,
                "onError " + String.valueOf(what) + " " + String.valueOf(extra));

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        sendBroadcast(META_CHANGED);
        if (mPlayImmediately) {
            play();
            mPlayImmediately = false;
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (mPlayList.size() == 0 || action.equals(ACTION_CHOOSE_SONG)) {
                    startMainActivity();
                } else if (action.equals(ACTION_TOGGLE)) {
                    toggle();
                } else if (action.equals(ACTION_STOP)) {
                    if (!mBound) {
                        stopSelf(mStartId);
                    }
                } else if (action.equals(ACTION_NEXT)) {
                    playNext(true);
                } else if (action.equals(ACTION_PREVIOUS)) {
                    playPrev(true);
                }
            }

        }
        return START_STICKY;
    }

    private void startMainActivity() {
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    private void updateNotification() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            updateSupportNotification();
            return;
        }
        RemoteViews contentViews = new RemoteViews(getPackageName(),
                R.layout.notification);
        contentViews.setTextViewText(R.id.song_title, getSongTitle());
        contentViews.setTextViewText(R.id.song_artist, getArtistName());

        // ArtworkHelper.loadArtworkAsync(this, getAlbumId(), contentViews, R.id.album_artwork);
        PendingIntent togglePlayIntent = PendingIntent.getService(this, 0,
                new Intent(this, PlaybackService.class)
                        .setAction(ACTION_TOGGLE), 0);
        contentViews.setOnClickPendingIntent(R.id.quick_play_pause_toggle,
                togglePlayIntent);

        PendingIntent nextIntent = PendingIntent.getService(this, 0,
                new Intent(this, PlaybackService.class).setAction(ACTION_NEXT),
                0);
        contentViews.setOnClickPendingIntent(R.id.quick_next, nextIntent);

        PendingIntent previousIntent = PendingIntent.getService(this, 0,
                new Intent(this, PlaybackService.class)
                        .setAction(ACTION_PREVIOUS), 0);
        contentViews.setOnClickPendingIntent(R.id.quick_prev, previousIntent);

        PendingIntent stopIntent = PendingIntent.getService(this, 0,
                new Intent(this, PlaybackService.class).setAction(ACTION_STOP),
                0);
        contentViews.setOnClickPendingIntent(R.id.close, stopIntent);

        if (isPlaying()) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_pause);
            } else {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_pause_black);
            }
            // contentView.setContentDescription(R.id.play_pause_toggle,
            // getString(R.string.pause));
        } else {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_play_small);
            } else {
                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_play_black);
            }
            // contentView.setContentDescription(R.id.play_pause_toggle,
            // getString(R.string.play));

        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt)
                .setOngoing(true).setContent(contentViews);

        builder.setSmallIcon(R.drawable.ic_stat_note);


        Bitmap icon = ArtworkHelper.getArtworkBitmap(this, getAlbumId());
        Log.d("eeaa", "bool1 : " + (icon == null));
        if (icon != null) {
            Resources res = getResources();
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
            icon = Bitmap.createScaledBitmap(icon, width, height, false);

            builder.setLargeIcon(icon);
        } else {
            BitmapDrawable d = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_stat_note));
            builder.setLargeIcon(d.getBitmap());
        }

        startForeground(NOTIFY_ID, builder.build());
    }

    private void updateSupportNotification() {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt)
                .setOngoing(true)
                .setContentTitle(getSongTitle())
                .setContentText(getArtistName());


        builder.setSmallIcon(R.drawable.ic_stat_note);


        startForeground(NOTIFY_ID, builder.build());
    }


    public class PlaybackBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }


}
