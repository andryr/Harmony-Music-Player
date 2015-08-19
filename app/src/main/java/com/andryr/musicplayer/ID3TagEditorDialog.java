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
import java.util.Locale;


public class ID3TagEditorDialog extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_ARTIST = "artist";
    private static final String ARG_ALBUM = "album";
    private static final String ARG_ALBUM_ID = "album_id";


    private Song mSong;


    private EditText mTitleEditText;
    private EditText mArtistEditText;
    private EditText mAlbumEditText;


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
            mSong = new Song(id, title, artist, album, albumId);

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.edit_tags);

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_id3_tag_editor_dialog, null);
        builder.setView(dialogView);

        mTitleEditText = (EditText) dialogView.findViewById(R.id.title);
        mArtistEditText = (EditText) dialogView.findViewById(R.id.artist);
        mAlbumEditText = (EditText) dialogView.findViewById(R.id.album);

        mTitleEditText.setText(mSong.getTitle());
        mArtistEditText.setText(mSong.getArtist());
        mAlbumEditText.setText(mSong.getAlbum());


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
        String newTitle = mTitleEditText.getText().toString();
        String newArtist = mArtistEditText.getText().toString();
        String newAlbum = mAlbumEditText.getText().toString();

        Uri songUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mSong.getId());

        File f = new File(ContentHelper.getRealPathFromUri(getActivity(), songUri));

        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(f);
        } catch (CannotReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }

        Tag tag = null;

        if (audioFile != null) {
            tag = audioFile.getTag();
        } else {
            Log.d("tag", "audiofile null");

        }


        if (tag != null) {
            Log.d("tag", "not null");
            ContentValues values = new ContentValues();

            if (!mSong.getTitle().equals(newTitle)) {
                try {
                    tag.setField(FieldKey.TITLE, newTitle);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                values.put(MediaStore.Audio.Media.TITLE, newTitle);
                Log.d("tag", "title");

            }
            if (!mSong.getArtist().equals(newArtist)) {
                try {
                    tag.setField(FieldKey.ARTIST, newArtist);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                values.put(MediaStore.Audio.Media.ARTIST, newArtist);
                Log.d("tag", "artist");

            }
            if (!mSong.getAlbum().equals(newAlbum)) {
                try {
                    tag.setField(FieldKey.ALBUM, newAlbum);
                } catch (FieldDataInvalidException e) {
                    e.printStackTrace();
                }

                Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{BaseColumns._ID,
                        MediaStore.Audio.AlbumColumns.ALBUM, MediaStore.Audio.AlbumColumns.ALBUM_KEY, MediaStore.Audio.AlbumColumns.ARTIST}, MediaStore.Audio.AlbumColumns.ALBUM + " = ?", new String[]{newAlbum}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);


                if (cursor != null && cursor.moveToFirst()) {

                    long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

                    values.put(MediaStore.Audio.Media.ALBUM_ID, id);


                    Log.d("er", String.valueOf(id));


                } else {

                    values.put(MediaStore.Audio.Media.ALBUM, newAlbum);
                }
                Log.d("tag", "album");

            }

            if (values.size() > 0) {

                getActivity().getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media._ID + "=" + mSong.getId(), null);
            }
            return true;
        }


        return false;
    }


}
