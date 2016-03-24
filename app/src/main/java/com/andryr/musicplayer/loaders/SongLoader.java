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

package com.andryr.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.database.DatabaseUtilsCompat;
import android.util.Log;

import com.andryr.musicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by andry on 21/08/15.
 */
public class SongLoader extends BaseLoader<List<Song>> {

    private static final String[] sProjection = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION
    };

    private List<Song> mSongList = null;

    public SongLoader(Context context) {
        super(context);

    }

    @Override
    public List<Song> loadInBackground() {
        mSongList = new ArrayList<>();

        Cursor cursor = getSongCursor();
        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor
                    .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
            if (idCol == -1) {
                idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            }
            int titleCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int trackCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TRACK);
            int durationCol = cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);

                String artist = cursor.getString(artistCol);

                String album = cursor.getString(albumCol);

                long albumId = cursor.getLong(albumIdCol);

                int track = cursor.getInt(trackCol);

                long duration = cursor.getLong(durationCol);


                mSongList.add(new Song(id, title, artist, album, albumId, track, duration));
            } while (cursor.moveToNext());

         /*   Collections.sort(mSongList, new Comparator<Song>() {

                @Override
                public int compare(Song lhs, Song rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getTitle(), rhs.getTitle());
                }
            });*/

        }

        if (cursor != null) {
            cursor.close();
        }
        Log.e("test", "  d " + mSongList.size());

        return mSongList;
    }

    protected Uri getContentUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    protected Cursor getSongCursor() {

        Uri musicUri = getContentUri();

        String selection = getSelectionString();
        String[] selectionArgs = getSelectionArgs();

        selection = DatabaseUtilsCompat.concatenateWhere(selection, MediaStore.Audio.Media.IS_MUSIC + " = 1");

        String fieldName = MediaStore.Audio.Media.TITLE;
        String filter = getFilter();
        return getCursor(musicUri, sProjection, selection, selectionArgs, fieldName, filter);
    }


}
