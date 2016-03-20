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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.andryr.musicplayer.images.ArtworkCache;

/**
 * Implementation of App Widget functionality.
 */
public class PlaybackWidget extends AppWidgetProvider {


    private static int sArtworkSize;

    public static void updateAppWidget(PlaybackService service, int appWidgetIds[]) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(service, appWidgetIds[i]);
        }
    }

    private static void updateAppWidget(PlaybackService service, int appWidgetId) {
        if (service == null) {
            return;
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(service);

        RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.playback_widget);
        views.setTextViewText(R.id.title, service.getSongTitle());
        views.setTextViewText(R.id.artist, service.getArtistName());
        Bitmap b = ArtworkCache.getInstance().getCachedBitmap(service.getAlbumId(), sArtworkSize, sArtworkSize);
        if (b != null) {
            views.setImageViewBitmap(R.id.album_artwork, b);
        } else {
            views.setImageViewResource(R.id.album_artwork, R.drawable.default_artwork);
        }
        if (service.isPlaying()) {
            views.setImageViewResource(R.id.play_pause_toggle, R.drawable.ic_pause);

        } else {
            views.setImageViewResource(R.id.play_pause_toggle, R.drawable.ic_play_small);

        }
        setUpButtons(service, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

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
        sArtworkSize = context.getResources().getDimensionPixelSize(R.dimen.widget_art_size);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.playback_widget);


        views.setTextViewText(R.id.title, context.getResources().getString(R.string.touch_to_select_a_song));

        setUpButtons(context, views);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setUpButtons(Context context, RemoteViews views) {
        PendingIntent chooseSongIntent = PendingIntent.getService(context, 0,
                new Intent(context, PlaybackService.class)
                        .setAction(PlaybackService.ACTION_CHOOSE_SONG), 0);
        views.setOnClickPendingIntent(R.id.song_info, chooseSongIntent);

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


}

