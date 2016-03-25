package org.oucho.musicplayer.model.db.queue;

import org.oucho.musicplayer.model.db.SongListColumns;


class QueueContract {

    public class QueueEntry implements SongListColumns {
        public static final String TABLE_NAME = "queue";
    }
}