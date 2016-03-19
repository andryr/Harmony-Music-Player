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

package com.andryr.musicplayer.model.db;

import android.provider.BaseColumns;

/**
 * Created by Andry on 08/11/15.
 */
public interface SongListColumns extends BaseColumns {
    String COLUMN_NAME_SONG_ID = "song_id";
    String COLUMN_NAME_TITLE = "title";
    String COLUMN_NAME_ARTIST = "artist";
    String COLUMN_NAME_ALBUM = "album";
    String COLUMN_NAME_TRACK_NUMBER = "number";
    String COLUMN_NAME_ALBUM_ID = "album_id";
    String COLUMN_NAME_GENRE = "genre";
}
