package com.andryr.musicplayer;

import com.andryr.musicplayer.model.Song;

import java.util.List;

/**
 * Created by Andry on 27/10/15.
 */
public interface FragmentListener {
    void onSongSelected(List<Song> songList, int position);

    void onShuffleRequested(List<Song> songList, boolean play);
}
