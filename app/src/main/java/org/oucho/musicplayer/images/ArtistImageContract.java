package org.oucho.musicplayer.images;

import android.provider.BaseColumns;


class ArtistImageContract {

    public ArtistImageContract() {}

    public class Entry implements BaseColumns {
        public static final String TABLE_NAME = "artist_images";

        public static final String COLUMN_NAME_MBID = "mbid";
        public static final String COLUMN_NAME_ARTIST_NAME = "artist_name";
        public static final String COLUMN_NAME_ARTIST_IMAGE = "artist_image";

    }
}
