package org.oucho.musicplayer.utils;

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

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.PlaybackService;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.images.ArtworkCache;
import org.oucho.musicplayer.images.BitmapCache;

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

        boolean preLollipop = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                playbackService);


        builder.setContentTitle(playbackService.getSongTitle())
                .setContentText(playbackService.getArtistName())
                .setSubText(playbackService.getAlbumName());

        int toggleResId = playbackService.isPlaying() ? R.drawable.notification_pause : R.drawable.notification_play;

        builder.addAction(R.drawable.notification_previous, "", previousIntent)
                .addAction(toggleResId, "", togglePlayIntent)
                .addAction(R.drawable.notification_next, "", nextIntent);



        if (!preLollipop) {
            builder.setVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        }


        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt);


        Boolean unlock;
        unlock = playbackService.isPlaying();
        builder.setSmallIcon(R.drawable.ic_stat_note)
                .setShowWhen(false)
                .setOngoing(unlock);


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            builder.setStyle(new NotificationCompat.MediaStyle()
                    .setMediaSession(playbackService.getMediaSession().getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2));
        }

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
                .setContentTitle(playbackService.getSongTitle())
                .setContentText(playbackService.getArtistName())
                .setSubText(playbackService.getAlbumName());


        builder.setSmallIcon(R.drawable.ic_stat_note);


        playbackService.startForeground(NOTIFY_ID, builder.build());
    }


    public static void removeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);
    }
}