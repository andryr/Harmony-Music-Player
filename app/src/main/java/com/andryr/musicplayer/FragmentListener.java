package com.andryr.musicplayer;

import java.util.List;

public interface FragmentListener {
    void onSongSelected(List<Song> songList, int position);

    void onShuffleRequested(List<Song> songList, boolean play);
}
