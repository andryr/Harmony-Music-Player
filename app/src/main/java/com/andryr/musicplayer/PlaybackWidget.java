package com.andryr.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class PlaybackWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);



        views.setTextViewText(R.id.title,context.getResources().getString(R.string.touch_to_select_a_song));

        setUpButtons(context, views);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setUpButtons(Context context, RemoteViews views) {
        PendingIntent chooseSongIntent = PendingIntent.getService(context, 0,
                new Intent(context, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_CHOOSE_SONG), 0);
        views.setOnClickPendingIntent(R.id.song_info,chooseSongIntent);

        PendingIntent togglePlayIntent = PendingIntent.getService(context, 0,
                new Intent(context, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_TOGGLE), 0);
        views.setOnClickPendingIntent(R.id.play_pause_toggle,
                togglePlayIntent);

        PendingIntent nextIntent = PendingIntent.getService(context, 0,
                new Intent(context, PlaybackService.class).setAction(PlaybackService.ACTION_NEXT),
                0);
        views.setOnClickPendingIntent(R.id.next, nextIntent);

        PendingIntent previousIntent = PendingIntent.getService(context, 0,
                new Intent(context, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_PREVIOUS), 0);
        views.setOnClickPendingIntent(R.id.prev, previousIntent);
    }

    private static void updateAppWidget(PlaybackService service, int appWidgetId) {
        if (service == null) {
            return;
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(service);

        RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.playback_widget);
        views.setTextViewText(R.id.title, service.getTrackName());
        views.setTextViewText(R.id.artist, service.getArtistName());
        Drawable d = ImageUtils.getArtworkDrawable(service,service.getAlbumId());
        if(d != null) {
            views.setImageViewBitmap(R.id.album_artwork, ((BitmapDrawable) d).getBitmap());
        }
        else
        {
            views.setImageViewResource(R.id.album_artwork,R.drawable.default_artwork);
        }
        if(service.isPlaying())
        {
            views.setImageViewResource(R.id.play_pause_toggle,R.drawable.ic_pause);

        }
        else
        {
            views.setImageViewResource(R.id.play_pause_toggle,R.drawable.ic_play_small);

        }
        setUpButtons(service, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    public static void updateAppWidget(PlaybackService service, int appWidgetIds[]) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(service, appWidgetIds[i]);
        }
    }


}

