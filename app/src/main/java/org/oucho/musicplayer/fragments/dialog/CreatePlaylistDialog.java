package org.oucho.musicplayer.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.oucho.musicplayer.utils.Playlists;
import org.oucho.musicplayer.R;


public class CreatePlaylistDialog extends DialogFragment {


    private OnPlaylistCreatedListener mListener;


    public CreatePlaylistDialog() {
        // Required empty public constructor
    }

    public static CreatePlaylistDialog newInstance() {

        return new CreatePlaylistDialog();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View layout = LayoutInflater.from(getActivity()).inflate(R.layout.create_playlist_dialog,
                new LinearLayout(getActivity()), false);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_playlist)
                .setView(layout)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                EditText editText = (EditText) layout
                                        .findViewById(R.id.playlist_name);
                                Playlists.createPlaylist(getActivity()
                                        .getContentResolver(), editText
                                        .getText().toString());

                                if (mListener != null) {
                                    mListener.onPlaylistCreated();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        });


        return builder.create();
    }




    public void setOnPlaylistCreatedListener(OnPlaylistCreatedListener listener) {
        mListener = listener;
    }


    public interface OnPlaylistCreatedListener {


        void onPlaylistCreated();
    }


}
