package com.andryr.musicplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;


public class ID3TagEditorDialog extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_ARTIST = "artist";
    private static final String ARG_ALBUM = "album";
    private static final String ARG_ALBUM_ID = "album_id";
    private static final String ARG_TRACK_NUMBER = "track_number";


    private Song mSong;


    private EditText mTitleEditText;
    private EditText mArtistEditText;
    private EditText mAlbumEditText;
    private EditText mTrackEditText;
    private EditText mGenreEditText;


    public ID3TagEditorDialog() {
        // Required empty public constructor
    }

    public static ID3TagEditorDialog newInstance(Song song) {
        ID3TagEditorDialog fragment = new ID3TagEditorDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, song.getId());
        args.putString(ARG_TITLE, song.getTitle());
        args.putString(ARG_ARTIST, song.getArtist());
        args.putString(ARG_ALBUM, song.getAlbum());
        args.putLong(ARG_ALBUM_ID, song.getAlbumId());
        args.putInt(ARG_TRACK_NUMBER, song.getTrackNumber());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {

            long id = args.getLong(ARG_ID);
            String title = args.getString(ARG_TITLE);
            String artist = args.getString(ARG_ARTIST);
            String album = args.getString(ARG_ALBUM);
            long albumId = args.getLong(ARG_ALBUM_ID);
            int trackNumber = args.getInt(ARG_TRACK_NUMBER);
            mSong = new Song(id, title, artist, album, albumId, trackNumber);


        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_tags);

        mSong.setGenre(MusicLibraryHelper.getSongGenre(getActivity(),mSong.getId()));
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_id3_tag_editor_dialog, null);
        builder.setView(dialogView);

        mTitleEditText = (EditText) dialogView.findViewById(R.id.title);
        mArtistEditText = (EditText) dialogView.findViewById(R.id.artist);
        mAlbumEditText = (EditText) dialogView.findViewById(R.id.album);
        mTrackEditText = (EditText) dialogView.findViewById(R.id.track_number);
        mGenreEditText = (EditText) dialogView.findViewById(R.id.genre);

        mTitleEditText.setText(mSong.getTitle());
        mArtistEditText.setText(mSong.getArtist());
        mAlbumEditText.setText(mSong.getAlbum());
        mTrackEditText.setText(String.valueOf(mSong.getTrackNumber()));
        mGenreEditText.setText(mSong.getGenre());


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();

                boolean b = saveTags();

                if (!b) {
                    //TODO message d'erreur
                }
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });


        return builder.create();
    }

    private boolean saveTags() {

        HashMap<String,String> tags = new HashMap<>();

        tags.put(MusicLibraryHelper.TITLE, mTitleEditText.getText().toString());
        tags.put(MusicLibraryHelper.ARTIST,  mArtistEditText.getText().toString());
        tags.put(MusicLibraryHelper.ALBUM,  mAlbumEditText.getText().toString());
        tags.put(MusicLibraryHelper.TRACK,  mTrackEditText.getText().toString());
        tags.put(MusicLibraryHelper.GENRE,  mGenreEditText.getText().toString());




        return MusicLibraryHelper.editSongTags(getActivity(), mSong, tags);

    }


}
