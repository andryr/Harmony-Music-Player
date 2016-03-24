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
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.database.DatabaseUtilsCompat;

import com.andryr.musicplayer.R;
import com.andryr.musicplayer.model.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andry on 23/08/15.
 */
public class AlbumLoader extends BaseLoader<List<Album>> {


    private static final String[] sProjection = {BaseColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.FIRST_YEAR,


            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS};

    private String mArtist = null;

    private List<Album> mAlbumList;


    public AlbumLoader(Context context) {
        super(context);
    }

    public AlbumLoader(Context context, String artist) {
        super(context);
        mArtist = artist;
    }


    @Override
    public List<Album> loadInBackground() {


        mAlbumList = new ArrayList<>();

        Cursor cursor = getAlbumCursor();

        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(BaseColumns._ID);

            int albumNameCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
            int artistCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
            int yearCol = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR);

            int songsNbCol = cursor
                    .getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);

            do {

                long id = cursor.getLong(idCol);

                String name = cursor.getString(albumNameCol);
                if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                    name = getContext().getString(R.string.unknown_album);
                    id = -1;
                }
                String artist = cursor.getString(artistCol);
                int year = cursor.getInt(yearCol);
                int count = cursor.getInt(songsNbCol);


                mAlbumList.add(new Album(id, name, artist, year, count));

            } while (cursor.moveToNext());


        }


        if (cursor != null) {
            cursor.close();
        }
        return mAlbumList;
    }

    private Cursor getAlbumCursor() {

        Uri musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String selection = getSelectionString();
        String[] selectionArgs = getSelectionArgs();
        if (mArtist != null) {
            selection = DatabaseUtilsCompat.concatenateWhere(selection, MediaStore.Audio.Albums.ARTIST + " = ?");
            selectionArgs = DatabaseUtilsCompat.appendSelectionArgs(selectionArgs, new String[]{mArtist});

        }

        String fieldName = MediaStore.Audio.Albums.ALBUM;
        String filter = getFilter();
        return getCursor(musicUri, sProjection, selection, selectionArgs, fieldName, filter);
    }


}
