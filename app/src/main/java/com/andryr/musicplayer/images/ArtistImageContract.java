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

package com.andryr.musicplayer.images;

import android.provider.BaseColumns;

/**
 * Created by Andry on 18/10/15.
 */
public class ArtistImageContract {

    public ArtistImageContract() {};

    public class Entry implements BaseColumns {
        public static final String TABLE_NAME = "artist_images";

        public static final String COLUMN_NAME_MBID = "mbid";
        public static final String COLUMN_NAME_ARTIST_NAME = "artist_name";
        public static final String COLUMN_NAME_ARTIST_IMAGE = "artist_image";

    }
}
