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

package com.andryr.musicplayer.model;

import android.provider.MediaStore;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private int trackNumber;
    private long albumId;
    private String genre;
    private long duration;

    public Song(long id, String title, String artist, String album, long albumId, int trackNumber, long duration) {
        super();
        this.id = id;
        this.title = title == null ? MediaStore.UNKNOWN_STRING : title;
        this.artist = artist == null ? MediaStore.UNKNOWN_STRING : artist;
        this.album = album == null ? MediaStore.UNKNOWN_STRING : album;
        this.albumId = albumId;
        this.trackNumber = trackNumber;
        this.duration = duration;

    }

    public long getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getAlbumId() {
        return albumId;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
