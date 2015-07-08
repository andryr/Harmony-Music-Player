package com.andryr.musicplayer;

import java.util.List;

public interface OnSongSelectedListener {
    void onSongSelected(List<Song> songList, int position);
}
