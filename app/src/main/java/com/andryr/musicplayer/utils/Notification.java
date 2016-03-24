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

package com.andryr.musicplayer.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.PlaybackService;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.images.ArtworkCache;
import com.andryr.musicplayer.images.BitmapCache;

/**
 * Created by Andry on 27/01/16.
 */
public class Notification {
    private static final String TAG = Notification.class.getCanonicalName();
    private static int NOTIFY_ID = 32;

    private static boolean sIsServiceForeground = false;

    public static void updateNotification(@NonNull final PlaybackService playbackService) {

        if (!playbackService.hasPlaylist()) {
            removeNotification(playbackService);
            return; // no need to go further since there is nothing to display
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            updateSupportNotification(playbackService);
            return;
        }
        Log.d(TAG, "p " + playbackService.hasPlaylist() + " " + playbackService.getPlayList().size());
        PendingIntent togglePlayIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_TOGGLE), 0);


        PendingIntent nextIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_NEXT),
                0);

        PendingIntent previousIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_PREVIOUS), 0);


        int toggleResId = playbackService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_small;

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                playbackService);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            final RemoteViews contentViews = new RemoteViews(playbackService.getPackageName(),
                    R.layout.notification);
            contentViews.setTextViewText(R.id.song_title, playbackService.getSongTitle());
            contentViews.setTextViewText(R.id.song_artist, playbackService.getArtistName());


            contentViews.setOnClickPendingIntent(R.id.quick_play_pause_toggle,
                    togglePlayIntent);


            contentViews.setOnClickPendingIntent(R.id.quick_next, nextIntent);


            contentViews.setOnClickPendingIntent(R.id.quick_prev, previousIntent);

            PendingIntent stopIntent = PendingIntent.getService(playbackService, 0,
                    new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_STOP),
                    0);

            contentViews.setOnClickPendingIntent(R.id.close, stopIntent);


            contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                    toggleResId);


            builder.setOngoing(true)
                    .setContent(contentViews);

        } else {

            builder.setContentTitle(playbackService.getSongTitle())
                    .setContentText(playbackService.getArtistName());


            builder.addAction(R.drawable.ic_prev_small, "", previousIntent)
                    .addAction(toggleResId, "", togglePlayIntent)
                    .addAction(R.drawable.ic_next_small, "", nextIntent);


        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(playbackService.getMediaSession().getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2));

        }


        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt);

        builder.setShowWhen(false);

        builder.setSmallIcon(R.drawable.ic_stat_note);

        Resources res = playbackService.getResources();

        final int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        final int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

        ArtworkCache artworkCache = ArtworkCache.getInstance();
        Bitmap b = artworkCache.getCachedBitmap(playbackService.getAlbumId(), width, height);
        if (b != null) {
            setBitmapAndBuild(b, playbackService, builder);

        } else {
            ArtworkCache.getInstance().loadBitmap(playbackService.getAlbumId(), width, height, new BitmapCache.Callback() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap) {
                    setBitmapAndBuild(bitmap, playbackService, builder);

                }
            });
        }


    }

    private static void setBitmapAndBuild(Bitmap bitmap, @NonNull PlaybackService playbackService, NotificationCompat.Builder builder) {
        if (bitmap == null) {
            BitmapDrawable d = ((BitmapDrawable) playbackService.getResources().getDrawable(R.drawable.ic_stat_note));
            bitmap = d.getBitmap();
        }
        builder.setLargeIcon(bitmap);


        android.app.Notification notification = builder.build();

        boolean startForeground = playbackService.isPlaying();
        if (startForeground) {
            playbackService.startForeground(NOTIFY_ID, notification);
        } else {
            if (sIsServiceForeground) {
                playbackService.stopForeground(false);
            }
            NotificationManager notificationManager = (NotificationManager) playbackService.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFY_ID, notification);
        }

        sIsServiceForeground = startForeground;


    }


    private static void updateSupportNotification(PlaybackService playbackService) {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                playbackService);

        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt)
                .setOngoing(true)
                .setContentTitle(playbackService.getSongTitle())
                .setContentText(playbackService.getArtistName());


        builder.setSmallIcon(R.drawable.ic_stat_note);


        playbackService.startForeground(NOTIFY_ID, builder.build());
    }


    public static void removeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);
    }
}
