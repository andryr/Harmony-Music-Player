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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.model.Song;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andry on 19/08/15.
 */
public class MusicLibraryHelper {

    public static final String TITLE = "title";
    public static final String ARTIST = "artist";
    public static final String ALBUM = "album";
    public static final String TRACK = "track";
    public static final String GENRE = "genre";
    public static final String ALBUM_NAME = "album_name";
    public static final String ARTIST_NAME = "artist_name";
    public static final String YEAR = "year";


    private static Map<Long, String> getGenres(Context context) {
        HashMap<Long, String> genreIdMap = new HashMap<>();
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME},
                null, null, null);

        if (c != null && c.moveToFirst()) {
            do {
                genreIdMap.put(c.getLong(0), c.getString(1));
            } while (c.moveToNext());


        }


        if (c != null) {
            c.close();
        }
        return genreIdMap;
    }

    private final static long getGenreId(Context context, String genreName) {
        long id = -1;
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME},
                null, null, null);

        if (c != null && c.moveToFirst()) {
            do {
                if (genreName.equals(c.getString(1))) {
                    id = c.getLong(0);
                    break;
                }
            } while (c.moveToNext());


        }


        if (c != null) {
            c.close();
        }

        return id;
    }

    public final static String getSongGenre(Context context, long songId) {


        Map<Long, String> genreIdMap = getGenres(context);

        String genre = "";
        for (Long genreId : genreIdMap.keySet()) {

            boolean found = false;
            Cursor c = context.getContentResolver().query(
                    MediaStore.Audio.Genres.Members.getContentUri(
                            "external", genreId),
                    new String[]{MediaStore.Audio.Genres.Members.AUDIO_ID},
                    MediaStore.Audio.Genres.Members.AUDIO_ID + " = " + songId,
                    null, null);


            if (c.getCount() != 0) {
                genre = genreIdMap.get(genreId);
                found = true;
            }

            if (c != null) {
                c.close();
            }

            if (found) {
                break;
            }
        }
        return genre;
    }

    public final static long getSongGenreId(Context context, long songId) {


        Map<Long, String> genreIdMap = getGenres(context);

        long genreId = -1;
        for (Long l : genreIdMap.keySet()) {
            Cursor c = context.getContentResolver().query(
                    MediaStore.Audio.Genres.Members.getContentUri(
                            "external", l),
                    new String[]{MediaStore.Audio.Genres.Members.AUDIO_ID},
                    MediaStore.Audio.Genres.Members.AUDIO_ID + " = " + songId,
                    null, null);


            if (c.getCount() != 0) {
                genreId = l;
            }

            if (c != null) {
                c.close();
            }
        }

        return genreId;
    }


    public final static boolean editSongTags(Context context, Song song, Map<String, String> tags) {


        String newTitle = tags.get(TITLE) == null ? song.getTitle() : tags.get(TITLE);
        String newArtist = tags.get(ARTIST) == null ? song.getArtist() : tags.get(ARTIST);
        String newAlbum = tags.get(ALBUM) == null ? song.getAlbum() : tags.get(ALBUM);
        String newTrackNumber = tags.get(TRACK) == null ? String.valueOf(song.getTrackNumber()) : tags.get(TRACK);
        String newGenre = tags.get(GENRE) == null ? song.getGenre() : tags.get(GENRE);
        Uri songUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.getId());

        File f = new File(ContentHelper.getRealPathFromUri(context, songUri));

        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(f);
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }

        Tag tag = null;

        if (audioFile != null) {
            tag = audioFile.getTag();
        } else {
            Log.d("tag", "audiofile null");

        }


        if (tag != null) {
            Log.d("tag", "not null");
            ContentValues values = new ContentValues();

            if (!song.getTitle().equals(newTitle)) {
                try {
                    tag.setField(FieldKey.TITLE, newTitle);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                values.put(MediaStore.Audio.Media.TITLE, newTitle);
                Log.d("tag", "title");

            }
            if (!song.getArtist().equals(newArtist)) {
                try {
                    tag.setField(FieldKey.ARTIST, newArtist);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                values.put(MediaStore.Audio.Media.ARTIST, newArtist);
                Log.d("tag", "artist");

            }
            if (!song.getAlbum().equals(newAlbum)) {
                try {
                    tag.setField(FieldKey.ALBUM, newAlbum);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{BaseColumns._ID,
                        MediaStore.Audio.AlbumColumns.ALBUM, MediaStore.Audio.AlbumColumns.ALBUM_KEY, MediaStore.Audio.AlbumColumns.ARTIST}, MediaStore.Audio.AlbumColumns.ALBUM + " = ?", new String[]{newAlbum}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);


                if (cursor != null && cursor.moveToFirst()) {

                    long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

                    values.put(MediaStore.Audio.Media.ALBUM_ID, id);


                    Log.d("er", String.valueOf(id));


                } else {

                    values.put(MediaStore.Audio.Media.ALBUM, newAlbum);
                }


                if (cursor != null) {
                    cursor.close();
                }
                Log.d("tag", "album");

            }
            if (!String.valueOf(song.getTrackNumber()).equals(newTrackNumber)) {
                try {
                    tag.setField(FieldKey.TRACK, newTrackNumber);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                values.put(MediaStore.Audio.Media.TRACK, newTrackNumber);
            }
            if (!song.getGenre().equals(newGenre)) {
                editSongGenre(context, song, newGenre);
            }
            if (values.size() > 0) {

                context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media._ID + "=" + song.getId(), null);
            }
            return true;
        }


        return false;
    }


    private final static void editSongGenre(Context context, Song song, String newGenre) {
        long genreId = getSongGenreId(context, song.getId());

        if (genreId != -1)//si la chanson se trouve dans une des tables Genres.Members on supprime l'entrée correspondante
        {
            context.getContentResolver().delete(MediaStore.Audio.Genres.Members.getContentUri(
                    "external", genreId), MediaStore.Audio.Genres.Members.AUDIO_ID + " = " + song.getId(), null);
        }


        genreId = getGenreId(context, newGenre);
        ContentValues values = new ContentValues();

        if (genreId != -1)//si le nouveau genre existe dans la bdd
        {
            values.put(MediaStore.Audio.Genres.Members.AUDIO_ID, song.getId());
            values.put(MediaStore.Audio.Genres.Members.GENRE_ID, genreId);

            context.getContentResolver().insert(MediaStore.Audio.Genres.Members.getContentUri(
                    "external", genreId), values);
        } else {
            values.put(MediaStore.Audio.Genres.NAME, newGenre);
            context.getContentResolver().insert(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, values);
            genreId = getGenreId(context, newGenre);

            if (genreId != -1) {
                values.clear();
                values.put(MediaStore.Audio.Genres.Members.AUDIO_ID, song.getId());
                values.put(MediaStore.Audio.Genres.Members.GENRE_ID, genreId);

                context.getContentResolver().insert(MediaStore.Audio.Genres.Members.getContentUri(
                        "external", genreId), values);
            }

        }
    }

    public final static boolean editAlbumData(Context context, Album mAlbum, HashMap<String, String> data) {
        ContentValues values = new ContentValues();

        String newName = data.get(ALBUM_NAME) == null ? mAlbum.getAlbumName() : data.get(ALBUM_NAME);
        String newArtistName = data.get(ARTIST_NAME) == null ? mAlbum.getArtistName() : data.get(ARTIST_NAME);
        String newYear = data.get(YEAR) == null ? String.valueOf(mAlbum.getYear()) : data.get(YEAR);

        if (!mAlbum.getAlbumName().equals(newName)) {
            values.put(MediaStore.Audio.Media.ALBUM, newName);
        }

        if (!mAlbum.getArtistName().equals(newArtistName)) {
            values.put(MediaStore.Audio.Media.ARTIST, newArtistName);
        }

        if (!String.valueOf(mAlbum.getYear()).equals(newYear)) {
            values.put(MediaStore.Audio.Media.YEAR, newYear);
        }

        if (values.size() > 0) {
            context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media.ALBUM_ID + "=" + mAlbum.getId(), null);
            return true;
        }
        return false;

    }

}
