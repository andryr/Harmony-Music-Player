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
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.andryr.musicplayer.audiofx.AudioEffectsReceiver;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.model.Song;
import com.andryr.musicplayer.model.db.queue.QueueDbHelper;
import com.andryr.musicplayer.utils.Notification;
import com.andryr.musicplayer.utils.Utils;

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
    public static final String REPEAT_MODE_CHANGED = "com.andryr.musicplayer.REPEAT_MODE_CHANGED";
    public static final String EXTRA_POSITION = "com.andryr.musicplayer.POSITION";
    public static final int NO_REPEAT = 20;
    public static final int REPEAT_ALL = 21;
    public static final int REPEAT_CURRENT = 22;
    private static final String TAG = "PlaybackService";
    private static final int IDLE_DELAY = 60000;
    private static final String STATE_PREFS_NAME = "PlaybackState";

    private PlaybackBinder mBinder = new PlaybackBinder();
    private MediaPlayer mMediaPlayer;

    private List<Song> mOriginalSongList = new ArrayList<>();
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
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)&&isPlaying()) {
                boolean plugged = intent.getIntExtra("state", 0) == 1;
                if (!plugged) {
                    pause();
                }
            }

        }
    };

    private SharedPreferences mStatePrefs;

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

    private AudioManager mAudioManager;
    private boolean mPausedByFocusLoss;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (isPlaying()) {
                                pause();
                                mPausedByFocusLoss = true;
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!isPlaying() && mPausedByFocusLoss) {
                                resume();
                                mPausedByFocusLoss = false;
                            }
                            mMediaPlayer.setVolume(1.0f, 1.0f);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            if (isPlaying()) {
                                mMediaPlayer.setVolume(0.1f, 0.1f);
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
                            pause();
                            mPausedByFocusLoss = false;
                            break;
                    }
                }
            };
    private MediaSessionCompat mMediaSession;


    @Override
    public void onCreate() {
        super.onCreate();
        mStatePrefs = getSharedPreferences(STATE_PREFS_NAME, MODE_PRIVATE);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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

        restoreState();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupMediaSession();
        }

    }

    private void saveState(boolean saveQueue) {
        if (mPlayList.size() > 0) {
            SharedPreferences.Editor editor = mStatePrefs.edit();
            editor.putBoolean("stateSaved", true);

            if (saveQueue) {
                QueueDbHelper dbHelper = new QueueDbHelper(this);
                dbHelper.removeAll();
                dbHelper.add(mPlayList);
                dbHelper.close();
            }

            editor.putInt("currentPosition", mCurrentPosition);
            editor.putInt("repeatMode", mRepeatMode);
            editor.putBoolean("shuffle", mShuffle);
            editor.apply();
        }

    }

    private void restoreState() {

        if (Utils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (mStatePrefs.getBoolean("stateSaved", false)) {
                QueueDbHelper dbHelper = new QueueDbHelper(this);
                List<Song> playList = dbHelper.readAll();
                dbHelper.close();



                mRepeatMode = mStatePrefs.getInt("repeatMode", mRepeatMode);

                int position = mStatePrefs.getInt("currentPosition", 0);

                mShuffle = mStatePrefs.getBoolean("shuffle", mShuffle);

                if(playList.size() > 0) {
                    setPlayListInternal(playList);

                    setPosition(position, false);

                    open();
                }





            }
        }
    }

    private void saveSeekPos() {
        Log.d(TAG,"save seek pos");
        SharedPreferences.Editor editor = mStatePrefs.edit();
        editor.putBoolean("seekPosSaved", true);
        editor.putInt("seekPos", mMediaPlayer.getCurrentPosition());
        editor.apply();
    }

    private void restoreSeekPos() {
        if (mStatePrefs.getBoolean("seekPosSaved", false)) {
            int seekPos = mStatePrefs.getInt("seekPos", 0);
            Log.d(TAG, "restore seek pos "+seekPos+"ms");
            seekTo(seekPos);

            SharedPreferences.Editor editor = mStatePrefs.edit();
            editor.putBoolean("seekPosSaved", false);
            editor.putInt("seekPos", 0);
            editor.apply();
        }
    }

    private void setupMediaSession() {
        mMediaSession = new MediaSessionCompat(this, TAG);
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                playNext(true);
            }

            @Override
            public void onSkipToPrevious() {
                playPrev(true);
            }

            @Override
            public void onStop() {
                pause();
            }

            @Override
            public void onSeekTo(long pos) {
                seekTo((int) pos);
            }
        });
    }

    private void initTelephony() {
        if (mAutoPause) {
            mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
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
                } else if (action.equals(ACTION_PAUSE)) {
                    pause();
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


    @Override
    public void onDestroy() {


        if (mMediaSession != null) {
            mMediaSession.release();
        }

        unregisterReceiver(mHeadsetStateReceiver);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        saveSeekPos();

        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);

        mMediaPlayer.stop();
        Intent i = new Intent(this, AudioEffectsReceiver.class);
        i.setAction(AudioEffectsReceiver.ACTION_CLOSE_AUDIO_EFFECT_SESSION);
        sendBroadcast(i);
        mMediaPlayer.release();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBound = false;
        if (isPlaying()) {
            return true;
        }

        if(isPaused()) {
            saveSeekPos();
        }

        if (mPlayList.size() > 0) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }

        stopSelf(mStartId);
        return true;
    }

    public void setAutoPauseEnabled(boolean enable) {
        if (enable == !mAutoPause) {
            mAutoPause = enable;

            if (enable) {
                initTelephony();
            }
            //si !enable on a rien à faire à priori

        }
    }

    public Song getCurrentSong() {
        return mCurrentSong;
    }

    public long getSongId() {
        if (mCurrentSong != null) {
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

    public void setPlayList(List<Song> songList, int position, boolean play) {

        setPlayListInternal(songList);

        setPosition(position, play);
        if (mShuffle) {
            shuffle();
        }
        notifyChange(QUEUE_CHANGED);


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
        notifyChange(QUEUE_CHANGED);
        if (play) {
            play();
        }

    }

    public void addToQueue(Song song) {
        if (mPlayList != null) {
            mOriginalSongList.add(song);
            mPlayList.add(song);
            notifyChange(ITEM_ADDED);

        }
    }

    private void notifyChange(String what) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateMediaSession(what);
        }
        saveState(QUEUE_CHANGED.equals(what) || ITEM_ADDED.equals(what) || ORDER_CHANGED.equals(what));

        if (PLAYSTATE_CHANGED.equals(what) || META_CHANGED.equals(what)) {
            Notification.updateNotification(this);

            // notify third party applications
            Intent intent = new Intent();
            intent.setAction("com.android.music." +(PLAYSTATE_CHANGED.equals(what) ? "playstatechanged" : "metachanged"));
            Bundle bundle = new Bundle();

            // put the song's metadata
            bundle.putString("track", getSongTitle());
            bundle.putString("artist", getArtistName());
            bundle.putString("album", getAlbumName());

            // put the song's total duration (in ms)
            bundle.putLong("duration", getTrackDuration());

            // put the song's current position
            bundle.putLong("position", getPlayerPosition());

            // put the playback status
            bundle.putBoolean("playing", isPlaying()); // currently playing

            // put your application's package
            bundle.putString("scrobbling_source", getPackageName());

            intent.putExtras(bundle);
            sendBroadcast(intent);

        }
        sendBroadcast(what, null);
    }



    private void updateMediaSession(String what) {

        if (!mMediaSession.isActive()) {
            mMediaSession.setActive(true);
        }

        if (what.equals(PLAYSTATE_CHANGED)) {

            int playState = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(playState, getPlayerPosition(), 1.0F)
                    .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build());
        }

        if (what.equals(META_CHANGED)) {
            int largeArtSize = (int) getResources().getDimension(R.dimen.art_size);
            Bitmap artwork = ArtworkCache.getInstance().getCachedBitmap(getAlbumId(), largeArtSize, largeArtSize);

            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtistName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getAlbumName())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getSongTitle())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getTrackDuration())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork);
            mMediaSession.setMetadata(builder.build());

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

    public void setAsNextTrack(Song song) {
        if (mPlayList != null) {
            mOriginalSongList.add(song);
            int currentPos = mCurrentPosition;
            mPlayList.add(currentPos + 1, song);
            notifyChange(ITEM_ADDED);

        }
    }

    public void setPosition(int position, boolean play) {
        if (position >= mPlayList.size()) {
            return;
        }
        mCurrentPosition = position;
        Song song = mPlayList.get(position);
        if (song != mCurrentSong) {
            mCurrentSong = song;
            Log.d(TAG, "current song " + mCurrentSong.getTitle());

            if (play) {
                openAndPlay();
            } else {
                open();
            }
        } else if (play) {
            play();
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
        switch (mRepeatMode) {
            case NO_REPEAT:
                return REPEAT_ALL;
            case REPEAT_ALL:
                return REPEAT_CURRENT;
            case REPEAT_CURRENT:
                return NO_REPEAT;
        }
        return NO_REPEAT;
    }

    public void play() {
        int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer.start();
            mIsPlaying = true;
            mIsPaused = false;
            notifyChange(PLAYSTATE_CHANGED);
        }

    }

    public void pause() {
        mMediaPlayer.pause();
        mIsPlaying = false;
        mIsPaused = true;
        notifyChange(PLAYSTATE_CHANGED);
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
        notifyChange(PLAYSTATE_CHANGED);
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
        notifyChange(REPEAT_MODE_CHANGED);
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


            notifyChange(ORDER_CHANGED);

        }
    }

    private void shuffle() {
        boolean b = mPlayList.remove(mCurrentSong);
        Collections.shuffle(mPlayList);
        if (b) {
            Log.d(TAG, mCurrentSong.getTitle());
            Log.d(TAG, "removed");

            mPlayList.add(0, mCurrentSong);
        }
        setPosition(0, false);
    }

    private void updateCurrentPosition() {
        int pos = mPlayList.indexOf(mCurrentSong);
        if (pos != -1) {
            mCurrentPosition = pos;
        }
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // mp.stop();

        Log.d(TAG, "onCompletion");
        playNext(false);

    }

    public void playNext(boolean force) {
        int position = getNextPosition(force);
        if (position >= 0 && position < mPlayList.size()) {
            mCurrentPosition = position;
            mCurrentSong = mPlayList.get(position);
            openAndPlay();
        }
        else {
            seekTo(0);
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

    private void openAndPlay() {

        mPlayImmediately = true;

        open();
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

    public int getPositionWithinPlayList() {
        if (mPlayList != null) {
            return mPlayList.indexOf(mCurrentSong);
        }
        return -1;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG,
                "onError " + String.valueOf(what) + " " + String.valueOf(extra));

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        notifyChange(META_CHANGED);
        restoreSeekPos();
        if (mPlayImmediately) {
            play();
            mPlayImmediately = false;
        }

    }

    private void startMainActivity() {
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }


    public class PlaybackBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }


}
