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

public class Album {

    private long id;
    private String albumName;
    private String artistName;
    private int year;
    private int trackCount;

    public Album(long id, String albumName, String artistName, int year, int trackCount) {
        super();
        this.id = id;
        this.albumName = albumName == null ? MediaStore.UNKNOWN_STRING : albumName;
        this.artistName = artistName == null ? MediaStore.UNKNOWN_STRING : artistName;
        this.year = year;
        this.trackCount = trackCount;
    }

    public long getId() {
        return id;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getYear() {
        return year;
    }

    public int getTrackCount() {
        return trackCount;
    }


}
