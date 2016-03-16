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
import android.support.v4.app.NotificationCompat;
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
    private static int NOTIFY_ID = 32;

    public static void updateNotification(@NonNull final PlaybackService playbackService) {

        if (!playbackService.hasPlaylist()) {
            return; // no need to go further since there is nothing to display
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            updateSupportNotification(playbackService);
            return;
        }
        PendingIntent togglePlayIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_TOGGLE), 0);


        PendingIntent nextIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_NEXT),
                0);

        PendingIntent previousIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_PREVIOUS), 0);

        boolean preLollipop = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                playbackService);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            final RemoteViews contentViews = new RemoteViews(playbackService.getPackageName(),
                    R.layout.notification);
            contentViews.setTextViewText(R.id.song_title, playbackService.getSongTitle());
            contentViews.setTextViewText(R.id.song_artist, playbackService.getArtistName());

            // ArtworkHelper.loadArtworkAsync(this, getAlbumId(), contentViews, R.id.album_artwork);

            contentViews.setOnClickPendingIntent(R.id.quick_play_pause_toggle,
                    togglePlayIntent);


            contentViews.setOnClickPendingIntent(R.id.quick_next, nextIntent);


            contentViews.setOnClickPendingIntent(R.id.quick_prev, previousIntent);

            PendingIntent stopIntent = PendingIntent.getService(playbackService, 0,
                    new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_STOP),
                    0);
            contentViews.setOnClickPendingIntent(R.id.close, stopIntent);

            if (playbackService.isPlaying()) {


                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_pause);

                // contentView.setContentDescription(R.id.play_pause_toggle,
                // getString(R.string.pause));
            } else {


                contentViews.setImageViewResource(R.id.quick_play_pause_toggle,
                        R.drawable.ic_play_small);

                // contentView.setContentDescription(R.id.play_pause_toggle,
                // getString(R.string.play));

            }
            builder.setOngoing(true)
                    .setContent(contentViews);

        } else {

            builder.setContentTitle(playbackService.getSongTitle())
                    .setContentText(playbackService.getArtistName());

            int toggleResId = playbackService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_small;

            builder.addAction(R.drawable.ic_prev_small, "", previousIntent)
                    .addAction(toggleResId, "", togglePlayIntent)
                    .addAction(R.drawable.ic_next_small, "", nextIntent);


        }

        if(!preLollipop) {
            builder.setVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        }


        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt);


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
        NotificationManager mNotifyManager = (NotificationManager) playbackService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(NOTIFY_ID, notification);


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
}
