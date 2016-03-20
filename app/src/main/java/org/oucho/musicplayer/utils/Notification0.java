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
import android.widget.RemoteViews;

import org.oucho.musicplayer.MainActivity;
import org.oucho.musicplayer.PlaybackService;
import org.oucho.musicplayer.R;
import org.oucho.musicplayer.images.ArtworkCache;
import org.oucho.musicplayer.images.BitmapCache;




public class Notification0 {
    private static final int NOTIFY_ID = 32;


    public static void updateNotification(@NonNull final PlaybackService playbackService) {

        if (!playbackService.hasPlaylist()) {
            return; // no need to go further since there is nothing to display
        }


        final NotificationCompat.Builder builder = new NotificationCompat.Builder(playbackService);


        Intent intent = new Intent(playbackService, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(playbackService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Boolean unlock;
        if (playbackService.isPlaying()) {
            unlock = true;
        } else {
            unlock = false;
        }
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setColor(playbackService.getResources().getColor(R.color.controls_bg_dark))
                .setShowWhen(false)
                .setOngoing(unlock)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendInt)
                .setSmallIcon(R.drawable.ic_stat_note);



        int toggleResId = playbackService.isPlaying() ? R.drawable.notification_pause : R.drawable.notification_play;

        //Previous
        builder.addAction(R.drawable.notification_previous, "action_previous", PendingIntent.getService(playbackService, 0, new Intent(playbackService, PlaybackService.class)
                .setAction(PlaybackService.ACTION_PREVIOUS), 0));

        // Play/Pause
        builder.addAction(toggleResId, "play_pause", PendingIntent.getService(playbackService, 0, new Intent(playbackService, PlaybackService.class)
                .setAction(PlaybackService.ACTION_TOGGLE), 0));

        // Next
        builder.addAction(R.drawable.notification_next, "action_next",
                PendingIntent.getService(playbackService, 0, new Intent(playbackService, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_NEXT), 0));


        builder.setContentTitle(playbackService.getSongTitle())
                .setContentText(playbackService.getArtistName())
                .setSubText(playbackService.getAlbumName());

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
            bitmap = d != null ? d.getBitmap() : null;
        }
        builder.setLargeIcon(bitmap);

        android.app.Notification notification = builder.build();
        NotificationManager mNotifyManager = (NotificationManager) playbackService.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(NOTIFY_ID, notification);
    }
}
