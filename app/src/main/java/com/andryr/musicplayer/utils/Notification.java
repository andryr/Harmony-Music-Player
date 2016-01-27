package com.andryr.musicplayer.utils;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.PlaybackService;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.images.ArtworkCache;

/**
 * Created by Andry on 27/01/16.
 */
public class Notification {
    private static int NOTIFY_ID = 32;

    public static void updateNotification(PlaybackService playbackService) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            updateSupportNotification(playbackService);
            return;
        }
        RemoteViews contentViews = new RemoteViews(playbackService.getPackageName(),
                R.layout.notification);
        contentViews.setTextViewText(R.id.song_title, playbackService.getSongTitle());
        contentViews.setTextViewText(R.id.song_artist, playbackService.getArtistName());

        // ArtworkHelper.loadArtworkAsync(this, getAlbumId(), contentViews, R.id.album_artwork);
        PendingIntent togglePlayIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_TOGGLE), 0);
        contentViews.setOnClickPendingIntent(R.id.quick_play_pause_toggle,
                togglePlayIntent);

        PendingIntent nextIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_NEXT),
                0);
        contentViews.setOnClickPendingIntent(R.id.quick_next, nextIntent);

        PendingIntent previousIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_PREVIOUS), 0);
        contentViews.setOnClickPendingIntent(R.id.quick_prev, previousIntent);

        PendingIntent stopIntent = PendingIntent.getService(playbackService, 0,
                new Intent(playbackService, PlaybackService.class).setAction(PlaybackService.ACTION_STOP),
                0);
        contentViews.setOnClickPendingIntent(R.id.close, stopIntent);

        if (playbackService.isPlaying()) {

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
                playbackService);

        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt)
                .setOngoing(true).setContent(contentViews);

        builder.setSmallIcon(R.drawable.ic_stat_note);

        int thumbSize = playbackService.getResources().getDimensionPixelSize(R.dimen.art_thumbnail_size);

        Bitmap icon = ArtworkCache.getInstance().getBitmap(playbackService.getAlbumId(), thumbSize, thumbSize);
        if (icon != null) {
            Resources res = playbackService.getResources();
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
            icon = Bitmap.createScaledBitmap(icon, width, height, false);

            builder.setLargeIcon(icon);
        } else {
            BitmapDrawable d = ((BitmapDrawable) playbackService.getResources().getDrawable(R.drawable.ic_stat_note));
            builder.setLargeIcon(d.getBitmap());
        }

        playbackService.startForeground(NOTIFY_ID, builder.build());
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
